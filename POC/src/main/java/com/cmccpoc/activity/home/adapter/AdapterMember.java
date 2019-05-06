package com.cmccpoc.activity.home.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.cmccpoc.config.Config;
import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeContactPresence;
import com.airtalkee.sdk.entity.AirContact;
import com.airtalkee.sdk.entity.AirContactTiny;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;

/**
 * 成员用户 适配器
 * @author Yao
 */
@SuppressLint("UseSparseArrays")
public class AdapterMember extends BaseAdapter
{
	private Context context = null;
	private AirSession session = null;
	private List<AirContact> memberList = null;
	private HashMap<Integer, AirContact> isSelected;
	private View vMemberBottom = null;
	private boolean allowCheck = false;
	private boolean allowRole = false;
	private View layoutBtns;
	private CheckedCallBack checkedCallBack;

	public interface CheckedCallBack
	{ 
		public void onChecked(boolean isChecked);
	}

	public AdapterMember(Context _context, View v, View v2, boolean allowCheck, boolean allowRole, CheckedCallBack checkedCallBack)
	{
		this.checkedCallBack = checkedCallBack;
		context = _context;
		vMemberBottom = v;
		this.layoutBtns = v2;
		this.allowCheck = allowCheck;
		this.allowRole = allowRole;
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
	 * 刷新member列表与选中状态
	 * @param _session 会话Entity
	 * @param _memberList 
	 */
	public void notifyMember(AirSession _session, List<AirContact> _memberList)
	{
		session = _session;
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
		return (memberList != null) ? memberList.size() : 0;
	}

	@Override
	public Object getItem(int position)
	{
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
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = null;
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(R.layout.listitem_member_new, null);
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

				if (session.getType() == AirSession.TYPE_CHANNEL)
				{
					if (TextUtils.equals(AirtalkeeAccount.getInstance().getUserId(), member.getIpocId()))
					{
						holder.ivSPresence.setImageResource(R.drawable.user_state_owner);
						holder.tvName.setTextColor(context.getResources().getColor(R.color.focuse_item_color));
					}
					else
					{
						if (member.getStateInChat() == AirContact.IN_CHAT_STATE_ONLINE)
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
									holder.ivSPresence.setImageResource(R.drawable.user_state_offline);
									holder.tvName.setTextColor(context.getResources().getColor(R.color.color_hint_dark));
									break;
							}
						}
					}
	
					if (allowRole)
					{
						switch (member.getCusertype())
						{
							case AirContact.CURSETYPE_CREATEER:
								//holder.ivRole.setVisibility(View.VISIBLE);
								//holder.ivRole.setImageResource(R.drawable.media_role_creater);
								break;
							case AirContact.CURSETYPE_MANAGER:
								//holder.ivRole.setVisibility(View.VISIBLE);
								//holder.ivRole.setImageResource(R.drawable.media_role2);
								break;
							case AirContact.CURSETYPE_USER:
								//holder.ivRole.setVisibility(View.VISIBLE);
								//holder.ivRole.setImageResource(R.drawable.media_role1);
								break;
							case AirContact.CURSETYPE_LISTEN_ONLY:
								//holder.ivRole.setVisibility(View.VISIBLE);
								//holder.ivRole.setImageResource(R.drawable.media_role_listen);
								break;
							default:
								//holder.ivRole.setVisibility(View.GONE);
								break;
						}
					}
					else
					{
						//holder.ivRole.setVisibility(View.GONE);
					}
	
					if (!allowCheck)
					{
						//holder.ivRole.setVisibility(View.GONE);
					}
	
					String myIpocId = (AirtalkeeAccount.getInstance() != null) ? AirtalkeeAccount.getInstance().getUserId() : "";
					if (myIpocId.equals(member.getIpocId()))
					{
						// holder.tvName.setText(member.getDisplayName());
						holder.checkBox.setClickable(false);
						holder.checkBox.setVisibility(View.INVISIBLE);
					}
					else
					{
						//holder.checkBox.setVisibility(View.VISIBLE);
						//holder.checkBox.setClickable(true);
					}
	
					holder.checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
					{
						public void onCheckedChanged(CompoundButton arg0, boolean isCheck)
						{
							putSelected(position, member, isCheck);
	
							if (isSelected.size() > 0)
							{
								if (vMemberBottom != null)
									vMemberBottom.setVisibility(View.VISIBLE);
								if (layoutBtns != null)
									layoutBtns.setVisibility(View.GONE);
								if (checkedCallBack != null)
									checkedCallBack.onChecked(true);
							}
							else
							{
								if (vMemberBottom != null)
									vMemberBottom.setVisibility(View.GONE);
								if (layoutBtns != null)
									layoutBtns.setVisibility(View.VISIBLE);
								if (checkedCallBack != null)
									checkedCallBack.onChecked(false);
							}
						}
	
					});
					holder.checkBox.setChecked(!(isSelected != null && isSelected.get(position) == null));
					if (!allowCheck)
					{
						holder.checkBox.setVisibility(View.GONE);
					}
				}
				else if (session.getType() == AirSession.TYPE_DIALOG)
				{
					holder.checkBox.setVisibility(View.GONE);
					//holder.ivRole.setVisibility(View.GONE);
					if (TextUtils.equals(AirtalkeeAccount.getInstance().getUserId(), member.getIpocId()))
					{
						holder.ivSPresence.setImageResource(R.drawable.user_state_owner);
						holder.tvName.setTextColor(context.getResources().getColor(R.color.focuse_item_color));
					}
					else 
					{
						if (member.getStateInChat() == AirContact.IN_CHAT_STATE_OFFLINE)
						{
							holder.ivSPresence.setImageResource(R.drawable.user_state_offline);
							holder.tvName.setTextColor(context.getResources().getColor(R.color.color_hint_dark));
						}
						else
						{
							holder.ivSPresence.setImageResource(R.drawable.user_state_online);
							holder.tvName.setTextColor(context.getResources().getColor(R.color.focuse_item_color));
						}
					}
				} 
			}
		}
		catch (Exception e)
		{}
		return convertView;
	}

	class ViewHolder
	{
		CheckBox checkBox;
		TextView tvName;
		ImageView ivSPresence;
		ImageView ivRole;
	}
}
