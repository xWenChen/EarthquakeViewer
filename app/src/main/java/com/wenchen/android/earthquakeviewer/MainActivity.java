package com.wenchen.android.earthquakeviewer;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
	implements LoaderManager.LoaderCallbacks<List<Earthquake>>
{
	//loader 的id
	private static final int LOADER_ID = 0;

	//URL 表头用 https，否则会出错
	private static final String USGS_REQUEST_URL = "https://earthquake.usgs.gov" +
			"/fdsnws/event/1/query?format=geojson";

	//实际的查询地址
	private static Uri uri;

	//排序方式
	private static final int SORT_TIME = 0; //按时间排序
	private static final int SORT_MAGNITUDE = 1; //按震级排序

	//显示样式
	private static final int ASCEND = 0; //升序
	private static final int DESCEND = 1; //降序

	private ListView earthquakesList;
	LinearLayout messageLayout;

	private EarthquakesListAdapter adapter;

	private TextView emptyTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		earthquakesList = (ListView)findViewById(R.id.list);

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

	/*//当从设置界面返回的时候刷新
	@Override
	protected void onResume()
	{
		super.onResume();

		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}*/

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
			seeAsMap(); //如果在中国境内，地震地点可以使用地图的形式查看，地图使用的是百度地图API
			return true;
		}
	}

	//打开设置页面
	private void openSettingPage()
	{
		Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);

		startActivity(settingIntent);
	}

	//重载回调方法，创建一个异步线程的加载器
	@Override
	public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle)
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
		if(settings.getLatitude() != 91 && settings.getLongitude() != 181)
		{
			//半径为 50km
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
	public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakes)
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
		}
	}

	//重载回调方法，重置加载器，由其获得的数据不可用
	@Override
	public void onLoaderReset(Loader<List<Earthquake>> loader)
	{
		adapter.clear();
	}

	//返回查询地址URI
	public static Uri getUri()
	{
		return uri;
	}

	//当用户点击地图菜单项时，以地图的形式呈现
	private void seeAsMap()
	{
	}
}
