package com.wenchen.android.earthquakeviewer;

import android.content.AsyncTaskLoader;
import android.content.Context;


import java.util.List;

/**
 * Created by WenChen on 2018/3/24.
 */

//继承自异步线程任务的 Loader 可以使下载网络数据在后台下，不在主线程中，避免程序未响应
public class EarthquakeLoader extends AsyncTaskLoader<List<Earthquake>>
{
	//查询地震信息的url
	private String url;

	public EarthquakeLoader(Context context, String url)
	{
		super(context);
		this.url = url;
	}

	/**
	 * 覆盖 onStartLoading() 方法 来调用 forceLoad()，强制在后台开启任务
	 * 必须执行该步骤才能实际触发 loadInBackground() 方法的执行。
	 * */
	@Override
	protected void onStartLoading()
	{
		forceLoad();
	}

	@Override
	public List<Earthquake> loadInBackground()
	{
		//后台下载数据
		if(url == null)
			return null;
		List<Earthquake> earthquakes = QueryUtils.fetchEarthquakeData(url);

		return earthquakes;
	}
}
