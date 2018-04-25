package com.wenchen.android.earthquakeviewer;

//设置参数的视图页面
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener
{
	//0 代表按时间排序，1 代表按震级排序
	//0 代表升序，1 代表降序

	//排序 Spinner 与 说明按钮
	Spinner sortStyleSpinner;
	Button tipsButton;
	ArrayAdapter<String> arrayAdapter;

	//展示样式按钮 和 说明 ImageView
	RadioGroup displayStyleRadioGroup;
	ImageView arrowUpDownImageView;

	//开始与结束日期选择组件
	TextView startDateTextView;
	Button startDateButton;
	TextView endDateTextView;
	Button endDateButton;

	//显示经纬度的组建
	TextView latitudeTextView;
	TextView longitudeTextView;
	Button placeButton;

	//最小震级和数量限制组件
	EditText minMagnitudeEditText;
	EditText limitEditText;

	//确认与取消按钮
	Button cancelButton;
	Button confirmButton;

	//显示 Fragment 的组件
	LinearLayout extraContainer;

	//存储设置
	SettingPreference settings;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

		//返回上级
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		//绑定 Spinner
		sortStyleSpinner = (Spinner)findViewById(R.id.sort_style_spinner);
		tipsButton = (Button)findViewById(R.id.tips_button);

		//绑定 RadioGroup 与相应的 ImageView
		displayStyleRadioGroup = (RadioGroup)findViewById(R.id.display_style);
		arrowUpDownImageView = (ImageView)findViewById(R.id.arrow_up_down_image_view);

		//绑定开始与结束日期视图
		startDateTextView = (TextView)findViewById(R.id.start_date_text_view);
		startDateButton = (Button)findViewById(R.id.start_date_button);
		endDateTextView = (TextView)findViewById(R.id.end_date_text_view);
		endDateButton = (Button)findViewById(R.id.end_date_button);

		//绑定经纬度显示相关视图
		latitudeTextView = (TextView)findViewById(R.id.latitude_text_view);
		longitudeTextView = (TextView)findViewById(R.id.longitude_text_view);
		placeButton = (Button) findViewById(R.id.place_button);

		//绑定震级与文件限制视图
		minMagnitudeEditText = (EditText)findViewById(R.id.magnitude_edit_text);
		limitEditText = (EditText)findViewById(R.id.number_edit_text);

		//绑定功能按钮
		cancelButton = (Button)findViewById(R.id.cancel_button);
		confirmButton = (Button)findViewById(R.id.confirm_button);

		//绑定 Fragment 动态项
		extraContainer = (LinearLayout)findViewById(R.id.extra_container);

		//初始化
		init(savedInstanceState);
	}

	//初始化顺序建议不做更改，否则会出现莫名其妙的错误
	private void init(Bundle savedInstanceState)
	{
		//初始化下拉选项
		initialFragment(savedInstanceState);

		//初始化 Spinner
		initialSpinner();

		//初始化 RadioButton
		initialRadioButton();

		//初始化 EditText 的焦点
		initialEditText();

		//初始化按钮的监听器
		tipsButton.setOnClickListener(this);
		startDateButton.setOnClickListener(this);
		endDateButton.setOnClickListener(this);
		placeButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		confirmButton.setOnClickListener(this);

		//初始化首次加载设置界面时的数据
		initialData();
	}

	//加载时初始化设定
	private void initialData()
	{
		/**
		 * 创建时首先加载设置，该设置可以被存储
		 * */
		settings = new SettingPreference(PreferenceManager.getDefaultSharedPreferences
				(getApplicationContext()));

		//预设排序样式
		sortStyleSpinner.setSelection(settings.getSortStyle());

		//预设显示样式
		setDisplayStyle();

		//预设开始与结束时间
		startDateTextView.setText(settings.getStartDate());
		endDateTextView.setText(settings.getEndDate());

		//预设地理位置
		if(settings.getLatitude() != 91 && settings.getLongitude() != 181)
		{
			latitudeTextView.setText(String.valueOf(settings.getLatitude()));
			longitudeTextView.setText(String.valueOf(settings.getLongitude()));
		}

		//预设最小震级和数量限制
		minMagnitudeEditText.setText(settings.getMinMagnitude());
		limitEditText.setText(settings.getLimit());
	}

	//初始化 Fragment 组件，显示下拉选项
	private void initialFragment(Bundle savedInstanceState)
	{
		if(savedInstanceState != null)
			return;
		DownFragment downFragment = new DownFragment();
		getSupportFragmentManager().beginTransaction()
				.add(R.id.extra_container, downFragment)
				.commit();
	}

	//初始化 Spinner
	private void initialSpinner()
	{
		//绑定适配器
		arrayAdapter = new ArrayAdapter<>(this, R.layout.personal_spinner,
				getResources().getStringArray(R.array.sort_style_spinner_array));
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		sortStyleSpinner.setAdapter(arrayAdapter);

		sortStyleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
		{
			@Override public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
			{

			}

			@Override public void onNothingSelected(AdapterView<?> adapterView)
			{

			}
		});
	}

	private void initialRadioButton()
	{
		displayStyleRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
		{
			@Override
			public void onCheckedChanged(RadioGroup radioGroup, int position)
			{
				int index; //设置位置
				//设置显式样式
				if(position == R.id.display_ascend_radio_button)
				{
					displayStyleRadioGroup.check(position);
					arrowUpDownImageView.setImageResource(R.drawable.ic_arrow_upward_grey_500_24dp);
					index = 0;
				}
				//显示样式
				else
				{
					displayStyleRadioGroup.check(position);
					arrowUpDownImageView.setImageResource(R.drawable.ic_arrow_downward_grey_500_24dp);
					index = 1;
				}
				settings.setDisplayStyle(index);
			}
		});
	}

	private void initialEditText()
	{
		//设置正确的隐藏键盘
		final InputMethodManager manager = (InputMethodManager)getSystemService(Context
				.INPUT_METHOD_SERVICE);

		//正确设置光标
		minMagnitudeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View view, boolean hasFocus)
			{
				if(hasFocus)
					minMagnitudeEditText.setSelection(minMagnitudeEditText.getText().length() - 1);
				else
					manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		});

		limitEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
		{
			@Override
			public void onFocusChange(View view, boolean hasFocus)
			{
				if(hasFocus)
					minMagnitudeEditText.setSelection(limitEditText.getText().length() - 1);
				else
					manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}
		});

		//记录用户更改
		minMagnitudeEditText.addTextChangedListener(new TextWatcher()
		{
			@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{

			}

			@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{

			}

			@Override public void afterTextChanged(Editable editable)
			{

			}
		});

		limitEditText.addTextChangedListener(new TextWatcher()
		{
			@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{

			}

			@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
			{

			}

			@Override public void afterTextChanged(Editable editable)
			{

			}
		});
	}

	private void setDisplayStyle()
	{
		if(settings.getDisplayStyle() == 0)
		{
			displayStyleRadioGroup.check(R.id.display_ascend_radio_button);
			arrowUpDownImageView.setImageResource(R.drawable.ic_arrow_upward_grey_500_24dp);
		}
		else
		{
			displayStyleRadioGroup.check(R.id.display_descend_radio_button);
			arrowUpDownImageView.setImageResource(R.drawable.ic_arrow_downward_grey_500_24dp);
		}
	}

	//点击按钮进行响应
	@Override
	public void onClick(View view)
	{
		AlertDialog.Builder builder;

		switch(view.getId())
		{
		case R.id.tips_button:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("说明");
			builder.setMessage("考虑实际，最小等级限制在 8 级以下，大于 8 级统一设定为 8 级。\n" +
					"\n受地震数据源的限制，地震数量限制在 20000 以下且数量必须大于 0，超出的统一限制在此范围。\n");
			builder.setCancelable(false);
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int i)
				{
					dialog.dismiss();
				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();

			break;
		case R.id.start_date_button:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("请选择起始日期");
			View startDateDialog = LayoutInflater.from(this).inflate(R.layout
					.date_picker_dialog, null);

			//设置日期
			final DatePicker startDatePicker = (DatePicker) startDateDialog.findViewById(R.id
					.datePicker);

			int tempStartYear = settings.getStartYear();
			int tempStartMonth = settings.getStartMonth();
			int tempStartDay = settings.getStartDay();

			startDatePicker.updateDate(tempStartYear, tempStartMonth, tempStartDay);

			builder.setView(startDateDialog);
			builder.setCancelable(false);
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int i)
				{
					dialog.dismiss();
				}
			});

			builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int i)
				{
					int tempStartYear = startDatePicker.getYear();
					int tempStartMonth = startDatePicker.getMonth();
					int tempStartDay = startDatePicker.getDayOfMonth();

					settings.setStartYear(tempStartYear);
					settings.setStartMonth(tempStartMonth);
					settings.setStartDay(tempStartDay);

					((TextView)findViewById(R.id.start_date_text_view)).setText(settings.getStartDate());
				}
			});
			builder.create().show();

			break;
		case R.id.end_date_button:
			builder = new AlertDialog.Builder(this);
			builder.setTitle("请选择终止日期");
			View endDateDialog = LayoutInflater.from(this).inflate(R.layout
					.date_picker_dialog, null);

			//设置日期
			final DatePicker endDatePicker = (DatePicker) endDateDialog.findViewById(R.id
					.datePicker);

			int tempEndYear = settings.getEndYear();
			int tempEndMonth = settings.getEndMonth();
			int tempEndDay = settings.getEndDay();

			endDatePicker.updateDate(tempEndYear, tempEndMonth, tempEndDay);

			builder.setView(endDateDialog);
			builder.setCancelable(false);
			builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int i)
				{
					dialog.dismiss();
				}
			});

			builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int i)
				{
					int tempEndYear = endDatePicker.getYear();
					int tempEndMonth = endDatePicker.getMonth();
					int tempEndDay = endDatePicker.getDayOfMonth();

					settings.setEndYear(tempEndYear);
					settings.setEndMonth(tempEndMonth);
					settings.setEndDay(tempEndDay);

					((TextView)findViewById(R.id.end_date_text_view)).setText(
							settings.getEndDate());

					dialog.dismiss();
				}
			});

			builder.create().show();

			break;
		case R.id.place_button:
			Intent placeIntent = new Intent(SettingActivity.this, MapsActivity.class);
			startActivityForResult(placeIntent, 0);

			break;
		case R.id.cancel_button:
			//撤销
			initialData();
			/*//离开设置界面
			SettingActivity.this.finish();*/

			Toast.makeText(SettingActivity.this, "取消更改：还原设置成功", Toast.LENGTH_SHORT).show();

			break;
		case R.id.confirm_button:
			//进行设置
			//设置排序样式
			settings.setSortStyle(sortStyleSpinner.getSelectedItemPosition());
			//设置最小震级
			String magnitudeString = minMagnitudeEditText.getText().toString();
			double magnitude = Double.parseDouble(magnitudeString);
			//设置限定条件，大于0级，并且小于8级
			if(magnitude > 8)
				magnitude = 8.0;
			if(magnitude < 0)
				magnitude = 0.1;
			settings.setMinMagnitude(String.valueOf(magnitude));
			//设置数量限制
			String limitString = limitEditText.getText().toString();
			int limit = Integer.parseInt(limitString);
			//设置文件限定条件，大于0，并且小于20000
			if(limit > 20000)
				limit = 20000;
			if(limit < 0)
				limit = 1;

			settings.setLimit(String.valueOf(limit));

			settings.setPreferences();

			//SettingActivity.this.finish();

			Toast.makeText(SettingActivity.this, "应用更改：更新设置成功", Toast.LENGTH_SHORT).show();

			break;
		case R.id.down_image:
			break;
		default:
			break;
		}
	}

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == 0)
		{
			if(resultCode == RESULT_OK)
			{
				double[] place = data.getDoubleArrayExtra("position");

				latitudeTextView.setText(String.valueOf(place[0]));
				longitudeTextView.setText(String.valueOf(place[1]));
			}
		}
	}
}
