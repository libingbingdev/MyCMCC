package com.cmccpoc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.adapter.AdapterReport;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirReportManager;
import com.cmccpoc.entity.AirReport;
import com.cmccpoc.listener.OnMmiReportListener;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Toast;

/**
 * 更多：上报记录
 * 可以删除，编辑
 * @author Yao
 */
public class MenuReportActivity extends ActivityBase implements OnClickListener, OnMmiReportListener, OnItemClickListener, OnCheckedChangeListener
{
	public AdapterReport adapterReport;
	private ListView lvReportList;
	private View talk_report_list_panel, talk_report_empty;
	private ImageView ivRight;
	private RelativeLayout reportDelPanel;
	private LinearLayout btReportDel;
	private TextView tvReportTip;
	private CheckBox cbSelectAll;
	private static MenuReportActivity mInstance;
	
	private boolean isEditing = false;

	public static MenuReportActivity getInstance()
	{
		return mInstance;
	}
	@Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_report);
		// AirReportManager.getInstance().loadReports();
		doInitView();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		AirReportManager.getInstance().setReportListener(null);
		// adapterReport.showIcons(false);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		refreshListOrEmpty();
		AirReportManager.getInstance().setReportListener(this);
		if (adapterReport != null)
			adapterReport.notifyDataSetChanged();
	}

	/**
	 * 初始化绑定控件Id
	 */
	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_report_upload_record);

		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_setting, this));
		ivRightLay.setOnClickListener(this);

		talk_report_list_panel = findViewById(R.id.talk_report_list_panel);
		talk_report_empty = findViewById(R.id.talk_report_empty);
		lvReportList = (ListView) findViewById(R.id.talk_report_list);
		adapterReport = new AdapterReport(this, lvReportList);

		lvReportList.setAdapter(adapterReport);
		adapterReport.notifyDataSetChanged();
		lvReportList.setOnItemClickListener(this);

		reportDelPanel = (RelativeLayout) findViewById(R.id.rl_report_panel);
		btReportDel = (LinearLayout) findViewById(R.id.bt_report_del);
		btReportDel.setOnClickListener(this);
		tvReportTip = (TextView) findViewById(R.id.tv_report_select_count);
		tvReportTip.setText("已选择0条记录");
		btReportDel.setClickable(false);
		cbSelectAll = (CheckBox) findViewById(R.id.cb_report_selectall);
		cbSelectAll.setOnCheckedChangeListener(this);
		adapterReport.notifyDataSetChanged();
		mInstance = this;
	}

	/**
	 * 刷新上报记录列表
	 */
	public void refreshListOrEmpty()
	{
		if (AirReportManager.getInstance().getReports().size() == 0)
		{
			talk_report_list_panel.setVisibility(View.GONE);
			talk_report_empty.setVisibility(View.VISIBLE);
		}
		else
		{
			talk_report_list_panel.setVisibility(View.VISIBLE);
			talk_report_empty.setVisibility(View.GONE);
			adapterReport.notifyDataSetChanged();
			if (adapterReport.getSelectReportCount() > 0)
			{
				btReportDel.setClickable(true);
				btReportDel.setBackgroundResource(R.drawable.bg_report_red);
			}
			else
			{
				btReportDel.setClickable(false);
				btReportDel.setBackgroundResource(R.drawable.bg_report_gray);
			}
			cbSelectAll.setOnCheckedChangeListener(null);
			cbSelectAll.setChecked(adapterReport.getSelectReportCount() == adapterReport.getCount());
			cbSelectAll.setOnCheckedChangeListener(this);
			tvReportTip.setText("已选择" + adapterReport.getSelectReportCount() + "条记录");
		}
	}

	/**
	 * 计算上报资源大小
	 * @param size 资源文件大小
	 */
	public static String sizeMKB(int size)
	{
		String str = "";
		if (size >= 1024 && size < 1024 * 1024)
		{
			str = (size / 1024) + "K";
		}
		else if (size >= 1024 * 1024)
		{
			float sizeF = (float) size / (float) 1024 / (float) 1024;
			str = String.format("%.2f", sizeF) + "M";
//			str = (size / 1024 / 1024) + ".";
//			if (size % 1024 >= 900)
//			{
//				str += "9M";
//			}
//			else
//			{
//				str += (size % 1024) / 100 + "M";
//			}
		}
		else
		{
			str = size + "B";
		}
		return str;
	}
	
	public void refreshReport()
	{
		adapterReport.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
			case R.id.talk_menu_right_button:
			{
				if (lvReportList.getCount() > 0)
				{
					isEditing = !isEditing;
					if (isEditing)
					{
						ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_clean, this));
						reportDelPanel.setVisibility(View.VISIBLE);
						adapterReport.setEditing(true);
						lvReportList.setClickable(false);
						tvReportTip.setText("已选择0条记录");
						btReportDel.setBackgroundResource(R.drawable.bg_report_gray);
						// rootPanel.setBackgroundResource(R.attr.theme_sider_title_bg_report);
					}
					else
					{
						ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_setting, this));
						reportDelPanel.setVisibility(View.GONE);
						adapterReport.setEditing(false);
						lvReportList.setClickable(true);
						cbSelectAll.setChecked(false);
						// rootPanel.setBackgroundResource(R.attr.theme_sider_title_bg);
					}
					refreshListOrEmpty();
				}
				break;
			}
			case R.id.bt_report_del:
			{
				if (isEditing)
				{
					AirReportManager.getInstance().ReportsDelete(adapterReport.getReportMap());
					adapterReport.deleteVideoPic();
					tvReportTip.setText("已选择0条记录");
					btReportDel.setClickable(false);
					btReportDel.setBackgroundResource(R.drawable.bg_report_gray);
					adapterReport.removedSelected();
					adapterReport.checkedAll(false);
					if(Toast.isDebug) Toast.makeText1(this, "已删除", Toast.LENGTH_LONG).show();
					ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_setting, this));
					reportDelPanel.setVisibility(View.GONE);
					adapterReport.setEditing(false);
					lvReportList.setClickable(true);
					cbSelectAll.setChecked(false);
					isEditing = false;
				}
				break;
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		AirReport report = (AirReport) adapterReport.getItem(position);
		if (report != null)
		{
			if (isEditing)
			{
				if (report.getState() != AirReport.STATE_UPLOADING)
				{
					CheckBox cb = (CheckBox) view.findViewById(R.id.cb_report);
					adapterReport.setSelected(report, !cb.isChecked());
					refreshListOrEmpty();
				}
				else
					if(Toast.isDebug) Toast.makeText1(this, "正在上传，禁止选择", Toast.LENGTH_LONG).show();
			}
			else
			{
				Intent it = new Intent(this, MenuReportViewActivity.class);
				it.putExtra("code", report.getCode());
				startActivity(it);
			}
		}
	}


	@Override
	public void onMmiReportResourceListRefresh()
	{
		refreshListOrEmpty();
		adapterReport.notifyDataSetChanged();
	}

	@Override
	public void onMmiReportDel()
	{
		if (adapterReport != null)
			refreshListOrEmpty();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		adapterReport.checkedAll(isChecked);
		refreshListOrEmpty();
		if (isChecked && adapterReport.getSelectReportCount() != adapterReport.getCount())
			if(Toast.isDebug) Toast.makeText1(this, "已选择非正在上传的记录", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onMmiReportProgress(int progress)
	{
		
	}
}
