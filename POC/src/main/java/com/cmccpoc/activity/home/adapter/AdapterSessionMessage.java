package com.cmccpoc.activity.home.adapter;

import android.app.Activity;
import android.content.Context;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.controller.LocationShareController;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.adapter.AdapterBase.OnImageLoadCompletedListener;
import com.cmccpoc.util.Language;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Util;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 会话消息适配器
 * @author Yao
 */
public class AdapterSessionMessage extends AdapterBase implements OnImageLoadCompletedListener
{
	AirSession currentSession = null;
	final int TYPE_ME = 0;
	final int TYPE_YOU = 1;
	final int TYPE_MAX = 2;
	private Context mContext = null;
	private boolean isChinese = false;
	private OnClickListener onClicklistener;
	private OnLongClickListener onLongClickListener;

	private class ImageSize
	{
		public int width = 0;
		public int height = 0;
	}

	private HashMap<String, ImageSize> mImageSizeMap = new HashMap<String, ImageSize>();

	public AdapterSessionMessage(Context context, OnClickListener listener, OnLongClickListener longClickListener)
	{
		this.mContext = context;
		this.onClicklistener = listener;
		this.onLongClickListener = longClickListener;
	}

	public void setSession(AirSession session)
	{
		currentSession = session;
	}

	@Override
	public void notifyDataSetChanged()
	{
		super.notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		int size = 0;
		if (currentSession != null && currentSession.getMessages() != null)
		{
			size = currentSession.getMessages().size();
		}
		return size;
	}

	@Override
	public Object getItem(int position)
	{
		if (currentSession != null)
		{
			try
			{
				return currentSession.getMessages().get(position);
			}
			catch (IndexOutOfBoundsException e)
			{
			}
		}
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		AirMessage iMessage = (AirMessage) getItem(position);
		if (iMessage != null)
		{
			ViewHolder holder;
			String ipocIdFrom = iMessage.getIpocidFrom();
			String ipocId = (AccountController.getUserInfo() != null) ? AccountController.getUserInfo().getIpocId() : "";
			if (ipocIdFrom.equals(ipocId))
			{
				convertView = buildMessageItemWithMe(position, convertView, iMessage);
			}
			else
			{
				convertView = buildMessageItemWithOther(position, convertView, iMessage);
			}
			try
			{
				if (convertView != null)
				{
					holder = (ViewHolder) convertView.getTag();
					if (holder != null)
					{
						String msg_body = "";
						if (!TextUtils.isEmpty(iMessage.getBody()))
							msg_body = iMessage.getBody().replaceAll("\r", "");
						Spannable spannable = Util.buildPlainMessageSpannable(mContext, msg_body.getBytes());
						switch (iMessage.getType())
						{
							case AirMessage.TYPE_SYSTEM:
							{
								holder.userName.setVisibility(View.INVISIBLE);
								msg_body = "[" + iMessage.getTime() + "] ";
								msg_body += iMessage.getBody().replaceAll("\r", "");
								if (!TextUtils.equals(iMessage.getIpocidFrom(), AirtalkeeUserInfo.getInstance().getUserInfo().getIpocId()))
								{
									if (!msg_body.contains(iMessage.getInameFrom()))
									{
										msg_body += " (" + iMessage.getInameFrom() + ")";
									}
								}
								if(msg_body.contains("JOINED")) 
								{
									String time = msg_body.substring(0,10);
									String joinedId = msg_body.substring(10,msg_body.indexOf(")") + 1);
									String joinId = null;
									if(msg_body.contains("BY"))
									{
										joinId = msg_body.substring(msg_body.lastIndexOf("("),msg_body.lastIndexOf(")") + 1);
									}
									if(joinId == null)
										msg_body = time + joinedId + " 被加入";
									else
										msg_body = time + joinedId + " 被 " + joinId + " 加入";
								}
								spannable = Util.buildPlainMessageSpannable(mContext, msg_body.getBytes());
								holder.bodyLayout.setVisibility(View.GONE);
								holder.tvSystem.setVisibility(View.VISIBLE);
								holder.tvSystem.setText(spannable);
								Log.i(AdapterSessionMessage.class, "AdapterSessionMessage TYPE_SYSTEM msg=" + holder.tvSystem.getText());
								break;
							}
							default:
							{
								holder.userName.setVisibility(View.VISIBLE);
								holder.bodyLayout.setVisibility(View.VISIBLE);
								holder.tvSystem.setVisibility(View.GONE);
								if(iMessage.getType() == AirMessage.TYPE_SESSION_VIDEO)
								{
									holder.videoBody.setText(spannable);
									holder.videoPic.setVisibility(View.VISIBLE);
								}
								else if (iMessage.getType() == AirMessage.TYPE_VIDEO_SHARE_REAL)
								{
									holder.videoBody.setText(mContext.getString(R.string.talk_video_forward_real));
									holder.videoPic.setVisibility(View.VISIBLE);
								}
								else if (iMessage.getType() == AirMessage.TYPE_LOCATION)
								{
									JSONTokener jsonParser = new JSONTokener(spannable.toString()); 
									try
									{
										JSONObject location = (JSONObject) jsonParser.nextValue();
										holder.tvLocationTitle.setText(location.getString("name"));
										holder.tvLocationAddr.setText(location.getString("address"));
									}
									catch (Exception e)
									{
										if (iMessage.getSecretType() > 0)
										{
											holder.tvLocationTitle.setText("位置标题已被加密，无法查看");
											holder.tvLocationAddr.setText("位置地址信息已被加密，无法查看");
										}
									}
								}
								else if (iMessage.getType() == AirMessage.TYPE_VIDEO)
								{
									
								}
								else if (iMessage.getType() == AirMessage.TYPE_VIDEO_SHARE_STORE)
								{

								}
								else
								{
									holder.body.setText(spannable);
									holder.videoPic.setVisibility(View.GONE);
								}
								break;
							}
						}
						boolean showDate = needShowDateline(position);
						if (showDate)
						{
							holder.date.setVisibility(View.VISIBLE);
							holder.date.setText(Language.convertDate(iMessage.getDate(), iMessage.getTime(), isChinese));
						}
						else
						{
							holder.date.setVisibility(View.GONE);
						}
					}
				}
			}
			catch (Exception e)
			{}
		}
		return convertView;
	}

	/**
	 * 构建“我”发送的消息记录
	 * @param position 位置
	 * @param convertView view
	 * @param iMessage 消息Entity
	 * @return View
	 */
	private View buildMessageItemWithMe(int position, View convertView, AirMessage iMessage)
	{
		try
		{
			int type = iMessage.getType();
			int state = iMessage.getState();
			ViewHolder1 holder1 = null;
			if (convertView == null || !(convertView.getTag() instanceof ViewHolder1))
			{
				// Log.i( "ME convertView == null");
				convertView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.listitem_conversation_message_me, null);
				holder1 = new ViewHolder1();
				holder1.ViewHolderInit(convertView);
				holder1.userName = (TextView) convertView.findViewById(R.id.user_name);
				holder1.report_icon = (ImageView) convertView.findViewById(R.id.report_icon);
				convertView.setTag(holder1);
			}
			else if (convertView.getTag() instanceof ViewHolder1)
			{
				holder1 = (ViewHolder1) convertView.getTag();
			}
			if (holder1 == null)
				return convertView;
			holder1.pic.setVisibility(View.GONE);
			holder1.body.setVisibility(View.VISIBLE);
			holder1.pro.setVisibility(View.GONE);
			holder1.tvAndIvLayout.setVisibility(View.VISIBLE);
			holder1.record_layout.setVisibility(View.GONE);
			holder1.videoSessionLayout.setVisibility(View.GONE);
			holder1.videoLayout.setVisibility(View.GONE);
			holder1.locationLayout.setVisibility(View.GONE);
			holder1.locationShareView.setVisibility(View.GONE);
			holder1.bodyContent.setTag(iMessage);
			holder1.bodyContent.setOnClickListener(onClicklistener);
			holder1.bodyContent.setOnLongClickListener(onLongClickListener);
			if (type == AirMessage.TYPE_RECORD)// Record Message
			{
				holder1.videoSessionLayout.setVisibility(View.GONE);
				holder1.tvAndIvLayout.setVisibility(View.GONE);
				holder1.locationLayout.setVisibility(View.GONE);
				holder1.record_layout.setVisibility(View.VISIBLE);
				holder1.record_default.setVisibility(View.VISIBLE);
				holder1.record_time.setVisibility(View.VISIBLE);
				holder1.videoPic.setVisibility(View.GONE);
				holder1.record_layout.setTag(iMessage.getMessageCode());
				if (!iMessage.isRecordPlaying())
				{
					holder1.record_time.setText("" + iMessage.getImageLength() + "''");
					holder1.record_default.setImageResource(ThemeUtil.getResourceId(R.attr.theme_msg_audio_play, mContext));
				}
				else
				{
					holder1.record_time.setText("" + iMessage.getRecordTimer() + "''");
					holder1.record_default.setImageResource(ThemeUtil.getResourceId(R.attr.theme_msg_audio_stop, mContext));
				}
			}
			else
			{
				if (type == AirMessage.TYPE_PICTURE)
				{
					holder1.pic.setVisibility(View.VISIBLE);
					holder1.body.setVisibility(View.GONE);
					holder1.record_layout.setVisibility(View.GONE);
					holder1.locationLayout.setVisibility(View.GONE);
					holder1.videoLayout.setVisibility(View.GONE);
					holder1.videoPic.setVisibility(View.GONE);
					displayImageByUrl("file://" + IOoperate.FOLDER_PATH + IOoperate.IMAGES_PATH + "/" +iMessage.getMessageCode(), holder1.pic, iMessage.getMessageCode(), this);
				}
				else if (type == AirMessage.TYPE_SESSION_VIDEO)
				{
					holder1.pic.setVisibility(View.GONE);
					holder1.body.setVisibility(View.GONE);
					holder1.tvAndIvLayout.setVisibility(View.GONE);
					holder1.record_layout.setVisibility(View.GONE);
					holder1.locationLayout.setVisibility(View.GONE);
					holder1.videoSessionLayout.setVisibility(View.VISIBLE);
					holder1.videoLayout.setVisibility(View.GONE);
					holder1.videoPic.setVisibility(View.VISIBLE);
				}
				else if(type == AirMessage.TYPE_LOCATION)
				{
					holder1.videoLayout.setVisibility(View.GONE);
					holder1.tvAndIvLayout.setVisibility(View.GONE);
					holder1.record_layout.setVisibility(View.GONE);
					holder1.locationLayout.setVisibility(View.VISIBLE);
					
					holder1.pic.setVisibility(View.GONE);
					holder1.body.setVisibility(View.GONE);
					holder1.videoPic.setVisibility(View.GONE);
					holder1.locationBottom.getBackground().setAlpha(200);
				}
				else if (type == AirMessage.TYPE_LOCATION_SHARE_STATE)
				{
					holder1.videoLayout.setVisibility(View.GONE);
					holder1.tvAndIvLayout.setVisibility(View.GONE);
					holder1.record_layout.setVisibility(View.GONE);
					holder1.locationLayout.setVisibility(View.GONE);
					holder1.pic.setVisibility(View.GONE);
					holder1.body.setVisibility(View.GONE);
					holder1.locationShareView.setVisibility(View.VISIBLE);
					try
					{
						JSONObject json = new JSONObject(iMessage.getBody());
						int locState = json.getInt(LocationShareController.LOC_STATE);
						if (locState == AirMessage.LOCATION_SHARE_START)
							holder1.tvLocationShare.setText(mContext.getString(R.string.talk_me) + " " + mContext.getString(R.string.talk_location_share_start));
						else if (locState == AirMessage.LOCATION_SHARE_STOP)
							holder1.tvLocationShare.setText(mContext.getString(R.string.talk_me) + " " + mContext.getString(R.string.talk_location_share_end));
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
				else if (type == AirMessage.TYPE_VIDEO || type == AirMessage.TYPE_VIDEO_SHARE_STORE)
				{
					holder1.tvAndIvLayout.setVisibility(View.GONE);
					holder1.record_layout.setVisibility(View.GONE);
					holder1.locationLayout.setVisibility(View.GONE);
					holder1.videoLayout.setVisibility(View.VISIBLE);
					holder1.ivVideoMsg.setVisibility(View.VISIBLE);
					holder1.pic.setVisibility(View.GONE);
					holder1.body.setVisibility(View.GONE);
					holder1.videoPic.setVisibility(View.GONE);
					displayImageByUrl("file://" + IOoperate.FOLDER_PATH + IOoperate.IMAGES_PATH + "/" + iMessage.getMessageCode() + ".jpg", holder1.ivVideoMsg, iMessage.getMessageCode(), null);
				}
			}
			holder1.report_icon.setVisibility(View.VISIBLE);
			holder1.time.setVisibility(View.VISIBLE);
			switch (state)
			{
				case AirMessage.STATE_RES_DOING:
					holder1.time.setText("");
					holder1.pro.setVisibility(View.VISIBLE);
					holder1.report_icon.setImageResource(R.drawable.msg_state_sending);
					break;
				case AirMessage.STATE_SENDING:
					holder1.time.setText("");
					holder1.pro.setVisibility(View.GONE);
					holder1.report_icon.setImageResource(R.drawable.msg_state_sending);
					break;
				case AirMessage.STATE_RES_FAIL:
				case AirMessage.STATE_RESULT_FAIL:
					holder1.time.setText(iMessage.getTime());
					holder1.pro.setVisibility(View.GONE);
					holder1.report_icon.setImageResource(R.drawable.msg_state_send_error);
					if (iMessage.getType() == AirMessage.TYPE_SESSION_VIDEO)
					{
						holder1.report_icon.setVisibility(View.GONE);
					}
					break;
				case AirMessage.STATE_GENERATING:
					holder1.time.setVisibility(View.INVISIBLE);
					holder1.report_icon.setVisibility(View.INVISIBLE);
					holder1.record_time.setVisibility(View.INVISIBLE);
					holder1.record_default.setVisibility(View.INVISIBLE);
					break;
				default:
					holder1.time.setText(iMessage.getTime());
					holder1.pro.setVisibility(View.GONE);
					holder1.report_icon.setVisibility(View.INVISIBLE);
					break;
			}

			if (iMessage.getRecordType() == AirMessage.RECORD_TYPE_PTT)
			{
				holder1.msg_ptt.setVisibility(View.VISIBLE);
				holder1.report_icon.setVisibility(View.GONE);
			}
			else
			{
				if (iMessage.getType() == AirMessage.TYPE_LOCATION_SHARE_STATE)
					holder1.report_icon.setVisibility(View.GONE);
				holder1.msg_ptt.setVisibility(View.INVISIBLE);
			}
			holder1.userHead.setTag(AccountController.getUserIpocId());
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		return convertView;
	}

	/**
	 * 构建其他人发送的消息记录
	 * @param position 位置
	 * @param convertView view
	 * @param iMessage 消息Entity
	 * @return View
	 */
	private View buildMessageItemWithOther(int position, View convertView, final AirMessage iMessage)
	{
		try
		{
			int type = iMessage.getType();
			int state = iMessage.getState();
			ViewHolder2 holder2 = null;

			if (convertView == null || !(convertView.getTag() instanceof ViewHolder2))
			{
				convertView = ((Activity) mContext).getLayoutInflater().inflate(R.layout.listitem_conversation_message_other, null);
				holder2 = new ViewHolder2();
				holder2.ViewHolderInit(convertView);
				holder2.downlaod_btn = (TextView) convertView.findViewById(R.id.downlaod_btn);
				holder2.unRead = (ImageView) convertView.findViewById(R.id.un_read);
				holder2.userName = (TextView) convertView.findViewById(R.id.user_name);
				convertView.setTag(holder2);

			}
			else if (convertView.getTag() instanceof ViewHolder2)
			{
				holder2 = (ViewHolder2) convertView.getTag();
			}

			if (holder2 == null)
				return convertView;
			holder2.pic.setVisibility(View.GONE);
			holder2.bodyContent.setTag(iMessage);
			holder2.bodyContent.setOnClickListener(onClicklistener);
			holder2.bodyContent.setOnLongClickListener(onLongClickListener);
			holder2.unRead.setVisibility(View.GONE);
			holder2.tvAndIvLayout.setVisibility(View.VISIBLE);
			holder2.record_layout.setVisibility(View.GONE);
			holder2.videoLayout.setVisibility(View.GONE);
			holder2.locationLayout.setVisibility(View.GONE);
			holder2.body.setVisibility(View.VISIBLE);
			holder2.locationShareView.setVisibility(View.GONE);
			holder2.videoSessionLayout.setVisibility(View.GONE);
			if (type == AirMessage.TYPE_RECORD)
			{
				holder2.unRead.setTag(iMessage.getMessageCode() + "unRead");
				holder2.unRead.setVisibility(iMessage.getState() == AirMessage.STATE_NEW ? View.VISIBLE : View.GONE);
				holder2.tvAndIvLayout.setVisibility(View.INVISIBLE);
				holder2.record_layout.setVisibility(View.VISIBLE);
				holder2.locationLayout.setVisibility(View.GONE);
				holder2.videoLayout.setVisibility(View.GONE);
				holder2.videoPic.setVisibility(View.GONE);
				holder2.record_default.setVisibility(View.VISIBLE);
				holder2.record_layout.setTag(iMessage.getMessageCode());
				if (!iMessage.isRecordPlaying())
				{
					holder2.record_time.setText("" + iMessage.getImageLength() + "''");
					holder2.record_default.setImageResource(ThemeUtil.getResourceId(R.attr.theme_msg_audio_play, mContext));
				}
				else
				{
					holder2.record_time.setText("" + iMessage.getRecordTimer() + "''");
					holder2.record_default.setImageResource(ThemeUtil.getResourceId(R.attr.theme_msg_audio_stop, mContext));
				}
			}
			else if (type == AirMessage.TYPE_PICTURE)
			{
				holder2.record_default.setVisibility(View.GONE);
				holder2.pic.setVisibility(View.VISIBLE);
				holder2.record_layout.setVisibility(View.GONE);
				holder2.videoPic.setVisibility(View.GONE);
				holder2.locationLayout.setVisibility(View.GONE);
				holder2.videoLayout.setVisibility(View.GONE);
				String url = iMessage.getImageUri();
				if (iMessage.getSecretType() > 0)
					url = "file://" + IOoperate.FOLDER_PATH + IOoperate.IMAGES_PATH + "/" +iMessage.getMessageCode();
				displayImageByUrl(url, holder2.pic, iMessage.getMessageCode(), this);
			}
			else if (type == AirMessage.TYPE_SESSION_VIDEO)
			{
				holder2.record_default.setVisibility(View.GONE);
				holder2.tvAndIvLayout.setVisibility(View.GONE);
				holder2.record_layout.setVisibility(View.GONE);
				holder2.locationLayout.setVisibility(View.GONE);
				holder2.videoSessionLayout.setVisibility(View.VISIBLE);
				holder2.videoLayout.setVisibility(View.GONE);
				holder2.videoPic.setVisibility(View.VISIBLE);
				holder2.videoBody.setVisibility(View.VISIBLE);
				holder2.body.setVisibility(View.VISIBLE);
			}
			else if(type == AirMessage.TYPE_LOCATION)
			{
				iMessage.setBody(iMessage.getBody().replace("\\", ""));
				holder2.pic.setVisibility(View.GONE);
				holder2.body.setVisibility(View.GONE);
				holder2.videoPic.setVisibility(View.GONE);
				holder2.tvAndIvLayout.setVisibility(View.GONE);
				holder2.record_layout.setVisibility(View.GONE);
				holder2.locationLayout.setVisibility(View.VISIBLE);
				holder2.videoLayout.setVisibility(View.GONE);
				holder2.locationBottom.getBackground().setAlpha(200);
			}
			else if (type == AirMessage.TYPE_LOCATION_SHARE_STATE)
			{
				holder2.videoLayout.setVisibility(View.GONE);
				holder2.tvAndIvLayout.setVisibility(View.GONE);
				holder2.record_layout.setVisibility(View.GONE);
				holder2.locationLayout.setVisibility(View.GONE);
				holder2.pic.setVisibility(View.GONE);
				holder2.body.setVisibility(View.GONE);
				try
				{
					JSONObject json = new JSONObject(iMessage.getBody());
					int locState = json.getInt(LocationShareController.LOC_STATE);
					if (locState == AirMessage.LOCATION_SHARE_START)
					{
						holder2.tvLocationShare.setText(iMessage.getIpocidFrom() + " " + mContext.getString(R.string.talk_location_share_start));
						holder2.locationShareView.setVisibility(View.VISIBLE);
					}
					else if (locState == AirMessage.LOCATION_SHARE_STOP)
					{
						holder2.tvLocationShare.setText(iMessage.getIpocidFrom() + " " + mContext.getString(R.string.talk_location_share_end));
						holder2.locationShareView.setVisibility(View.VISIBLE);
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
			else if (type == AirMessage.TYPE_VIDEO)
			{
				ViewGroup.LayoutParams param = holder2.videoLayout.getLayoutParams();
				param.height = 180*2;
				param.width = 150*2;
				holder2.videoLayout.setLayoutParams(param);
				holder2.videoLayout.setVisibility(View.VISIBLE);
				holder2.ivVideoMsg.setVisibility(View.VISIBLE);
				holder2.tvAndIvLayout.setVisibility(View.GONE);
				holder2.record_layout.setVisibility(View.GONE);
				holder2.locationLayout.setVisibility(View.GONE);
				holder2.pic.setVisibility(View.GONE);
				holder2.body.setVisibility(View.GONE);
				holder2.videoPic.setVisibility(View.GONE);
				displayImageByUrl("file://" + IOoperate.FOLDER_PATH + IOoperate.IMAGES_PATH + "/" + iMessage.getMessageCode() + ".jpg", holder2.ivVideoMsg, iMessage.getMessageCode(), null);
			}
			else if (type == AirMessage.TYPE_VIDEO_SHARE_STORE)
			{
				ViewGroup.LayoutParams param = holder2.videoLayout.getLayoutParams();
				param.height = 60*2;
				param.width = 150*2;
				holder2.videoLayout.setLayoutParams(param);
				holder2.videoLayout.setVisibility(View.VISIBLE);
				holder2.ivVideoMsg.setVisibility(View.GONE);
				holder2.tvAndIvLayout.setVisibility(View.GONE);
				holder2.record_layout.setVisibility(View.GONE);
				holder2.locationLayout.setVisibility(View.GONE);
				holder2.pic.setVisibility(View.GONE);
				holder2.body.setVisibility(View.GONE);
				holder2.videoPic.setVisibility(View.GONE);
			}
			else if (type == AirMessage.TYPE_VIDEO_SHARE_REAL)
			{
				holder2.pic.setVisibility(View.GONE);
				holder2.body.setVisibility(View.GONE);
				holder2.tvAndIvLayout.setVisibility(View.GONE);
				holder2.record_layout.setVisibility(View.GONE);
				holder2.locationLayout.setVisibility(View.GONE);
				holder2.videoSessionLayout.setVisibility(View.VISIBLE);
				holder2.videoLayout.setVisibility(View.GONE);
				holder2.videoPic.setVisibility(View.VISIBLE);
			}
			holder2.time.setText(iMessage.getTime());
			holder2.userHead.setTag(iMessage.getIpocidFrom());
			holder2.userName.setText(iMessage.getInameFrom());
			holder2.downlaod_btn.setVisibility(View.GONE);
			switch (state)
			{
				case AirMessage.STATE_DOWNLOADING:
					holder2.time.setText("");
					holder2.pro.setVisibility(View.VISIBLE);
					break;
				case AirMessage.STATE_RES_FAIL:
				case AirMessage.STATE_RESULT_FAIL:
					holder2.time.setText(iMessage.getTime());
					holder2.pro.setVisibility(View.GONE);
					if (type == AirMessage.TYPE_PICTURE)
					{
						holder2.pic.setImageResource(R.drawable.msg_image_error);
						viewHolder2_showImageDownload(holder2, iMessage);
					}
					else if (type == AirMessage.TYPE_VIDEO || type == AirMessage.TYPE_VIDEO_SHARE_STORE)
					{
						viewHolder2_showVideoDownload(holder2, iMessage);
					}
					break;
				case AirMessage.STATE_RESULT_OK:
					holder2.time.setText(iMessage.getTime());
					holder2.pro.setVisibility(View.GONE);
					break;
				default:
					holder2.time.setText(iMessage.getTime());
					holder2.pro.setVisibility(View.GONE);
					holder2.record_default.setVisibility(View.VISIBLE);
					break;
			}
			holder2.msg_ptt.setVisibility(iMessage.getRecordType() == AirMessage.RECORD_TYPE_PTT ? View.VISIBLE : View.INVISIBLE);
		}
		catch (Exception e)
		{
		}
		
		return convertView;
	}

	/**
	 * 显示收到的图片
	 * @param holder 结构体
	 * @param message 消息Entity
	 */
	private void viewHolder2_showImageDownload(ViewHolder2 holder, final AirMessage message)
	{
		try
		{
			holder.downlaod_btn.setVisibility(View.VISIBLE);
			holder.downlaod_btn.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					AirtalkeeMessage.getInstance().MessageImageDownload(message);
				}
			});
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}
	
	/**
	 * 显示收到的视频
	 * @param holder 结构体
	 * @param message 消息Entity
	 */
	private void viewHolder2_showVideoDownload(ViewHolder2 holder, final AirMessage message)
	{
		try
		{
			holder.downlaod_btn.setVisibility(View.VISIBLE);
			holder.downlaod_btn.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					AirtalkeeMessage.getInstance().MessageVideoDownload(message);
				}
			});
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	abstract class ViewHolder
	{
		protected TextView time;
		protected TextView date;
		protected View tvAndIvLayout, record_layout, videoSessionLayout, locationLayout, videoLayout;
		// text and picture
		protected ImageView pic;
		protected TextView body;
		protected ProgressBar pro;
		// record and ptt msg
		protected ImageView loading;
		protected ImageView record_default;
		protected TextView record_time;
		// session video
		protected ImageView videoPic;
		protected TextView videoBody;
		// location msg
		protected ImageView mMapView;
		protected View locationBottom;
		protected TextView tvLocationTitle, tvLocationAddr;
		// location share msg
		protected View locationShareView;
		protected TextView tvLocationShare;
		// video msg
		protected ImageView ivVideoMsg;
		
		protected ImageView userHead;
		protected View bodyLayout;
		protected TextView tvSystem;
		protected ImageView msg_ptt;
		protected View bodyContent;
		protected TextView userName;

		protected void ViewHolderInit(View convertView)
		{
			this.time = (TextView) convertView.findViewById(R.id.time);
			this.body = (TextView) convertView.findViewById(R.id.body);
			this.date = (TextView) convertView.findViewById(R.id.sessionDate);
			this.videoBody = (TextView) convertView.findViewById(R.id.video_body);
			this.pic = (ImageView) convertView.findViewById(R.id.pic);
			this.userHead = (ImageView) convertView.findViewById(R.id.user_head);
			this.loading = (ImageView) convertView.findViewById(R.id.loading);
			this.pro = (ProgressBar) convertView.findViewById(R.id.pro_load);
			this.record_time = (TextView) convertView.findViewById(R.id.record_time);
			this.record_default = (ImageView) convertView.findViewById(R.id.record_pic);
			this.record_layout = convertView.findViewById(R.id.record_layout);
			this.tvAndIvLayout = convertView.findViewById(R.id.text_and_picture_layout);
			this.videoSessionLayout = convertView.findViewById(R.id.session_video_layout);
			this.locationLayout = convertView.findViewById(R.id.location_layout);
			this.bodyLayout = convertView.findViewById(R.id.body_layout);
			this.bodyContent = convertView.findViewById(R.id.body_content);
			this.tvSystem = (TextView) convertView.findViewById(R.id.tv_system);
			this.msg_ptt = (ImageView) convertView.findViewById(R.id.msg_ptt);
			this.videoPic = (ImageView) convertView.findViewById(R.id.video_pic);
			this.locationBottom = convertView.findViewById(R.id.rl_loction_bottom);
			this.mMapView = (ImageView) convertView.findViewById(R.id.mv_msg_location);
			this.tvLocationTitle = (TextView) convertView.findViewById(R.id.tv_msg_location_title);
			this.tvLocationAddr = (TextView) convertView.findViewById(R.id.tv_msg_location_detail);
			this.videoLayout = convertView.findViewById(R.id.video_layout);
			this.ivVideoMsg = (ImageView) convertView.findViewById(R.id.iv_msg_video);
			this.locationShareView = convertView.findViewById(R.id.location_share_layout);
			this.tvLocationShare = (TextView) convertView.findViewById(R.id.tv_location_share);
		}
	}

	protected class ViewHolder1 extends ViewHolder
	{
		ImageView report_icon;
	}

	class ViewHolder2 extends ViewHolder
	{
		TextView downlaod_btn;
		ImageView unRead;
	}

	/**
	 * 是否需要显示时间分割线
	 * @param position 位置
	 * @return
	 */
	private boolean needShowDateline(int position)
	{
		try
		{
			if (currentSession.getMessages().size() == 0)
				return false;
			if (position == 0)
			{
				return true;
			}
			else
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				AirMessage preMsg = currentSession.getMessages().get(position - 1);
				AirMessage currentMsg = currentSession.getMessages().get(position);

				String preDateStr = preMsg.getDate() + " " + preMsg.getTime();
				preDateStr = preDateStr.replace("年", "-").replace("月", "-").replace("日", "");
				Date preDate = sdf.parse(preDateStr);

				String currentDateStr = currentMsg.getDate() + " " + currentMsg.getTime();
				currentDateStr = currentDateStr.replace("年", "-").replace("月", "-").replace("日", "");
				Date currentDate = sdf.parse(currentDateStr);
				int minutes = (int) (currentDate.getTime() - preDate.getTime()) / (1000 * 60);
				if (minutes > 10)
				{
					return true;
				}
			}
		}
		catch (Exception e)
		{
			//
		}
		return false;
	}

	/**
	 * 通过消息code获取消息Entity
	 * @param code 消息code
	 * @return 消息Entity
	 */
	public AirMessage getMessageByCode(String code)
	{
		AirMessage msg = null;
		List<AirMessage> iMessages = currentSession.getMessages();
		if (iMessages != null)
		{
			for (int i = 0; i < iMessages.size(); i++)
			{
				msg = iMessages.get(i);
				if (msg != null)
				{
					if (code.equals(msg.getMessageCode()))
						return msg;
				}
			}
		}
		return msg;
	}

	/**
	 * 获取图片的Url地址
	 * @param url 地址
	 * @return url地址列表
	 */
	public ArrayList<String> getPicUrls(String[] url)
	{
		ArrayList<String> array = new ArrayList<String>();
		ArrayList<String> array1 = new ArrayList<String>();
		try
		{
			List<AirMessage> iMessages = currentSession.getMessages();
			if (iMessages != null)
			{
				for (int i = 0; i < iMessages.size(); i++)
				{
					AirMessage msg = iMessages.get(i);
					if (msg != null && msg.getType() == AirMessage.TYPE_PICTURE)
					{
						if (msg.getSecretType() > 0)
							array.add("file://" + IOoperate.FOLDER_PATH + IOoperate.IMAGES_PATH + "/" + msg.getMessageCode());
						else
							array.add(msg.getImageUri());
					}
				}
				String position = null;
				int j = 0;
				for (int i = array.size() - 1; i >= 0; i--)
				{
					array1.add(array.get(i));
					if (array.get(i).equals(url[0]))
					{
						position = new String(j + "");
					}
					j++;
				}
				if (position == null)
				{
					
				}
				url[0] = position;
			}	
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		return array1;
	}

	@Override
	public void onImageLoadCompleted(String imageUri, int orientation, View v, int width, int height)
	{
		try
		{
			/*
			ImageSize s = mImageSizeMap.get(imageUri);
			if (s != null)
			{
				width = s.width;
				height = s.height;
			}
			else
			{
				s = new ImageSize();
				s.width = width;
				s.height = height;
				mImageSizeMap.put(imageUri, s);
			}
			*/

			LayoutParams params = (LayoutParams) v.getLayoutParams();
			switch (orientation)
			{
				case AdapterSessionMessage.ORIENTATION_HORIZONTAL:
				case AdapterSessionMessage.ORIENTATION_VERTICAL:
				{
					params.width = width;
					params.height = height;
					break;
				}
				case AdapterSessionMessage.ORIENTATION_SQUARE:
					params.width = height;
					params.height = height;
					break;
			}
			v.setLayoutParams(params);
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}
}
