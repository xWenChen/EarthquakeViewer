package com.wenchen.android.earthquakeviewer;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * Created by WenChen on 2018/4/2.
 */

//上拉的 fragment
public class UpFragment extends Fragment
{
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.extra_fragment, container, false);

		TextView sourceDataTextView = (TextView)v.findViewById(R.id.source_data_text_view);
		TextView linkDataTextView = (TextView)v.findViewById(R.id.link_data_text_view);
		ImageButton up = (ImageButton)v.findViewById(R.id.up_image);

		//设置文本
		linkDataTextView.setText(MainActivity.getUri().toString());

		//加上下划线
		sourceDataTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		linkDataTextView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

		sourceDataTextView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent usgsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + "www" +
						".usgs.gov"));
				startActivity(usgsIntent);
			}
		});

		linkDataTextView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				Intent linkIntent = new Intent(Intent.ACTION_VIEW, MainActivity.getUri());
				startActivity(linkIntent);
			}
		});

		//不将操作入栈，否则会产生导航返回键和菜单栏返回键的差异
		up.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				DownFragment downFragment = new DownFragment();
				getFragmentManager().beginTransaction()
						.replace(R.id.extra_container, downFragment)
						.commit();
			}
		});

		return v;
	}
}
