package com.wenchen.android.earthquakeviewer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by WenChen on 2018/5/24.
 */

//在百度地图里显示地震信息的适配器

public class BDMInfoAdapter extends ArrayAdapter<Earthquake>
{
	private Earthquake current;
	private Context context;

	public BDMInfoAdapter(@NonNull Context context, ArrayList<Earthquake> earthquakes)
	{
		super(context, 0, earthquakes);
		this.context = context;
	}

	@NonNull @Override public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
	{
		View view = convertView;
		if(view == null)
		{
			view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
		}
		TextView magnitude = (TextView)view.findViewById(R.id.magnitude_item);
		TextView date = (TextView)view.findViewById(R.id.date_item);
		TextView time = (TextView)view.findViewById(R.id.time_item);
		current = getItem(position);
		magnitude.setText(getFormattedMagnitude(current.getMagnitude()));
		date.setText(getFormattedDate(current.getTime()));
		time.setText(getFormattedTime(current.getTime()));

		return view;
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
}
