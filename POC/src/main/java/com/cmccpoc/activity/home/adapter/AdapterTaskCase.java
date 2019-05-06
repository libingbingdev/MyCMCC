package com.cmccpoc.activity.home.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cmccpoc.R;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirTaskCaseManager;
import com.cmccpoc.entity.AirTaskCase;

import java.util.List;

/**
 * 成员用户 适配器
 * @author Yao
 */
@SuppressLint("UseSparseArrays")
public class AdapterTaskCase extends BaseAdapter
{
	private Context context = null;
	private List<AirTaskCase> taskList = null;
	private boolean allowManage = false;


	public AdapterTaskCase(Context _context, boolean _allowManage)
	{
		context = _context;
		allowManage = _allowManage;
		if (!Config.funcTask)
			AirTaskCaseManager.getInstance().LoadTasks();
		taskList = AirTaskCaseManager.getInstance().getTaskCaseList();
	}

	@Override
	public int getCount()
	{
		return (taskList != null) ? taskList.size() : 0;
	}

	@Override
	public Object getItem(int position)
	{
		AirTaskCase task = null;
		try
		{
			task = (taskList != null) ? taskList.get(position) : null;
		}
		catch (Exception e)
		{}
		return task;
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_task_case, null);
			holder = new ViewHolder();
			holder.tvName = (TextView) convertView.findViewById(R.id.talk_task_case_name);
			holder.ivDel = (ImageView) convertView.findViewById(R.id.talk_task_case_del);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		try
		{
			final AirTaskCase task = (AirTaskCase) getItem(position);
			if (task != null)
			{
				holder.tvName.setText(TextUtils.isEmpty(task.getCaseName()) ? task.getCaseCode() : task.getCaseName());
				if (allowManage)
				{
					holder.ivDel.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							AlertDialog dialog = new AlertDialog(context, "确认", "是否确认要删除？", "取消", "确认", new AlertDialog.DialogListener()
							{
								@Override
								public void onClickOk(int id, boolean isChecked)
								{
								}

								@Override
								public void onClickOk(int id, Object obj)
								{
									AirTaskCaseManager.getInstance().TaskDelete(task.getTaskId());
									notifyDataSetChanged();
								}

								@Override
								public void onClickCancel(int id)
								{
								}
							}, -1);
							dialog.show();
						}
					});
				}
				else
					holder.ivDel.setVisibility(View.GONE);
			}
		}
		catch (Exception e)
		{}
		return convertView;
	}

	class ViewHolder
	{
		TextView tvName;
		ImageView ivDel;
	}
}
