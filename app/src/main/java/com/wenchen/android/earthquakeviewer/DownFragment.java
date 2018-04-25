package com.wenchen.android.earthquakeviewer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Created by WenChen on 2018/3/26.
 */

//下拉的fragment
public class DownFragment extends Fragment
{
	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.base_fragment, container, false);
		ImageButton down = (ImageButton)v.findViewById(R.id.down_image);
		down.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				//直接用容器 id 可以生效
				UpFragment upFragment = new UpFragment();
				getFragmentManager().beginTransaction()
						.replace(R.id.extra_container, upFragment)
						.commit();
			}
		});

		return v;
	}
}
