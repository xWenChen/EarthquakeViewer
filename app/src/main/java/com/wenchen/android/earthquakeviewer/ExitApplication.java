package com.wenchen.android.earthquakeviewer;

/**
 * Created by WenChen on 2018/5/18.
 */

//退出程序用到的栈类
//采用单实例模式

import android.app.Application;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

public class ExitApplication extends Application
{

	private List<AppCompatActivity> list = new ArrayList<>();

	private static ExitApplication ea;

	private ExitApplication() {

	}

	public static ExitApplication getInstance() {
		if (null == ea) {
			ea = new ExitApplication();
		}
		return ea;
	}

	public void addActivity(AppCompatActivity activity) {
		list.add(activity);
	}

	public void exit(Context context) {
		for (AppCompatActivity activity : list) {
			activity.finish();
		}
		System.exit(0);
	}
}
