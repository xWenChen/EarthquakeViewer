package com.wenchen.android.earthquakeviewer;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by WenChen on 2018/3/22.
 */

public class EarthquakesListAdapter extends ArrayAdapter<Earthquake>
{
	//显示地震信息的视图
	TextView magnitudeTextView;
	TextView primaryPlaceTextView;
	TextView placeOffsetTextView;
	TextView dateTextView;
	TextView timeTextView;

	//地震信息项的当前类
	Earthquake currentEarthquake;

	/**用于分离位置*/
	private static final String PLACE_SEPARATOR = " of ";

	public EarthquakesListAdapter(Context context, List<Earthquake> earthquakes)
	{
		super(context, 0, earthquakes);
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
	{
		View listItemView = convertView;

		//适配器中的视图为空就创建一个
		if(listItemView == null)
		{
			listItemView = LayoutInflater.from(getContext()).inflate(R.layout
					.earthquake_list_item, parent, false);
		}

		currentEarthquake = getItem(position);

		/*显示地震信息*/
		double magnitude = currentEarthquake.getMagnitude();
		String originalPlace = currentEarthquake.getPlace();
		long time = currentEarthquake.getTime();

		//获取分隔后的地震位置
		String[] detachedPlace = getDetachedPlace(originalPlace);

		//绑定视图
		magnitudeTextView = (TextView)listItemView.findViewById(R.id.magnitude);
		primaryPlaceTextView = (TextView)listItemView.findViewById(R.id.primary_place);
		placeOffsetTextView = (TextView)listItemView.findViewById(R.id.place_offset);
		dateTextView = (TextView)listItemView.findViewById(R.id.date);
		timeTextView = (TextView)listItemView.findViewById(R.id.time);

		//设置地震等级的背景色
		setMagnitudeColor();

		//设置视图信息
		magnitudeTextView.setText(getFormattedMagnitude(magnitude)); //设置等级
		placeOffsetTextView.setText(detachedPlace[0]); //设置偏移位置
		primaryPlaceTextView.setText(detachedPlace[1]); //设置主要位置
		dateTextView.setText(getFormattedDate(time)); //设置日期
		timeTextView.setText(getFormattedTime(time)); //设置时间

		return listItemView;
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

	private String[] getDetachedPlace(String originalPlace)
	{
		/**
		 * 获取分隔后的地震位置，如果有偏移，则显示，没有则显示 Near the
		 * */

		String place[] = new String[2];
		if(originalPlace.contains(PLACE_SEPARATOR))
		{
			String[] part = originalPlace.split(PLACE_SEPARATOR);
			place[0] = part[0] + PLACE_SEPARATOR;
			place[1] = part[1];
		}
		else
		{
			place[0] = getContext().getString(R.string.near_the);
			place[1] = originalPlace;
		}

		return place;
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

	private void setMagnitudeColor()
	{
		GradientDrawable magnitudeCircle = (GradientDrawable)magnitudeTextView.getBackground();
		int magnitudeColor = getMagnitudeColorFromResource(currentEarthquake.getMagnitude());
		magnitudeCircle.setColor(magnitudeColor);
	}

	private int getMagnitudeColorFromResource(double magnitude)
	{
		int magnitudeColorResourceId;

		//0 级地震不考虑
		if(magnitude > 0 && magnitude <= 3)
			magnitudeColorResourceId = R.color.magnitude1;
		else if(magnitude > 3 && magnitude <= 4.5)
			magnitudeColorResourceId = R.color.magnitude2;
		else if(magnitude > 4.5 && magnitude <= 6)
			magnitudeColorResourceId = R.color.magnitude3;
		else if(magnitude > 6 && magnitude <= 8)
			magnitudeColorResourceId = R.color.magnitude4;
		else if(magnitude > 8)
			magnitudeColorResourceId = R.color.magnitude5;
		else //地震等级为负，按照0级算
			magnitudeColorResourceId = R.color.magnitude1;


		//getColor(int id) 被弃用了
		//查询网站：stackoverflow
		return ContextCompat.getColor(getContext(), magnitudeColorResourceId);
	}
}
