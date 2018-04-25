package com.wenchen.android.earthquakeviewer;

import android.content.SharedPreferences;
import android.location.LocationManager;

/**
 * Created by WenChen on 2018/3/31.
 */

public class SettingPreference
{
	private int sortStyle;
	private int displayStyle;

	private int startYear, startMonth, startDay, endYear, endMonth, endDay;
	private float latitude, longitude;
	private String minMagnitude;
	private String limit;

	SharedPreferences settings;
	LocationManager manager;

	public SettingPreference(SharedPreferences settings)
	{
		/**
		 * 创建时首先加载设置，该设置可以被存储
		 * */

		//获得地震数据排序方式，没有就按钮时间排序(0, 按震级排序 1)
		sortStyle = settings.getInt("sortStyle", 0);

		//获得显示样式，没有就降序(1, 升序 0)
		displayStyle = settings.getInt("displayStyle", 1);

		//获得起始日期，没有就为 2008-05-11
		//月份从 0 开始
		startYear = settings.getInt("startYear", 2008);
		startMonth = settings.getInt("startMonth", 4);
		startDay = settings.getInt("startDay", 11);

		//获得终止日期，没有就为 2008-05-13
		endYear = settings.getInt("endYear", 2008);
		endMonth = settings.getInt("endMonth", 4);
		endDay = settings.getInt("endDay", 13);

		//经纬度，没有就为 91 和 181，表示不设置
		//纬度范围 [-90, 90] 经度范围 [-180, 180]
		latitude = settings.getFloat("latitude", 91);
		longitude = settings.getFloat("longitude", 181);

		//获得最小震级，没有就设置为 6 级
		minMagnitude = settings.getString("minMagnitude", "6");

		//获得地震数量限制，没有就设置为 100
		limit = settings.getString("limit", "100");

		//获取设置文件
		this.settings = settings;
	}

	//sortStyle 的 getter 和 setter
	public void setSortStyle(int sortStyle)
	{
		this.sortStyle = sortStyle;
	}

	public int getSortStyle()
	{
		return sortStyle;
	}

	//displayStyle 的 getter 和 setter
	public void setDisplayStyle(int displayStyle)
	{
		this.displayStyle = displayStyle;
	}

	public int getDisplayStyle()
	{
		return displayStyle;
	}

	//startYear 的 getter 和 setter
	public void setStartYear(int startYear)
	{
		this.startYear = startYear;
	}

	public int getStartYear()
	{
		return startYear;
	}

	//startMonth 的 getter 和 setter
	public void setStartMonth(int startMonth)
	{
		this.startMonth = startMonth;
	}

	public int getStartMonth()
	{
		return startMonth;
	}

	//startDay 的 getter 和 setter
	public void setStartDay(int startDay)
	{
		this.startDay = startDay;
	}

	public int getStartDay()
	{
		return startDay;
	}

	//endYear 的 getter 和 setter
	public void setEndYear(int endYear)
	{
		this.endYear = endYear;
	}

	public int getEndYear()
	{
		return endYear;
	}

	//endMonth 的 getter 和 setter
	public void setEndMonth(int endMonth)
	{
		this.endMonth = endMonth;
	}

	public int getEndMonth()
	{
		return endMonth;
	}

	//endDay 的 getter 和 setter
	public void setEndDay(int endDay)
	{
		this.endDay = endDay;
	}

	public int getEndDay()
	{
		return endDay;
	}

	//latitude 的 getter 和 setter
	public void setLatitude(float latitude)
	{
		this.latitude = latitude;
	}

	public float getLatitude()
	{
		return latitude;
	}

	//longitude 的 getter 和 setter
	public void setLongitude(float longitude)
	{
		this.longitude = longitude;
	}

	public float getLongitude()
	{
		return longitude;
	}

	//minMagnitude 的 getter 和 setter
	public void setMinMagnitude(String minMagnitude)
	{
		this.minMagnitude = minMagnitude;
	}

	public String getMinMagnitude()
	{
		return minMagnitude;
	}

	//limit 的 getter 和 setter
	public void setLimit(String limit)
	{
		this.limit = limit;
	}

	public String getLimit()
	{
		return limit;
	}

	public void setPreferences()
	{
		SharedPreferences.Editor editor = settings.edit();

		editor.putInt("sortStyle", sortStyle);

		editor.putInt("displayStyle", displayStyle);

		editor.putInt("startYear", startYear);
		editor.putInt("startMonth", startMonth);
		editor.putInt("startDay", startDay);

		editor.putInt("endYear", endYear);
		editor.putInt("endMonth", endMonth);
		editor.putInt("endDay", endDay);

		editor.putFloat("latitude", latitude);
		editor.putFloat("longitude", longitude);

		editor.putString("minMagnitude", minMagnitude);
		editor.putString("limit", limit);

		editor.commit();
	}

	public String getStartDate()
	{
		return startYear + "-" + (startMonth + 1) + "-" + startDay;
	}

	public String getEndDate()
	{
		return endYear + "-" + (endMonth + 1) + "-" + endDay;
	}

	//根据 sortStyle 和 displayStyle 返回实际排序参数
	public String getSortMethod()
	{
		//按时间升序
		if(this.sortStyle == 0 && this.displayStyle == 0)
			return "time-asc";
		else if(this.sortStyle == 0 && this.displayStyle == 1)
			return "time";
		//按震级升序
		else if(this.sortStyle == 1 && this.displayStyle == 0)
			return "magnitude-asc";
		else
			return "magnitude";
	}
}
