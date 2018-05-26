package com.wenchen.android.earthquakeviewer;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

//将地震数据以地图的形式显示出来的Activity

/**
 * 可优化的点，地震的格式化可以封装到一个类里面，而不是在每个类里加入相同的代码
 */

public class BDMActivity extends AppCompatActivity
{
	Intent data; //获得地震数据
	ArrayList<Earthquake> earthquakes;

	GeoCoder coder; //地理编码与反地理编码时用到

	/**
	 * 用于分离位置
	 */
	MapView mapView;
	BaiduMap baiduMap;

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_bdm);

		ExitApplication.getInstance().addActivity(this);

		//设置返回键
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mapView = (MapView) findViewById(R.id.main_map);

		data = getIntent();
		earthquakes = data.getParcelableArrayListExtra("data");

		init(); //初始化
	}

	@Override protected void onDestroy()
	{
		super.onDestroy();
		//在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mapView.onDestroy();
	}

	@Override protected void onResume()
	{
		super.onResume();
		//在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mapView.onResume();
	}

	@Override protected void onPause()
	{
		super.onPause();
		//在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mapView.onPause();
	}

	//重写后退键监听方法
	@Override public void onBackPressed()
	{
		new AlertDialog.Builder(this).setMessage("确定退出？").setPositiveButton("确定", new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				ExitApplication.getInstance().exit(BDMActivity.this);
				moveTaskToBack(true); //true表示不管是否在根任务，直接退出程序
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener()
		{
			@Override public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss(); //隐藏掉对话框
			}
		}).create().show();
		//super.onBackPressed();
	}

	private void init()
	{
		initBaiduMap();

		initMapView();

		initInfo(); //将地震信息添加到地图上
	}

	private void initBaiduMap()
	{
		//地图设置
		baiduMap = mapView.getMap();
		baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL); //设置地图类型为普通的地图
		baiduMap.setTrafficEnabled(false); //不显示交通图
		baiduMap.setBaiduHeatMapEnabled(false); //不显示百度热力图
		baiduMap.setBuildingsEnabled(false); //不显示建筑物

		MapStatusUpdate status = MapStatusUpdateFactory.newLatLngZoom(new LatLng(earthquakes.get(0).getLatitude(), earthquakes.get(0).getLongitude()), 4);
		baiduMap.setMapStatus(status); //设置上次指定的位置

		UiSettings settings = baiduMap.getUiSettings();
		settings.setOverlookingGesturesEnabled(false); //禁止3D俯视
		settings.setRotateGesturesEnabled(false); //禁止地图旋转
	}

	private void initMapView()
	{
		mapView.showScaleControl(false); //不显示默认比例尺控件
		//普通地图的缩放等级是4-21
		mapView.showZoomControls(false); //不显示默认缩放控件
	}

	//初始化地震信息
	private void initInfo()
	{
		for(int i = 0; i < earthquakes.size(); i++)
		{
			Earthquake earthquake = earthquakes.get(i);
			double maxMagnitude = earthquake.getMagnitude();
			LatLng point = new LatLng(earthquake.getLatitude(), earthquake.getLongitude());
			setMapOverlay(point, maxMagnitude);
		}

		initMarkerClickEvent();
	}

	//初始化标记的点击事件
	private void initMarkerClickEvent()
	{
		baiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener()
		{
			@Override public boolean onMarkerClick(Marker marker)
			{
				final LatLng location = marker.getPosition();
				final Earthquake earthquake = getTargetEarthquake(location);
				final double maxMagnitude = earthquake.getMagnitude();
				//显示消息信息框
				Button button = new Button(BDMActivity.this); //显示InfoWindow的button
				button.setText(getFormattedMagnitude(maxMagnitude));
				//第三个参数，y轴偏移量
				InfoWindow infoWindow = new InfoWindow(button, location, -150);
				baiduMap.showInfoWindow(infoWindow);

				//设置按钮的点击事件
				button.setOnClickListener(new View.OnClickListener()
				{
					@Override public void onClick(View v)
					{
						AlertDialog.Builder builder = new AlertDialog.Builder(BDMActivity.this);
						View view = getLayoutInflater().inflate(R.layout.bdm_info, null);

						/** 填充国内地点信息，需要用到反地理编码 */
						final TextView dlgPlace = (TextView) view.findViewById(R.id.dlg_place);
						coder = GeoCoder.newInstance();
						//设置反地理编码选项
						ReverseGeoCodeOption option = new ReverseGeoCodeOption().location(location);
						//发起反地理编码，先设置监听，顺序不能乱
						coder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener()
						{
							//地理编码
							@Override public void onGetGeoCodeResult(GeoCodeResult geoCodeResult)
							{

							}

							//反地理编码
							@Override public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult)
							{
								//没有收到
								if(reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR)
								{
									System.out.println("位置查询错误");
									return;
								}
								//进行国内外位置判断
								BDLocation where = new BDLocation();
								where.setLatitude(reverseGeoCodeResult.getLocation().latitude);
								where.setLongitude(reverseGeoCodeResult.getLocation().longitude);
								//在国内，显示中文地址
								if(where.getLocationWhere() == BDLocation.LOCATION_WHERE_IN_CN)
								{
									dlgPlace.setText(reverseGeoCodeResult.getAddress()); //设置简要位置信息
								}
								//判断位置文本框是否被填充，如果未被填充则填充
								if(TextUtils.isEmpty(dlgPlace.getText()))
								{
									dlgPlace.setText(earthquake.getPlace()); //设置简要位置信息
								}
							}
						});
						coder.reverseGeoCode(option);

						//显示地震信息
						TextView magnitude = (TextView) view.findViewById(R.id.magnitude_item);
						TextView date = (TextView) view.findViewById(R.id.date_item);
						TextView time = (TextView) view.findViewById(R.id.time_item);

						magnitude.setText(getFormattedMagnitude(maxMagnitude));
						date.setText(getFormattedDate(earthquake.getTime()));
						time.setText(getFormattedTime(earthquake.getTime()));

						//设置总视图的点击事件，打开网址
						view.setOnClickListener(new View.OnClickListener()
						{
							@Override public void onClick(View v)
							{
								Uri earthquakeUri = Uri.parse(earthquake.getUri());

								Intent queryIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);
								startActivity(queryIntent);
							}
						});

						builder.setView(view);
						builder.create().show();
					}
				});
				//需要一个返回值
				return false;
			}
		});
	}

	private Earthquake getTargetEarthquake(LatLng location)
	{
		for(int i = 0; i < earthquakes.size(); i++)
		{
			Earthquake temp = earthquakes.get(i);
			if(temp.getLatitude() == location.latitude && temp.getLongitude() == location.longitude)
				return temp;
		}
		return null;
	}

	private String getFormattedDate(long time)
	{
		/**
		 * 根据 Unix 时间戳返回一个 Date 对象
		 * 从 Date 对象返回格式化的日期字符串（形如 2018-3-9, 或者 2017-11-13）。
		 * 因为时区的问题，可能不同的人运行程序显示的时间可能不同
		 * */
		//大写的D表示一年中的一天，小写的d表示一个月中的一天
		SimpleDateFormat formatter = new SimpleDateFormat("YYYY-M-d");
		return formatter.format(new Date(time));
	}

	private String getFormattedTime(long time)
	{
		/**
		 * 根据 Unix 时间戳返回一个 Date 对象
		 * 从 Date 对象返回格式化的时间字符串（形如 "4:30, 或者 11:28"）。
		 */
		//小写h、m表示12小时进制，大写H、M代表24小时进制
		SimpleDateFormat formatter = new SimpleDateFormat("H:MM");
		return formatter.format(new Date(time));
	}

	//设置标记
	private void setMapOverlay(LatLng position, double magnitude)
	{
		int id = getMarkerId(magnitude);
		BitmapDescriptor markerIcon = BitmapDescriptorFactory.fromResource(id);

		OverlayOptions options = new MarkerOptions().position(position).icon(markerIcon);

		baiduMap.addOverlay(options);
	}

	//获得标记的id
	private int getMarkerId(double magnitude)
	{
		//获得表示地震等级大小的不同颜色的标记：颜色为：绿、蓝、黄、橙、红
		//一个地区的标记选择按照发生的最大地震等级来确定
		int id; //数字后面加个d，表示双精度浮点数，加个f表示浮点数
		//0 到 3 等级的地震颜色：弱地震
		if(magnitude > 0d && magnitude <= 3d)
			id = R.drawable.ic_earthquake_marker_48dp_green;
			//3 到 4.5 等级的地震颜色：有感地震
		else if(magnitude > 3d && magnitude <= 4.5d)
			id = R.drawable.ic_earthquake_marker_48dp_blue;
			//4.5 到 6 等级的地震颜色：中强度地震
		else if(magnitude > 4.5d && magnitude <= 6d)
			id = R.drawable.ic_earthquake_marker_48dp_yellow;
			//6 到 8 等级的地震颜色：强地震
		else if(magnitude > 6d && magnitude <= 8d)
			id = R.drawable.ic_earthquake_marker_48dp_orange;
			//8 等级及以上的地震颜色：巨大地震
		else
			id = R.drawable.ic_earthquake_marker_48dp_red;

		return id;
	}

	private String getFormattedMagnitude(double magnitude)
	{
		/**
		 * 获得格式化后的地震等级，小数点后保留一位：
		 * DecimalFormat 类主要靠 # 和 0 两种占位符号来指定数字长度。
		 * 0 表示如果位数不足则以 0 填充，# 表示只要有可能就把数字拉上这个位置。
		 * */
		DecimalFormat formatter = new DecimalFormat("0.0");
		return formatter.format(magnitude);
	}

	//判断地震地点是否在国内，如果在，就解析出详细地名，如果不在，就显示粗略地名
	/**
	 * 纬度N, 经度M
	 * 纬度一度相差111千米，经度一度相差111*COS(N)
	 * */

	/*//将地震信息按照地点分类
	private ArrayList<Location> getClassifiedPlace()
	{
		ArrayList<Earthquake> tempList = (ArrayList<Earthquake>)earthquakes.clone();
		ArrayList<Location> classifiedList = new ArrayList<>();

		//列表每次删除一个内容后后面的内容都会向前移动，故此处i不加
		for(int i = 0; i < tempList.size();)
		{
			if(classifiedList.isEmpty()) //如果列表为空，则直接添加
			{
				Earthquake earthquake = tempList.get(i);
				Location location = new Location(earthquake.getLatitude(), earthquake
						.getLongitude(), i);
				classifiedList.add(location);
				//将地震信息从列表中移除，保证不重复
				tempList.remove(earthquake);
			}
			else
			{
				boolean isLoop = true; //是否进行循环的控制变量
				Earthquake earthquake = tempList.get(i);
				//遍历找出earthquake的位置是否已经存在
				for(int j = 0; j < classifiedList.size() && isLoop; j++)
				{
					Location temp = classifiedList.get(j);
					if(temp.equals(earthquake)) //如果地点已经存在
					{
						temp.add(i);
						isLoop = false; //找到地点就不再循环了
					}
					else
					{
						Location location = new Location(earthquake.getLatitude(), earthquake
								.getLongitude(), i);
						classifiedList.add(location);
					}
					//将地震信息从列表中移除，保证不重复
					tempList.remove(earthquake);
				}
			}
		}

		return classifiedList;
	}*/

	/*//获得最大的地震等级
	private double getMaxMagnitude(Location place)
	{
		//将地震等级数据整理到一个数组中
		double[] magnitudes = new double[place.getMarker().size()];
		for(int i = 0; i < place.getMarker().size(); i++)
		{
			magnitudes[i] = earthquakes.get(place.getMarker().get(i)).getMagnitude();
		}
		//如果相应列表长度为1，则直接返回，无需比较
		if(magnitudes.length == 1)
			return magnitudes[0];
		else
		{
			//采用快速排序取得最大数
			QuickSort quickSort = new QuickSort(magnitudes);
			magnitudes = quickSort.getDoubleArray();

			return magnitudes[magnitudes.length - 1]; //最大数列在最后面
		}
	}*/

	/*//将地震信息添加到地图上
	private void initInfo()
	{
		String[] place;

		//将标记添加到地图上
		ArrayList<Location> classifiedPlace = getClassifiedPlace();
		for(int i = 0; i < classifiedPlace.size(); i++)
		{
			final Location location = classifiedPlace.get(i);
			double maxMagnitude = getMaxMagnitude(location);
			final LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
			setMapOverlay(point, maxMagnitude);

			//显示消息信息框
			Button button = new Button(this); //显示InfoWindow的button
			button.setText("Max：" + getFormattedMagnitude(maxMagnitude));
			button.setOnClickListener(new View.OnClickListener()
			{
				@Override public void onClick(View v)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(BDMActivity.this);
					View view = getLayoutInflater().inflate(R.layout.bdm_info, null);

					*//** 填充国内地点信息，需要用到反地理编码 *//*
					final TextView dlgPlace = (TextView)view.findViewById(R.id.dlg_place);
					coder = GeoCoder.newInstance();
					//设置反地理编码选项
					ReverseGeoCodeOption option = new ReverseGeoCodeOption().location(point);
					//发起反地理编码，先设置监听
					coder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener()
					{
						//地理编码
						@Override public void onGetGeoCodeResult(GeoCodeResult geoCodeResult)
						{

						}

						//反地理编码
						@Override public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult)
						{
							//没有收到
							if(reverseGeoCodeResult == null || reverseGeoCodeResult.error !=
									SearchResult.ERRORNO.NO_ERROR)
							{
								System.out.println("位置查询错误");
								return;
							}
							dlgPlace.setText(reverseGeoCodeResult.getAddress()); //设置简要位置信息
						}
					});
					coder.reverseGeoCode(option);
					//显示地震信息
					ListView list = (ListView)view.findViewById(R.id.message_list);
					ArrayList<Earthquake> temp = new ArrayList<>();
					for(int i = 0; i < location.getMarker().size(); i++)
					{
						Earthquake item = earthquakes.get(location.getMarker().get(i));
						temp.add(item);
					}
					final BDMInfoAdapter adapter = new BDMInfoAdapter(BDMActivity.this, temp);
					list.setAdapter(adapter);
					//设置列表项的点击事件
					list.setOnItemClickListener(new AdapterView.OnItemClickListener()
					{
						@Override
						public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
						{
							Earthquake currentEarthquake = adapter.getItem(i);
							Uri earthquakeUri = Uri.parse(currentEarthquake.getUri());

							Intent queryIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);
							startActivity(queryIntent);
						}
					});

					builder.setView(view);
					builder.create().show();
				}
			});
			//第三个参数，y轴偏移量
			InfoWindow infoWindow = new InfoWindow(button, point, -150);
			baiduMap.showInfoWindow(infoWindow);
		}
	}*/
}
