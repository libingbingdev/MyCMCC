package com.cmccpoc.activity.home.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactTiny;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirSessionControl;

/**
 * 全部成员适配器
 * @author Yao
 */
@SuppressLint("UseSparseArrays")
public class AdapterMemberAll extends BaseAdapter 
{
	private Context context = null;
	private List<AirContact> memberList = new ArrayList<AirContact>();
	private HashMap<Integer, AirContact> isSelected = new HashMap<Integer, AirContact>();
	private CheckedCallBack checkedCallBack;

	private List <AirContact>mArrayList=new ArrayList<AirContact>();
	private AirSession session;
	public interface CheckedCallBack
	{
		public void onChecked(boolean isChecked);
	}

	public AdapterMemberAll(Context _context, CheckedCallBack checkedCallBack)
	{
		this.checkedCallBack = checkedCallBack;
		context = _context;
		session= AirSessionControl.getInstance().getCurrentSession();
		if(session.getType()== AirSession.TYPE_DIALOG){
			mArrayList=session.getMemberAll();
		}
	}

	/**
	 * 设置被选中的成员列表
	 * @param key 键值
	 * @param value 成员Entity
	 * @param isCheck 是否选中
	 */
	private void putSelected(Integer key, AirContact value, boolean isCheck)
	{
		if (isCheck)
		{
			isSelected.put(key, value);
		}
		else if (isSelected.size() > 0)
		{
			isSelected.remove(key);
		}
	}

	/**
	 * 获取被选中的成员列表
	 */
	public List<AirContact> getSelectedMemberList()
	{
		List<AirContact> selectList = new ArrayList<AirContact>();
		if (isSelected != null)
		{
			@SuppressWarnings("rawtypes")
			Iterator iterable = (Iterator) isSelected.values().iterator();
			while (iterable.hasNext())
			{
				selectList.add((AirContact) iterable.next());
			}
		}
		return selectList;
	}

	/**
	 * 获取被选中的成员列表
	 */
	public List<AirContactTiny> getSelectedMemberListTiny()
	{
		List<AirContactTiny> selectList = new ArrayList<AirContactTiny>();
		if (isSelected != null)
		{
			@SuppressWarnings("rawtypes")
			Iterator iterable = (Iterator) isSelected.values().iterator();
			while (iterable.hasNext())
			{
				AirContact contact = (AirContact) iterable.next();
				AirContactTiny contactTiny = new AirContactTiny();
				contactTiny.setIpocId(contact.getIpocId());
				contactTiny.setDisplayName(contact.getDisplayName());
				selectList.add(contactTiny);
			}
		}
		return selectList;
	}

	/**
	 * 刷新全部成员列表与选中状态
	 * @param _memberList 
	 */
	public void notifyMember(List<AirContact> _memberList)
	{
		memberList = _memberList;
		resetCheckBox();
	}

	/**
	 * 重置选中状态
	 */
	public void resetCheckBox()
	{
		isSelected = new HashMap<Integer, AirContact>();
		isSelected.clear();
		notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		// TODO Auto-generated method stub
		return (memberList != null) ? memberList.size() : 0;
	}

	@Override
	public Object getItem(int position)
	{
		// TODO Auto-generated method stub
		AirContact ct = null;
		try
		{
			ct = (memberList != null) ? memberList.get(position) : null;
		}
		catch (Exception e)
		{}
		return ct;
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		ViewHolder holder = null;
		// Log.e(AdapterMember.class, "AdapterMember getView");
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_member, null);
			holder = new ViewHolder();
			holder.checkBox = (CheckBox) convertView.findViewById(R.id.talk_cb_group_member);
			holder.tvName = (TextView) convertView.findViewById(R.id.talk_tv_group_member);
			holder.ivSPresence = (ImageView) convertView.findViewById(R.id.talk_iv_presence);
			holder.ivRole = (ImageView) convertView.findViewById(R.id.talk_iv_group_role);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();
		}
		try
		{
			final AirContact member = (AirContact) getItem(position);
			if (member != null)
			{
				if (Config.funcUserIdShow)
					holder.tvName.setText(member.getDisplayName() + "(" + member.getIpocId() + ")");
				else
					holder.tvName.setText(member.getDisplayName());
				if (TextUtils.equals(AirtalkeeAccount.getInstance().getUserId(), member.getIpocId()))
				{
					holder.ivSPresence.setImageResource(R.drawable.user_state_online);
					holder.tvName.setTextColor(context.getResources().getColor(R.color.focuse_item_color));
				}
				else
				{
					int state = AirtalkeeContactPresence.getInstance().getContactStateById(member.getIpocId());
					switch (state)
					{
						case AirContact.CONTACT_STATE_NONE:
							holder.ivSPresence.setImageResource(R.drawable.user_state_offline);
							holder.tvName.setTextColor(context.getResources().getColor(R.color.color_hint_dark));
							break;
						case AirContact.CONTACT_STATE_ONLINE:
						case AirContact.CONTACT_STATE_ONLINE_BG:
							holder.ivSPresence.setImageResource(R.drawable.user_state_online);
							holder.tvName.setTextColor(context.getResources().getColor(R.color.focuse_item_color));
							break;
					}
				}
				holder.ivRole.setVisibility(View.GONE);
				String myIpocId = (AirtalkeeAccount.getInstance() != null) ? AirtalkeeAccount.getInstance().getUserId() : "";
				if (myIpocId.equals(member.getIpocId()))
				{
					//holder.tvName.setText(member.getDisplayName());
					holder.checkBox.setChecked(false);
					holder.checkBox.setEnabled(true);
					holder.checkBox.setVisibility(View.INVISIBLE);
				}else if(session.getType()== AirSession.TYPE_DIALOG && Contains(member)){
					holder.checkBox.setVisibility(View.VISIBLE);
					holder.checkBox.setChecked(true);
					holder.checkBox.setEnabled(false);
					holder.tvName.setTextColor(context.getResources().getColor(R.color.color_hint_dark));
				}else
				{
					holder.checkBox.setVisibility(View.VISIBLE);
					holder.checkBox.setClickable(true);
					holder.checkBox.setChecked(false);
					holder.checkBox.setEnabled(true);
					holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
					{
						public void onCheckedChanged(CompoundButton arg0, boolean isCheck)
						{
							putSelected(position, member, isCheck);
							if (checkedCallBack != null)
								checkedCallBack.onChecked(isSelected.size() > 0);
						}

					});
					//holder.checkBox.setChecked(!(isSelected != null && isSelected.get(position) == null));
				}
	

			}
		}
		catch (Exception e)
		{}
		return convertView;
	}

	private boolean Contains(AirContact airContact) {
		for (AirContact contact : mArrayList) {
			if (airContact.getIpocId().equals(contact.getIpocId())) {
				return true;
			}
		}
		return false;
	}

	class ViewHolder
	{
		CheckBox checkBox;
		TextView tvName;
		ImageView ivSPresence;
		ImageView ivRole;
	}
}
