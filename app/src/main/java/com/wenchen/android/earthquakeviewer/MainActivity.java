package com.wenchen.android.earthquakeviewer;

import android.Manifest;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
	implements LoaderManager.LoaderCallbacks<ArrayList<Earthquake>>
{
	//private final int MAP_REQUEST_CODE = 102;
	Intent intent; //与SettingActivity通信的Intent

	//loader 的id
	private static final int LOADER_ID = 0;

	//URL 表头用 https，否则会出错
	private static final String USGS_REQUEST_URL = "https://earthquake.usgs.gov" +
			"/fdsnws/event/1/query?format=geojson";

	//实际的查询地址
	private static Uri uri;

	private ListView earthquakesList;
	LinearLayout messageLayout;

	private EarthquakesListAdapter adapter;

	private TextView emptyTextView;

	//将地震信息以地图形式查看时，需要的数据
	private ArrayList<Earthquake> data;

	//检查相应权限是否授权成全：使用百度地图功能必须的危险权限(运行时授予)
	ArrayList<String> permissions = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//将Activity加入到栈中，方便后续退出
		ExitApplication.getInstance().addActivity(this);

		earthquakesList = (ListView)findViewById(R.id.list);

		data = new ArrayList<>();

		initPermissions();

		//设置适配器
		adapter = new EarthquakesListAdapter(this, new ArrayList<Earthquake>());
		earthquakesList.setAdapter(adapter);

		//设置列表项的点击事件
		earthquakesList.setOnItemClickListener(new AdapterView.OnItemClickListener()
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

		messageLayout = (LinearLayout)findViewById(R.id.message_layout);

		//设置信息提示隐藏，有数据时才显示
		messageLayout.setVisibility(View.GONE);

		//设置空视图
		emptyTextView = (TextView)findViewById(R.id.empty_view);
		earthquakesList.setEmptyView(emptyTextView);

		//初始化异步线程加载器
		//三个参数的意义，第二个为null，表示没有 Bundle 对象，第三个为监听器
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	//当从设置界面返回的时候刷新
	@Override
	protected void onResume()
	{
		super.onResume();

		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}

	@Override public void onBackPressed()
	{
		new AlertDialog.Builder(this)
				.setMessage("确定退出？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						ExitApplication.getInstance().exit(MainActivity.this);
						moveTaskToBack(true); //true表示不管是否在根任务，直接退出程序
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener()
				{
					@Override public void onClick(DialogInterface dialog, int which)
					{
						dialog.dismiss(); //隐藏掉对话框
					}
				}).create().show();
		//super.onBackPressed();
	}

	//加载菜单
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.setting_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	//点击菜单后的操作
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case R.id.menu_setting_item:
			openSettingPage();
		    return true;
		default:
			return super.onOptionsItemSelected(item);
		case R.id.menu_see_as_map:
			seeAsMap(); //地震地点可以使用地图的形式查看，地图使用的是百度地图API
			return true;
		}
	}

	//打开设置页面
	private void openSettingPage()
	{
		if(permissions.isEmpty())
		{
			intent = new Intent(MainActivity.this, SettingActivity.class);

			startActivity(intent);
		}
	}

	//重载回调方法，创建一个异步线程的加载器
	@Override
	public Loader<ArrayList<Earthquake>> onCreateLoader(int i, Bundle bundle)
	{
		/**
		 * 创建时首先加载设置，该设置可以被存储
		 * */
		SharedPreferences settingSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());

		SettingPreference settings = new SettingPreference(settingSharedPreferences);

		Uri baseUri = Uri.parse(USGS_REQUEST_URL);

		//在 baseUri 的基础上构建 builder
		Uri.Builder builder = baseUri.buildUpon();

		builder.appendQueryParameter("orderby", settings.getSortMethod());
		builder.appendQueryParameter("starttime", settings.getStartDate());
		builder.appendQueryParameter("endtime", settings.getEndDate());
		if(settings.getLatitude() != 91f && settings.getLongitude() != 181f)
		{
			//以经纬度为圆点，半径为 50km范围内的地震
			builder.appendQueryParameter("latitude", String.valueOf(settings.getLatitude()));
			builder.appendQueryParameter("longitude", String.valueOf(settings.getLongitude()));
			builder.appendQueryParameter("maxradiuskm", "50");
		}

		builder.appendQueryParameter("minmagnitude", settings.getMinMagnitude());
		builder.appendQueryParameter("limit", settings.getLimit());

		uri = builder.build();

		return new EarthquakeLoader(this, uri.toString());
	}

	//重载回调方法，加载数据完成后执行的操作
	@Override
	public void onLoadFinished(Loader<ArrayList<Earthquake>> loader, ArrayList<Earthquake> earthquakes)
	{
		//加载完成后隐藏进度条
		View loadingIndicator = findViewById(R.id.loading_indicator);
		loadingIndicator.setVisibility(View.GONE);

		//此时设置空视图的信息可以防止在加载网络数据时一闪而过空视图
		emptyTextView.setText(R.string.no_earthquakes);

		//获取到了地震内容，显示信息提示
		if(earthquakes != null && !earthquakes.isEmpty())
			messageLayout.setVisibility(View.VISIBLE);

		//重新加载地震信息
		adapter.clear();
		if(earthquakes != null && !earthquakes.isEmpty())
		{
			adapter.addAll(earthquakes);
			data = earthquakes; //将数据赋予data
		}
	}

	//重载回调方法，重置加载器，由其获得的数据不可用
	@Override
	public void onLoaderReset(Loader<ArrayList<Earthquake>> loader)
	{
		adapter.clear();
	}

	//返回查询地址URI
	public static Uri getUri()
	{
		return uri;
	}

	//初始化动态权限信息
	private void initPermissions()
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) //API >= 23时检查
		{
			//检查权限
			if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
				permissions.add(Manifest.permission.READ_PHONE_STATE);
			if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
				permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
			if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
				permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
			if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
				permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
			if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
				permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

			//权限申请列表不为空，则要申请
			if(!permissions.isEmpty() || permissions != null)
			{
				if(permissions.contains(Manifest.permission.READ_PHONE_STATE))
				{
					//显示授权提示框
					if(shouldShowRequestPermissionRationale(Manifest.permission
							.READ_PHONE_STATE))
					{

					}
					else
					{
						ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
								.READ_PHONE_STATE}, 0);
					}

				}
				if(permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) ||
						permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION))
				{
					//显示授权提示框
					if(shouldShowRequestPermissionRationale(Manifest.permission
							.ACCESS_FINE_LOCATION))
					{

					}
					else
					{
						ActivityCompat.requestPermissions(this, new String[]{Manifest
								.permission.ACCESS_FINE_LOCATION, Manifest.permission
								.ACCESS_COARSE_LOCATION}, 1);
					}
				}
				if(permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
						permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE))
				{
					if(shouldShowRequestPermissionRationale(Manifest.permission
							.WRITE_EXTERNAL_STORAGE))
					{

					}
					else
					{
						ActivityCompat.requestPermissions(this, new String[]{Manifest
								.permission.READ_EXTERNAL_STORAGE, Manifest.permission
								.WRITE_EXTERNAL_STORAGE}, 2);
					}
				}
			}
		}
	}

	//当用户点击地图菜单项时，以地图的形式呈现
	private void seeAsMap()
	{
		if(data != null && !data.isEmpty())
		{
			Intent intent = new Intent(MainActivity.this, BDMActivity.class);
			intent.putParcelableArrayListExtra("data", data);

			//startActivityForResult(intent, MAP_REQUEST_CODE);
			startActivity(intent); //不需要数据交互
		}
		else
		{
			Toast.makeText(this, "地震列表为空，无法在地图上显示", Toast.LENGTH_LONG).show();
		}
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[]
			permissionsAry, @NonNull int[] grantResults)
	{
		//在权限申请回调函数中，去掉相应的需要申请的权限
		if(permissions != null && !permissions.isEmpty())
		{
			switch(requestCode)
			{
			case 0:
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					//如果包含此权限，就去除
					if(permissions.contains(Manifest.permission.READ_PHONE_STATE))
						permissions.remove(Manifest.permission.READ_PHONE_STATE);
				}
				break;
			case 1:
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					if(permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION))
						permissions.remove(Manifest.permission.ACCESS_FINE_LOCATION);
					if(permissions.contains(Manifest.permission.ACCESS_COARSE_LOCATION))
						permissions.remove(Manifest.permission.ACCESS_COARSE_LOCATION);
				}
				break;
			case 2:
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{
					if(permissions.contains(Manifest.permission.READ_EXTERNAL_STORAGE))
						permissions.remove(Manifest.permission.READ_EXTERNAL_STORAGE);
					if(permissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE))
						permissions.remove(Manifest.permission.WRITE_EXTERNAL_STORAGE);
				}
				break;
			}
		}
		//如果申请权限列表为空，说明全部需要的权限已经分配
		if(permissions.isEmpty())
		{
			Intent placeIntent = new Intent(MainActivity.this, SettingActivity.class);
			startActivityForResult(placeIntent, 0);
		}
	}
}
