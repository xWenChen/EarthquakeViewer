package com.wenchen.android.earthquakeviewer;

/**
 * Created by WenChen on 2018/3/21.
 */

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 地震类：存储地震的等级大小{@link Earthquake.magnitude}，
 * 	地震的发生地点{@link Earthquake.place}，
 * 	地震的发生时间{@link Earthquake.time}，
 * 	地震的详细信息链接{@link Earthquake.uri}, 以及相应的 getter 方法
 * */
public class Earthquake implements Parcelable
{
	//属性
	/**
	 * 存储地震的等级大小
	 * */
	private double magnitude;

	/**
	 * 存储地震的发生地点
	 * */
	private String place;

	/**
	 * 发生地震地点的详细经纬度
	 * */
	private double latitude;
	private double longitude;

	/**
	 * 存储地震的发生时间，格式为 Unix 时间戳
	 * */
	private long time;

	/**
	 * 存储查询地震详细信息的 URI
	 * */
	private String uri;

	/**
	 * 构造器，三个参数为{@param Earthquake.magnitude},
	 * {@param Earthquake.place}, {@param Earthquake.time}, {@param Earthquake.uri}
	 * */
	public Earthquake(double magnitude, String place, double latitude, double longitude, long
			time, String uri)
	{
		this.magnitude = magnitude;
		this.place = place;
		this.latitude = latitude;
		this.longitude = longitude;

		this.time = time;
		this.uri = uri;
	}

	protected Earthquake(Parcel in)
	{
		magnitude = in.readDouble();
		place = in.readString();
		latitude = in.readDouble();
		longitude = in.readDouble();
		time = in.readLong();
		uri = in.readString();
	}

	/**
	 * 返回地震对象的地震等级 {@link Earthquake.magnitude}
	 * */
	public double getMagnitude()
	{
		return magnitude;
	}

	/**
	 * 返回地震的发生地点 {@link Earthquake.place}
	 * */
	public String getPlace()
	{
		return place;
	}

	/**
	 * 返回地震的发生地点纬度 {@link Earthquake.latitude}
	 * */
	public double getLatitude()
	{
		return latitude;
	}

	/**
	 * 返回地震的发生地点经度 {@link Earthquake.longitude}
	 * */
	public double getLongitude()
	{
		return longitude;
	}

	/**
	 * 返回地震的发生时间 {@link Earthquake.time}
	 * */
	public long getTime()
	{
		return time;
	}

	/**
	 * 返回可查询地震详细信息的 URI {@link Earthquake.uri}
	 * */
	public String getUri()
	{
		return uri;
	}

	@Override public int describeContents()
	{
		return 0;
	}

	//Parcelable接口的实现部分
	@Override public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeDouble(magnitude);
		dest.writeString(place);
		dest.writeDouble(latitude);
		dest.writeDouble(longitude);
		dest.writeLong(time);
		dest.writeString(uri);
	}

	public static final Creator<Earthquake> CREATOR = new Creator<Earthquake>()
	{
		@Override public Earthquake createFromParcel(Parcel in)
		{
			return new Earthquake(in);
		}

		@Override public Earthquake[] newArray(int size)
		{
			return new Earthquake[size];
		}
	};
}
