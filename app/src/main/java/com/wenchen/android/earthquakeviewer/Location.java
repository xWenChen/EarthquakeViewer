package com.wenchen.android.earthquakeviewer;

/**
 * Created by WenChen on 2018/5/22.
 */

import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;

/**
 * 在地图显示(BDMActivity)中用于将地区分类的类
 * */

public class Location
{
	//两个属性：一个坐标点(经度，纬度)，一个记录地点相同的标记列表(即地震在原地图中地震列表的位置)
	private double latitude, longitude;
	private ArrayList<Integer> marker = new ArrayList<>();

	public Location()
	{

	}

	public Location(double latitude, double longitude, Integer i)
	{
		this.latitude = latitude;
		this.longitude = longitude;
		marker.add(i);
	}

	public void setLatitude(double latitude)
	{
		this.latitude = latitude;
	}

	public void setLongitude(double longitude)
	{
		this.longitude = longitude;
	}

	public void setMarker(ArrayList<Integer> marker)
	{
		this.marker = marker;
	}

	public double getLatitude()
	{

		return latitude;
	}

	public double getLongitude()
	{
		return longitude;
	}

	public ArrayList<Integer> getMarker()
	{
		return marker;
	}

	//添加标记
	public boolean add(Integer i)
	{
		return this.marker.add(i);
	}

	public boolean equals(Earthquake earthquake)
	{
		if(this.latitude == earthquake.getLatitude() && this.longitude == earthquake.getLongitude())
			return true;
		return false;
	}
}
