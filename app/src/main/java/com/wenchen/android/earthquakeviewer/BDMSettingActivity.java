package com.wenchen.android.earthquakeviewer;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

//位于设置界面的百度地图(BaiduMap), 包含一个地图两个按钮

public class BDMSettingActivity extends AppCompatActivity
{
	Intent intent; //和设置界面(SettingActivity)之间通信的Intent

	MapView mapView;
	BaiduMap baiduMap;
	FloatingActionButton cancel, confirm;
	public LocationClient locationClient = null; //使用定位服务需要用到的类
	private MyLocationListener myListener = new MyLocationListener();
	LocationClientOption clientOption;

	double latitude, longitude; //经纬度

	@Override protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext()); //必须放在setContent函数前面
		setContentView(R.layout.activity_bdmsetting);

		intent = getIntent();

		mapView = (MapView)findViewById(R.id.bdm_view);
		confirm = (FloatingActionButton) findViewById(R.id.bdm_confirm);
		cancel = (FloatingActionButton) findViewById(R.id.bdm_cancel);

		//初始化地图设置
		initBaiduMap();

		//获取位置配置第一步，设置监听器
		locationClient = new LocationClient(getApplicationContext());
		locationClient.registerLocationListener(myListener); //绑定监听器

		//第二步，初始化定位配置
		initLocation();

		//第三步：开启定位
		locationClient.start();

		initClickEvent();

		mapView.showScaleControl(false); //不显示默认比例尺控件
		//普通地图的缩放等级是4-21
		mapView.showZoomControls(false); //不显示默认缩放控件
	}

	//初始化按钮的点击事件
	private void initClickEvent()
	{
		confirm.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				//将结果放入intent中，方便被回传结果函数(onActivityResult)捕捉
				intent.putExtra("latitude", latitude);
				intent.putExtra("longitude", longitude);
				setResult(RESULT_OK); //设置正常结果

				BDMSettingActivity.this.finish(); //退出
			}
		});

		cancel.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				setResult(RESULT_CANCELED); //设置取消结果

				BDMSettingActivity.this.finish(); //不设置坐标退出
			}
		});

		baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener()
		{
			@Override public void onMapClick(LatLng latLng)
			{
				//记录经纬度
				latitude = latLng.latitude;
				longitude = latLng.longitude;

				setMapOverlay(latLng); //更新标记
			}

			@Override public boolean onMapPoiClick(MapPoi mapPoi)
			{
				return false;
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		//在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mapView.onDestroy();
	}
	@Override
	protected void onResume() {
		super.onResume();
		//在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mapView.onResume();
	}
	@Override
	protected void onPause() {
		super.onPause();
		//在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mapView.onPause();
	}

	private void initBaiduMap()
	{
		//地图设置
		baiduMap = mapView.getMap();
		baiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL); //设置地图类型为普通的地图
		baiduMap.setTrafficEnabled(false); //不显示交通图
		baiduMap.setBaiduHeatMapEnabled(false); //不显示百度热力图
		baiduMap.setBuildingsEnabled(false); //不显示建筑物
	}

	//定位设置
	private void initLocation()
	{
		clientOption = new LocationClientOption();

		//设置下列选项，才可以获取当前位置
		clientOption.setIsNeedAddress(true);
		clientOption.setOpenGps(true);
		clientOption.setLocationMode(LocationClientOption.LocationMode.Battery_Saving); //设置定位模式
		//海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标
		clientOption.setScanSpan(0); //设置定位请求时间间隔，设置为0表示仅定位一次

		/*
		clientOption.setCoorType("bd09ll");
		//可选，设置返回经纬度坐标类型，默认gcj02
		//gcj02：国测局坐标；
		//bd09ll：百度经纬度坐标；
		//bd09：百度墨卡托坐标；
		//海外地区定位，无需设置坐标类型，统一返回wgs84类型坐标
		*/

		/*
		clientOption.setScanSpan(1000);
		//可选，设置发起定位请求的间隔，int类型，单位ms
		//如果设置为0，则代表单次定位，即仅定位一次，默认为0
		//如果设置非0，需设置1000ms以上才有效
		*/

		/*
		clientOption.setOpenGps(true);
		//可选，设置是否使用gps，默认false
		//使用高精度和仅用设备两种定位模式的，参数必须设置为true
		*/

		/*
		clientOption.setLocationNotify(true);
		//可选，设置是否当GPS有效时按照1S/1次频率输出GPS结果，默认false
		*/


		clientOption.setIgnoreKillProcess(false);
		//可选，定位SDK内部是一个service，并放到了独立进程。
		//设置是否在stop的时候杀死这个进程，默认（建议）不杀死，即setIgnoreKillProcess(true)

		//可选，设置是否收集Crash信息，默认收集，即参数为false
		clientOption.SetIgnoreCacheException(true);

		/*
		clientOption.setWifiCacheTimeOut(5*60*1000);
		//可选，7.2版本新增能力
		//如果设置了该接口，首次启动定位时，会先判断当前WiFi是否超出有效期，若超出有效期，会先重新扫描WiFi，然后定位
		*/

		/*
		clientOption.setEnableSimulateGps(false);
		//可选，设置是否需要过滤GPS仿真结果，默认需要，即参数为false
		*/

		//locationClient为第二步初始化过的LocationClient对象
		//需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
		//更多LocationClientOption的配置，请参照类参考中LocationClientOption类的详细说明
		locationClient.setLocOption(clientOption);
	}

	//显示位置监听接口
	public class MyLocationListener extends BDAbstractLocationListener
	{
		//此处的BDLocation为定位结果信息类，通过它的各种get方法可获取定位相关的全部结果

		@Override public void onReceiveLocation(BDLocation bdLocation)
		{
			if(bdLocation != null)
			{
				latitude = bdLocation.getLatitude();
				longitude = bdLocation.getLongitude();

				LatLng temp = new LatLng(latitude, longitude);
				baiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(temp));
				//设置标记
				setMapOverlay(temp);
			}

			/*float radius = bdLocation.getRadius(); //获取定位精度
			//获取经纬度坐标类型，以LocationClientOption中设置过的坐标类型为准
			String coorType = bdLocation.getCoorType();
			//获取定位类型、定位错误返回码，具体信息可参照类参考中BDLocation类中的说明
			int errorCode = bdLocation.getLocType();*/
		}
	}

	//设置标记
	private void setMapOverlay(LatLng temp)
	{
		baiduMap.clear();

		BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable
				.ic_marker_32dp_red);
		OverlayOptions options = new MarkerOptions().position(temp).icon(bitmapDescriptor);
		baiduMap.addOverlay(options);
	}
}

