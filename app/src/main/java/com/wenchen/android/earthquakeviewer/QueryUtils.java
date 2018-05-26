package com.wenchen.android.earthquakeviewer;

/**
 * Created by WenChen on 2018/3/23.
 */

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * 创建查询条件 URL 的静态工具类，不能被更改
 * 无需创建实例
 */

public final class QueryUtils
{
	//用于打印日志信息的字符串
	private static final String LOG_TAG = "QueryUtils";

	/**
	 * 创建一个私有构造器是因为没有创建{@link QueryUtils}对象.
	 * QueryUtils的所有方法都是静态方法, 无需创建实例即可通过类名直接调用
	 * (即实例对象不需要，故将构造器声明为私有的).
	 */
	private QueryUtils()
	{
	}

	/**
	 * 创建查询地址的 URL
	 */
	private static URL createUrl(String stringUrl)
	{
		URL url = null;
		try
		{
			url = new URL(stringUrl);
		}
		catch(MalformedURLException e)
		{
			//打印日志信息
			Log.e(LOG_TAG, "创建 URL 错误: ", e);
		}
		return url;
	}

	/**
	 * 获得相应 URL 的请求数据
	 */
	private static String makeHttpRequest(URL url)
	{
		//请求后获得的回复
		String jsonResponse = "";

		//地址为空，无法连接，直接返回
		if(url == null)
			return jsonResponse;

		HttpURLConnection urlConnection = null;
		InputStream inputStream = null;

		/*
		* 查询链接：
		* https://developer.android.com/training/basics/network-ops/connecting.html
		* */
		try
		{
			//建立连接对象，并进行相应设置
			urlConnection = (HttpURLConnection)url.openConnection();
			//设置超时时间(读取输入流和网络连接时间)
			urlConnection.setConnectTimeout(15000 /* 毫秒 */);
			urlConnection.setReadTimeout(10000 /* 毫秒 */);
			//设置 HTTP 方法，包括GET, POST等等
			urlConnection.setRequestMethod("GET");

			//准备就绪，开始连接
			urlConnection.connect();

			//连接成功，返回状态码
			if(urlConnection.getResponseCode() == 200)
			{
				inputStream = urlConnection.getInputStream();
				jsonResponse = readFromStream(inputStream);
			}
			else
			{
				//打印日志信息
				Log.e(LOG_TAG, "网络响应码错误: " + urlConnection.getResponseCode());
			}
		}
		catch(ProtocolException e)
		{
			//打印日志信息
			Log.e(LOG_TAG, "协议错误: ", e);
		}
		catch(IOException e)
		{
			//打印日志信息
			Log.e(LOG_TAG, "解析结果错误: ", e);
		}
		finally
		{
			//断开连接，关闭输入流
			if(urlConnection != null)
				urlConnection.disconnect();
			try
			{
				if(inputStream != null)
					inputStream.close();
			}
			catch(IOException e)
			{
				//打印日志信息
				Log.e(LOG_TAG, "解析结果错误: ", e);
			}
		}
		return jsonResponse;
	}

	//根据 makeHttpRequest 函数得到的数据解析出所有 JSON 地震对象的信息
	private static ArrayList<Earthquake> extractFeatureFromJson(String earthquakeJson)
	{
		//可用 TextUtils.isEmpty(earthquakeJson) 代替
		if(earthquakeJson == null || earthquakeJson == "")
			return null;
		ArrayList<Earthquake> earthquakes = new ArrayList<>();

		try
		{
			JSONObject baseJsonResponse = new JSONObject(earthquakeJson);

			//获得 features 数组，里面包含了所有地震的详细信息
			JSONArray earthquakeArray = baseJsonResponse.getJSONArray("features");

			for(int i = 0; i < earthquakeArray.length(); i++)
			{
				/*地震相关信息路径：
				*	根 -> features数组 -> 第 i 个JSON地震对象数组
				*		-> properties对象 -> 具体信息
				* 具体说明链接：
				* 	https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php
				* */
				JSONObject currentEarthquake = earthquakeArray.getJSONObject(i);

				JSONObject properties = currentEarthquake.getJSONObject("properties");

				//获得地震信息：震级，地点，时间，详细信息链接
				double magnitude = properties.getDouble("mag");
				String place = properties.getString("place");
				long time = properties.getLong("time");
				String url = properties.getString("url");

				//获得经纬度的路径：coordinates是目标节点
				JSONObject geometry = currentEarthquake.getJSONObject("geometry");
				JSONArray coordinates = geometry.getJSONArray("coordinates");

				//获取经纬度
				double latitude = coordinates.getDouble(1);
				double longitude = coordinates.getDouble(0);

				//根据相关信息构造地震对象，并加入显示列表中
				Earthquake earthquake = new Earthquake(magnitude, place, latitude, longitude,
						time, url);
				earthquakes.add(earthquake);
			}
		}
		catch(JSONException e)
		{
			Log.e(LOG_TAG, "解析 JSON 数据出错：", e);
		}
		return earthquakes;
	}

	//最终调用是这个方法，根据相应 url 构造出地震信息的列表
	public static ArrayList<Earthquake> fetchEarthquakeData(String earthquakeUrl)
	{
		URL url = createUrl(earthquakeUrl);

		String jsonResponse = "";

		jsonResponse = makeHttpRequest(url);

		return extractFeatureFromJson(jsonResponse);
	}

	private static String readFromStream(InputStream inputStream)
	{
		StringBuilder output = new StringBuilder();
		/*
		* 查询网址：
		* 	https://stackoverflow.com/questions/2492076/android-reading-from-an-input-stream-efficiently
		* */
		//输入流不为空，进行下一步动作
		if(inputStream != null)
		{
			//设置输入流读取器的格式(此读取器将 byte 输入流转化为 character 输入流)
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset
					.forName("UTF-8"));
			//将设置加入到读取器中(一次可以读取很多字符，不限于一个)
			BufferedReader reader = new BufferedReader(inputStreamReader);

			try
			{
				String line = reader.readLine();
				while(line != null)
				{
					output.append(line);
					line = reader.readLine();
				}
			}
			catch(IOException e)
			{
				Log.e(LOG_TAG, "解析结果错误", e);
			}
		}

		return output.toString();
	}
}
