package com.cmccpoc.activity.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeMessage;
import com.airtalkee.sdk.AirtalkeeUserInfo;
import com.airtalkee.sdk.OnMessageListListener;
import com.airtalkee.sdk.controller.AccountController;
import com.airtalkee.sdk.entity.AirLocationShare;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.PicFactory;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.activity.ActivityImagePager;
import com.cmccpoc.activity.ActivityLocationMap;
import com.cmccpoc.activity.ActivityVideoPlayer;
import com.cmccpoc.activity.AlbumChooseActivity;
import com.cmccpoc.activity.MapPointBaiduActivity;
import com.cmccpoc.activity.MapShareActivity;
import com.cmccpoc.activity.VideoSessionActivity;
import com.cmccpoc.activity.home.adapter.AdapterSessionMessage;
import com.cmccpoc.activity.home.widget.AlertDialog;
import com.cmccpoc.activity.home.widget.SessionAndChannelView;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirLocationShareControl;
import com.cmccpoc.control.AirMessageTransaction;
import com.cmccpoc.listener.OnMmiMessageListener;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.AirMmiTimerListener;
import com.cmccpoc.util.Const;
import com.cmccpoc.util.Sound;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;
import com.cmccpoc.widget.MacRecordingView;
import com.cmccpoc.widget.PullToRefreshListView;
import com.cmccpoc.widget.PullToRefreshListView.OnPullToRefreshListener;
import com.cmccpoc.widget.VideoCamera;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 三大Fragment之一：IM消息Fragment，主要显示IM消息，可以发送语音、文字已经图片等消息
 * 
 * @author Yao
 */
public class IMFragment extends BaseFragment implements OnClickListener,
		OnMessageListListener, OnLongClickListener, TextWatcher,
		OnMmiMessageListener, OnPullToRefreshListener, OnItemClickListener,
		AirMmiTimerListener, OnTouchListener
{
	private static final int REQUEST_CODE_BROWSE_IMAGE = 111;

	public View textVoicePannel, textPannel, voicePannel, toolsPannel, locationSharePannel;
	private ImageView btnVoice, btnClose, btnImage, btnCamera, btnVideo;
	private TextView locationShareText;
	private PullToRefreshListView lvMessage;
	private AdapterSessionMessage adapterMessage;
	private AirMessage currentMessage;
	private AirSession session;
	private EditText etMsg;
	private Button btnSend;
	private Animation animRefresh;
	private MacRecordingView mvRecording;
	private AlertDialog dialog;
	private boolean recordCancel = false;
	private float startY = 0;
	public static String menuArray[];

	private boolean flag = true;
	private static boolean isSetSelection = true;
	private int resumeCount = 0;
	private static IMFragment mInstance;
	private PopupWindow pwLocation = null;

	public static IMFragment getInstance()
	{
		return mInstance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mInstance = this;
		AirMessageTransaction.getInstance().setOnMessageListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		v = inflater.inflate(getLayout(), container, false);
		try
		{
			textVoicePannel = findViewById(R.id.voic_text_pannel);
			textPannel = findViewById(R.id.text_pannel);
			voicePannel = findViewById(R.id.voice_pannel);
			toolsPannel = findViewById(R.id.tools_pannel);
			locationSharePannel = findViewById(R.id.layout_location_share);
			locationSharePannel.setOnClickListener(this);
			etMsg = (EditText) findViewById(R.id.et_msg);
			etMsg.addTextChangedListener(this);
			btnSend = (Button) findViewById(R.id.send);
			btnSend.setOnClickListener(this);
			btnVoice = (ImageView) findViewById(R.id.btn_voice);
			btnVoice.setOnTouchListener(this);
			btnClose = (ImageView) findViewById(R.id.tools_btn_close);
			btnClose.setOnClickListener(this);
			btnImage = (ImageView) findViewById(R.id.tools_btn_image);
			btnImage.setOnClickListener(this);
			btnCamera = (ImageView) findViewById(R.id.tools_btn_video);
			btnCamera.setOnClickListener(this);
			btnVideo = (ImageView) findViewById(R.id.tools_btn_gps);
			btnVideo.setOnClickListener(this);
			locationShareText = (TextView) findViewById(R.id.location_share_text);
			findViewById(R.id.btn_text_close).setOnClickListener(this);
			findViewById(R.id.btn_voice_close).setOnClickListener(this);

			adapterMessage = new AdapterSessionMessage(getActivity(), this, this);
			adapterMessage.notifyDataSetChanged();
			lvMessage = (PullToRefreshListView) findViewById(R.id.lv_message);
			lvMessage.setAdapter(adapterMessage);
			lvMessage.setOnRefreshListener(this);
			lvMessage.setOnItemClickListener(this);

			animRefresh = AnimationUtils.loadAnimation(getActivity(), R.anim.refresh);

			mvRecording = (MacRecordingView) findViewById(R.id.mac_talking);
			mvRecording.initChild();
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		return v;
	}

	@Override
	public int getLayout()
	{
		return R.layout.frag_im_layout;
	}

	@Override
	public void onPause()
	{
		super.onPause();
		try
		{
			/*if (BaseActivity.getInstance().pageIndex == BaseActivity.PAGE_IM)
			{
				setVoicePannelVisiblity(View.GONE);
				setTextPannelVisiblity(View.GONE);
			}*/
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		try
		{
			setSession(getSession());
			if (adapterMessage != null)
			{
				Log.i(HomeActivity.class, "IMFragment onResume adapterMessage size" + adapterMessage.getCount() + ",isSetSelection="+isSetSelection+",resumeCount="+resumeCount);
				if (isSetSelection && resumeCount == 0)
				{
					lvMessage.setSelection(adapterMessage.getCount());
				}
				else
				{
					if (resumeCount > 0)
						resumeCount = 0;
					else
					{
						if (!isSetSelection)
							resumeCount++;
					}
					isSetSelection = true;
				}
			}
		}
		catch (Exception e)
		{}
	}

	@Override
	public void dispatchBarClickEvent(int page, int id)
	{
		try
		{
			if (page == HomeActivity.PAGE_IM)
			{
				switch (id)
				{
					case R.id.bar_left:
						if (AirtalkeeAccount.getInstance().isEngineRunning())
							setVoicePannelVisiblity(View.VISIBLE);
						else
							Util.Toast(getActivity(), getActivity().getString(R.string.talk_network_warning));
						break;
					case R.id.bar_mid:
						if (AirtalkeeAccount.getInstance().isEngineRunning())
							setToolsPannelVisiblity(View.VISIBLE);
						else
							Util.Toast(getActivity(), getActivity().getString(R.string.talk_network_warning));
						break;
					case R.id.bar_right:
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						if (imm != null)
						{
							imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
						}
						setTextPannelVisiblity(View.VISIBLE);
						break;
				}
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onLongClick(View v)
	{
		try
		{
			if (v.getId() == R.id.body_content)
			{
				if (v.getTag() != null)
				{
					currentMessage = (AirMessage) v.getTag();
					if (currentMessage != null)
					{
						if (currentMessage.getType() == AirMessage.TYPE_CUSTOM_RELATION)
						{
							return false;
						}

						int dialogId = R.id.talk_dialog_message_txt;
						if (currentMessage.getIpocidFrom().equals(AccountController.getUserInfo().getIpocId()))
						{
							dialogId = R.id.talk_dialog_message_txt_send_fail;
							switch (currentMessage.getType())
							{
								case AirMessage.TYPE_TEXT:
									menuArray = getResources().getStringArray(R.array.handle_message_send_fail);
									break;
								case AirMessage.TYPE_PICTURE:
								case AirMessage.TYPE_RECORD:
								case AirMessage.TYPE_VIDEO:
								case AirMessage.TYPE_VIDEO_SHARE_STORE:
								case AirMessage.TYPE_LOCATION:
									menuArray = getResources().getStringArray(R.array.handle_message_send_fail1);
									break;
								default:
									menuArray = getResources().getStringArray(R.array.handle_message_txt1);
									break;
							}
						}
						else
						{
							switch (currentMessage.getType())
							{
								case AirMessage.TYPE_TEXT:
									menuArray = getResources().getStringArray(R.array.handle_message_txt);
									break;
								case AirMessage.TYPE_PICTURE:
								case AirMessage.TYPE_LOCATION:
								case AirMessage.TYPE_RECORD:
								case AirMessage.TYPE_VIDEO:
								case AirMessage.TYPE_VIDEO_SHARE_STORE:
									menuArray = getResources().getStringArray(R.array.handle_message_txt1);
									break;
								default:
									menuArray = getResources().getStringArray(R.array.handle_message_txt1);
									break;
							}
						}
						getActivity().removeDialog(dialogId);
						getActivity().showDialog(dialogId);
					}
				}
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		return false;
	}

	@Override
	public void onClick(View v)
	{
		try
		{
			switch (v.getId())
			{
				case R.id.btn_text_close:
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					if (imm != null)
					{
						imm.hideSoftInputFromWindow(mediaStatusBar.getBottomBarParent().getWindowToken(), 0);
					}
					setTextPannelVisiblity(View.GONE);
					etMsg.setText("");
					break;
				case R.id.btn_voice_close:
					setVoicePannelVisiblity(View.GONE);
					break;
				case R.id.send:
					if (AirtalkeeAccount.getInstance().isEngineRunning())
						messageSend();
					else
						Util.Toast(getActivity(), getActivity().getString(R.string.talk_network_warning));
					break;
				case R.id.body_content:
				{
					if (session != null && v.getTag() != null)
					{
						currentMessage = (AirMessage) v.getTag();
						if (currentMessage != null)
						{
							switch (currentMessage.getType())
							{
								case AirMessage.TYPE_RECORD:
									messageRecordPlay(v);
									break;
								case AirMessage.TYPE_PICTURE:
								{
									isSetSelection = false;
									if (getActivity() != null)
									{
										Intent intent = new Intent(getActivity(), ActivityImagePager.class);
										String url = "";
										try
										{
											if (TextUtils.equals(currentMessage.getIpocidFrom(), AirtalkeeUserInfo.getInstance().getUserInfo().getIpocId()))
											{
												// url =
												// currentMessage.getImageUri();
												url = "file://" + IOoperate.FOLDER_PATH + IOoperate.IMAGES_PATH + "/" + currentMessage.getMessageCode();
											}
											else
											{
												url = currentMessage.getImageUri();
												if (currentMessage.getSecretType() > 0)
													url = "file://" + IOoperate.FOLDER_PATH + IOoperate.IMAGES_PATH + "/" + currentMessage.getMessageCode();
											}
											String[] position = new String[] { url };
											ArrayList<String> images = adapterMessage.getPicUrls(position);
											Bundle b = new Bundle();
											b.putStringArrayList("images", images);
											b.putInt("position", Integer.parseInt(position[0]));
											intent.putExtras(b);
											startActivity(intent);
										}
										catch (Exception e)
										{
											url = currentMessage.getImageUri();
											String[] position = new String[] { url };
											ArrayList<String> images = adapterMessage.getPicUrls(position);
											Bundle b = new Bundle();
											b.putStringArrayList("images", images);
											b.putInt("position", Integer.parseInt(position[0]));
											intent.putExtras(b);
											startActivity(intent);
										}
									}
									break;
								}
								case AirMessage.TYPE_LOCATION:
								{
									isSetSelection = false;
									if (getActivity() != null)
									{
										JSONTokener jsonParser = new JSONTokener(currentMessage.getBody());
										try
										{
											JSONObject location = (JSONObject) jsonParser.nextValue();
											if (location.getDouble("latitude") == 0 && location.getDouble("longitude") == 0)
											{
												if(Toast.isDebug) Toast.makeText1(AirServices.getInstance(), "位置信息错误，无法打开", Toast.LENGTH_LONG).show();
											}
										}
										catch (Exception e)
										{
											e.printStackTrace();
										}
									}
									break;
								}
								case AirMessage.TYPE_VIDEO:
								case AirMessage.TYPE_VIDEO_SHARE_STORE:
								{
									isSetSelection = false;
									if (getActivity() != null)
									{
										Intent intent = new Intent(getActivity(), ActivityVideoPlayer.class);
										Bundle b = new Bundle();
										if (currentMessage.getType() == AirMessage.TYPE_VIDEO_SHARE_STORE)
										{
											b.putString(ActivityVideoPlayer.PARAM_PATH, currentMessage.getImageUri());
											b.putInt(ActivityVideoPlayer.PARAM_TYPE, ActivityVideoPlayer.VIDEO_TYPE_URL);
										}
										else
										{
											b.putString(ActivityVideoPlayer.PARAM_PATH, IOoperate.FOLDER_PATH + IOoperate.VIDEO_PATH + "/" + currentMessage.getMessageCode());
											b.putInt(ActivityVideoPlayer.PARAM_TYPE, ActivityVideoPlayer.VIDEO_TYPE_LOCAL);
										}
										intent.putExtras(b);
										startActivity(intent);
									}
									break;
								}
								case AirMessage.TYPE_VIDEO_SHARE_REAL:
								{
									Intent i = new Intent();
									i.setClass(getContext(), VideoSessionActivity.class);
									i.putExtra("sessionCode", session.getSessionCode());
									//i.putExtra("auto", true);
									startActivity(i);
									break;
								}
								case AirMessage.TYPE_LOCATION_SHARE_STATE:
								{
									if (getActivity() != null && Config.funcLocationShare && session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
									{
										Intent mapIntent = new Intent(getActivity(), MapShareActivity.class);
										mapIntent.putExtra("sessionCode", session.getSessionCode());
										startActivity(mapIntent);
									}
								}
								default:
									break;
							}
						}
					}
					break;
				}
				case R.id.tools_btn_close:
					setToolsPannelVisiblity(View.GONE);
					break;
				case R.id.tools_btn_image:
				{
					String status = Environment.getExternalStorageState();
					if (!status.equals(Environment.MEDIA_MOUNTED))
					{
						Util.Toast(getActivity(), getActivity().getString(R.string.insert_sd_card));
						return;
					}
					// 系统相册
					// Intent localIntent = new
					// Intent("android.intent.action.GET_CONTENT", null);
					// localIntent.setType("image/*");
					// 自定义相册
					Intent localIntent = new Intent(getActivity(), AlbumChooseActivity.class);
					localIntent.putExtra("type", AlbumChooseActivity.TYPE_IM);
					localIntent.putExtra("action", AlbumChooseActivity.ACTION_ALBUM);
					startActivityForResult(localIntent, REQUEST_CODE_BROWSE_IMAGE);
					break;
				}
				case R.id.tools_btn_video:
				{
					String status = Environment.getExternalStorageState();
					if (!status.equals(Environment.MEDIA_MOUNTED))
					{
						Util.Toast(getActivity(), getActivity().getString(R.string.insert_sd_card));
						return;
					}
					Intent serverIntent = new Intent(getActivity(), VideoCamera.class);
					serverIntent.putExtra("videoType", 0);
					startActivityForResult(serverIntent, Const.image_select.REQUEST_CODE_CREATE_VIDEO);
					break;
				}
				case R.id.tools_btn_gps:
				{
					if (AirtalkeeAccount.getInstance().isEngineRunning())
					{
						if (Config.funcLocationShare)
							showLocationPopup();
						else
						{
							Intent mapIntent = new Intent(getActivity(), MapPointBaiduActivity.class);
							startActivity(mapIntent);
						}
					}
					else
						Util.Toast(getActivity(), getActivity().getString(R.string.talk_network_warning));
					break;
				}
				case R.id.location_message:
					if (AirtalkeeAccount.getInstance().isEngineRunning())
					{
						Intent mapIntent = new Intent(getActivity(), MapPointBaiduActivity.class);
						startActivity(mapIntent);
						if (pwLocation != null)
							pwLocation.dismiss();
					}
					else
						Util.Toast(getActivity(), getActivity().getString(R.string.talk_network_warning));
					break;
				case R.id.layout_location_share:
				{
					if (AirtalkeeAccount.getInstance().isEngineRunning())
					{
						Map<String, AirLocationShare> maps = AirLocationShareControl.getInstance().getLocationShareMap(session.getSessionCode());
						if (maps != null && maps.containsKey(AirtalkeeAccount.getInstance().getUser().getIpocId()))
						{
							if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
							{
								Intent itShare = new Intent(getActivity(), MapShareActivity.class);
								itShare.putExtra("sessionCode", session.getSessionCode());
								getActivity().startActivity(itShare);
								if (pwLocation != null)
									pwLocation.dismiss();
							}
							else
								if(Toast.isDebug) Toast.makeText1(getActivity(), getActivity().getString(R.string.talk_location_share_tip), Toast.LENGTH_SHORT).show();
						}
						else
						{
							AlertDialog backDialog = new AlertDialog(getActivity(), getString(R.string.talk_location_share_join_tip), null, new AlertDialog.DialogListener()
							{
								@Override
								public void onClickOk(int id, Object obj)
								{
									if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
									{
										Intent itShare = new Intent(getActivity(), MapShareActivity.class);
										itShare.putExtra("sessionCode", session.getSessionCode());
										getActivity().startActivity(itShare);
										if (pwLocation != null)
											pwLocation.dismiss();
									}
									else
										if(Toast.isDebug) Toast.makeText1(getActivity(), getActivity().getString(R.string.talk_location_share_tip), Toast.LENGTH_SHORT).show();
								}
								@Override
								public void onClickOk(int id, boolean isChecked)
								{
								}

								@Override
								public void onClickCancel(int id)
								{
								}
							}, -1);
							backDialog.show();
						}
					}
					else
						Util.Toast(getActivity(), getActivity().getString(R.string.talk_network_warning));
					break;
				}
				case R.id.location_share:
					if (AirtalkeeAccount.getInstance().isEngineRunning())
					{
						if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
						{
							Intent itShare = new Intent(getActivity(), MapShareActivity.class);
							itShare.putExtra("sessionCode", session.getSessionCode());
							getActivity().startActivity(itShare);
							if (pwLocation != null)
								pwLocation.dismiss();
						}
						else
							if(Toast.isDebug) Toast.makeText1(getActivity(), getActivity().getString(R.string.talk_location_share_tip), Toast.LENGTH_SHORT).show();
					}
					else
						Util.Toast(getActivity(), getActivity().getString(R.string.talk_network_warning));
					break;
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		
	}

	/**
	 * 录音消息播放
	 * 
	 * @param view 当前录音消息所在view
	 */
	private void messageRecordPlay(View view)
	{
		try
		{
			if (session != null)
			{
				view.findViewById(R.id.record_layout).setTag(currentMessage.getMessageCode());
				if (!currentMessage.isRecordPlaying() && currentMessage.getImageUri() != null)
				{
					if (currentMessage.getRecordType() == AirMessage.RECORD_TYPE_PTT)
					{
						if (!AirtalkeeMessage.getInstance().MessageRecordPlayStartLocal(currentMessage))
						{
							Util.Toast(getActivity(), getString(R.string.talk_msg_no_local_file), R.drawable.ic_error);
						}
					}
					else
					{
						AirtalkeeMessage.getInstance().MessageRecordPlayStart(session, currentMessage);
					}
				}
				else
				{
					if (currentMessage.getImageUri() != null)
					{
						AirtalkeeMessage.getInstance().MessageRecordPlayStop();
					}
					else
					{
						if (currentMessage.getRecordType() == AirMessage.RECORD_TYPE_PTT)
						{
							if (!AirtalkeeMessage.getInstance().MessageRecordPlayStartLocal(currentMessage))
							{
								Util.Toast(getActivity(), getString(R.string.talk_msg_no_local_file), R.drawable.ic_error);
							}
						}
						else
						{
							AirtalkeeMessage.getInstance().MessageRecordPlayStart(session, currentMessage);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	/**
	 * 设置底部发送文字消息区域是否可见
	 * 
	 * @param visiblility
	 *            是否可见
	 */
	protected void setTextPannelVisiblity(int visiblility)
	{
		try
		{
			if (visiblility == View.GONE)
			{
				if (textVoicePannel != null)
					textVoicePannel.setVisibility(View.GONE);
				if (textPannel != null)
					textPannel.setVisibility(View.GONE);
				if (toolsPannel != null)
					toolsPannel.setVisibility(View.GONE);
				if (mediaStatusBar != null)
					mediaStatusBar.setMediaStatusBarVisibility(View.VISIBLE);
			}
			else
			{
				if (textVoicePannel != null)
					textVoicePannel.setVisibility(View.VISIBLE);
				if (textPannel != null)
					textPannel.setVisibility(View.VISIBLE);
				if (mediaStatusBar != null)
					mediaStatusBar.setMediaStatusBarVisibility(View.GONE);
				etMsg.setFocusable(true);
				etMsg.setFocusableInTouchMode(true);
				etMsg.requestFocus();
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		
	}

	/**
	 * 设置底部发送录音消息区域是否可见
	 * 
	 * @param visiblility
	 *            是否可见
	 */
	private void setVoicePannelVisiblity(int visiblility)
	{
		try
		{
			if (visiblility == View.GONE)
			{
				if (textVoicePannel != null)
					textVoicePannel.setVisibility(View.GONE);
				if (voicePannel != null)
					voicePannel.setVisibility(View.GONE);
				if (toolsPannel != null)
					toolsPannel.setVisibility(View.GONE);
				if (mediaStatusBar != null)
					mediaStatusBar.setMediaStatusBarVisibility(View.VISIBLE);
			}
			else
			{
				if (textVoicePannel != null)
					textVoicePannel.setVisibility(View.VISIBLE);
				if (voicePannel != null)
					voicePannel.setVisibility(View.VISIBLE);
				if (mediaStatusBar != null)
					mediaStatusBar.setMediaStatusBarVisibility(View.GONE);
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		
	}

	/**
	 * 设置底部发送其他类型消息区域是否可见
	 * 
	 * @param visiblility
	 *            是否可见
	 */
	private void setToolsPannelVisiblity(int visiblility)
	{
		try
		{
			if (visiblility == View.GONE)
			{
				if (textVoicePannel != null)
					textVoicePannel.setVisibility(View.GONE);
				if (voicePannel != null)
					voicePannel.setVisibility(View.GONE);
				if (toolsPannel != null)
					toolsPannel.setVisibility(View.GONE);
				if (mediaStatusBar != null)
					mediaStatusBar.setMediaStatusBarVisibility(View.VISIBLE);
			}
			else
			{
				if (textVoicePannel != null)
					textVoicePannel.setVisibility(View.VISIBLE);
				if (toolsPannel != null)
					toolsPannel.setVisibility(View.VISIBLE);
				if (mediaStatusBar != null)
					mediaStatusBar.setMediaStatusBarVisibility(View.GONE);
			}	
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		
	}

	private void showLocationPopup()
	{
		if (pwLocation == null)
		{
			LayoutInflater mLayoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View actionView = mLayoutInflater.inflate(R.layout.layout_popup_window_location_choose, null);
			pwLocation = new PopupWindow(actionView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			TextView tvLocationMessage = (TextView) actionView.findViewById(R.id.location_message);
			tvLocationMessage.setOnClickListener(this);
			TextView tvLocationShare = (TextView) actionView.findViewById(R.id.location_share);
			tvLocationShare.setOnClickListener(this);
		}
		FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) toolsPannel.getLayoutParams();

		pwLocation.setOutsideTouchable(true);
		pwLocation.setFocusable(true);
		pwLocation.setBackgroundDrawable(new BitmapDrawable());
		pwLocation.showAtLocation(toolsPannel, Gravity.BOTTOM, toolsPannel.getRight(), p.height + 10);
	}

	/**
	 * 设置Session会话
	 * @param s 会话Entity
	 */
	public void setSession(AirSession s)
	{
		try
		{
			if ((s != null && session != null && !s.getSessionCode().equals(session.getSessionCode())) || s != null)
			{
				this.session = s;
				if (lvMessage == null)
					return;
				refreshMessages();
				refreshLocationShareView();
				lvMessage.setHaveMore(s.isMessageMore());
				try
				{
					mHandler.sendEmptyMessageDelayed(1, 10);
				}
				catch (Exception e)
				{}
			}
			if (s != null && s.getMessageTextDraft() != null)
			{
				etMsg.setText(s.getMessageTextDraft());
			}
			adapterMessage.setSession(s);
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	/**
	 * 刷新消息列表
	 */
	public void refreshMessages()
	{
		try
		{
			if (adapterMessage != null)
				adapterMessage.notifyDataSetChanged();
		}
		catch (Exception e)
		{}
	}

	Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			super.handleMessage(msg);
			switch (msg.what)
			{
				case 1:
				{
					flag = false;
					try
					{
						AirtalkeeMessage.getInstance().MessageListMoreLoad(session, IMFragment.this);
					}
					catch (Exception e)
					{
					}
					break;
				}
			}
		}
	};

	/**
	 * 发送消息
	 */
	private void messageSend()
	{
		try
		{
			if (session != null)
			{
				if (session.getType() == AirSession.TYPE_CHANNEL)
				{
					if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
					{
						String msg = etMsg.getText().toString();
						if (msg != null && !msg.trim().equals(""))
						{
							AirtalkeeMessage.getInstance().MessageSend(session, msg, false, true);
							etMsg.setText("");
							adapterMessage.notifyDataSetChanged();
						}
					}
					else
						Util.Toast(getActivity(), getString(R.string.talk_channel_idle));
				}
				else
				{
					String msg = etMsg.getText().toString();
					if (msg != null && !msg.trim().equals(""))
					{
						AirtalkeeMessage.getInstance().MessageSend(session, msg, false, true);
						etMsg.setText("");
						adapterMessage.notifyDataSetChanged();
					}
				}
			}
			else
				Util.Toast(getActivity(), getString(R.string.talk_channel_idle));
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	public void refreshLocationShareView()
	{
		if (session != null && getActivity() != null)
		{
			Map<String, AirLocationShare> maps = AirLocationShareControl.getInstance().getLocationShareMap(session.getSessionCode());
			if (maps != null && maps.size() > 0)
			{
				locationSharePannel.setVisibility(View.VISIBLE);
				if (maps.size() == 1)
				{
					for (Map.Entry<String, AirLocationShare> entry : maps.entrySet())
					{
						String ipocid = entry.getKey();
						if (ipocid.equals(AirtalkeeAccount.getInstance().getUser().getIpocId()))
							locationShareText.setText(getString(R.string.talk_me) + " " + getString(R.string.talk_location_sharing));
						else
							locationShareText.setText(ipocid + " " + getString(R.string.talk_location_sharing));
					}
				}
				else
					locationShareText.setText(maps.size() + " " + getString(R.string.talk_location_share_count));
			}
			else
				locationSharePannel.setVisibility(View.GONE);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
		btnSend.setEnabled(!TextUtils.isEmpty(etMsg.getText().toString().trim()));
	}

	@Override
	public void afterTextChanged(Editable s)
	{

	}

	@Override
	public void onPullToRefresh(int firstVisibleItem, int visibleCount)
	{
		try
		{
			if (session != null && !Utils.isEmpty(session.getSessionCode()))
			{
				if (session.isMessageMore())
				{
					flag = true;
					AirtalkeeMessage.getInstance().MessageListMoreLoad(session, this);
				}
				else
				{
					lvMessage.onRefreshComplete();
				}
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public void onListItemLongClick(int id, int selectedItem)
	{
		try
		{
			if (currentMessage == null)
				return;
			switch (id)
			{
				case R.id.talk_dialog_message_txt:
				{
					switch (selectedItem)
					{
						case 0:
						{
							if (session != null)
							{
								// showRemoveAllDialog(session.getSessionCode());
								// AirtalkeeMessage.getInstance().MessageRemoveAll(session.getSessionCode());
								// adapterMessage.notifyDataSetChanged();
								dialog = new AlertDialog(getActivity(), null, getString(R.string.talk_msg_remove_all_confirm), getString(R.string.talk_no), getString(R.string.talk_ok), new AlertDialog.DialogListener()
								{
									@Override
									public void onClickOk(int id, boolean isChecked)
									{

									}

									@Override
									public void onClickOk(int id, Object obj)
									{
										AirtalkeeMessage.getInstance().MessageRemoveAll(session.getSessionCode());
										adapterMessage.notifyDataSetChanged();
									}

									@Override
									public void onClickCancel(int id)
									{
										dialog.cancel();
									}
								}, 0);
								dialog.show();
							}
							break;
						}
						case 1:
						{
							if (session != null)
							{
								AirtalkeeMessage.getInstance().MessageRemove(session.getSessionCode(), currentMessage);
								adapterMessage.notifyDataSetChanged();
							}
							break;
						}
						case 2:
						{
							Util.textClip(getActivity(), currentMessage.getBody());
							break;
						}
					}
					break;
				}
				case R.id.talk_dialog_message_txt_send_fail:
				{
					switch (selectedItem)
					{
						case 0:
						{
							if (session != null)
							{
								dialog = new AlertDialog(getActivity(), null, getString(R.string.talk_msg_remove_all_confirm), getString(R.string.talk_no), getString(R.string.talk_ok), new AlertDialog.DialogListener()
								{
									@Override
									public void onClickOk(int id, boolean isChecked)
									{

									}

									@Override
									public void onClickOk(int id, Object obj)
									{
										AirtalkeeMessage.getInstance().MessageRemoveAll(session.getSessionCode());
										adapterMessage.notifyDataSetChanged();
									}

									@Override
									public void onClickCancel(int id)
									{
										dialog.cancel();
									}
								}, 0);
								dialog.show();
							}
							break;
						}
						case 1:
						{
							try
							{
								if (session != null)
								{
									AirtalkeeMessage.getInstance().MessageRemove(session.getSessionCode(), currentMessage);
									adapterMessage.notifyDataSetChanged();
								}
							}
							catch (Exception e)
							{}
							break;
						}
						case 2:
						{
							try
							{
								if (session != null)
								{
									if (currentMessage.getType() == AirMessage.TYPE_PICTURE)
									{
										byte[] imageData = currentMessage.getImage();
										if (currentMessage.getState() == AirMessage.STATE_RESULT_FAIL || imageData == null)
										{
											String url = IOoperate.FOLDER_PATH + IOoperate.IMAGES_PATH + "/" + currentMessage.getMessageCode();
											imageData = AirServices.iOperator.readByteFile("", url, true);
										}
										if (imageData != null)
										{
											AirtalkeeMessage.getInstance().MessageImageSend(session, imageData, true);
										}
										else
											if(Toast.isDebug) Toast.makeText1(getActivity(), "图片资源错误，请检查图片是否存在", Toast.LENGTH_LONG).show();
									}
									else if (currentMessage.getType() == AirMessage.TYPE_RECORD)
									{
										if (AirtalkeeAccount.getInstance().getUser().getSecret() > 0) // 加密
											AirtalkeeMessage.getInstance().MessageRecordResend(session, currentMessage.getMessageCode(), currentMessage.getMessageCode(), currentMessage.getImageLength(), true);
										else
											AirtalkeeMessage.getInstance().MessageRecordResend(session, currentMessage.getMessageCode(), currentMessage.getImageUri(), currentMessage.getImageLength(), true);
									}
									else if (currentMessage.getType() == AirMessage.TYPE_VIDEO || currentMessage.getType() == AirMessage.TYPE_VIDEO_SHARE_STORE)
									{
										String filePath = IOoperate.FOLDER_PATH + IOoperate.VIDEO_PATH + "/" + currentMessage.getMessageCode();
										byte[] videoData = null;
										try
										{
											videoData = AirServices.iOperator.readByteFile("", filePath, true);
											AirtalkeeMessage.getInstance().MessageVideoSend(getActivity(), filePath, session, "", videoData, true);
										}
										catch (Exception e)
										{}
									}
									else if (currentMessage.getType() == AirMessage.TYPE_LOCATION)
									{
										AirtalkeeMessage.getInstance().MessageLocationSend(session, currentMessage.getBody(), true);
									}
									else
									{
										AirtalkeeMessage.getInstance().MessageSend(session, currentMessage.getBody(), false, true);
									}
									adapterMessage.notifyDataSetChanged();
								}
							}
							catch (Exception e)
							{}
							break;
						}
						case 3:
						{
							Util.textClip(getActivity(), currentMessage.getBody());
							break;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	/************************************
     * 
     * 
     * 
     ************************************/
	@Override
	public void onMessageIncomingRecv(List<AirMessage> messageList)
	{
		try
		{
			if (messageList != null)
			{
				boolean hasNew = false;
				for (int i = 0; i < messageList.size(); i++)
				{
					AirMessage message = messageList.get(i);
					if (session != null && TextUtils.equals(message.getSessionCode(), session.getSessionCode()))
					{
						hasNew = true;
						break;
					}
				}
				if (hasNew)
				{
					adapterMessage.notifyDataSetChanged();
					HomeActivity.getInstance().checkNewIM(false, null);
					SessionAndChannelView.getInstance().refreshChannelAndDialog();
					getStatusBarTitle().refreshNewMsg();
				}
			}
		}
		catch (Exception e)
		{}
	}

	@Override
	public boolean onMessageIncomingRecv(boolean isCustom, AirMessage message)
	{
		boolean isHandled = false;
		boolean toClean = false;
		try
		{
			if (HomeActivity.getInstance().pageIndex == HomeActivity.PAGE_IM)
			{
				toClean = true;
				if (!isCustom && message != null && session != null/* && TextUtils.equals(session.getSessionCode(), message.getSessionCode())*/)
				{
					try
					{
						adapterMessage.notifyDataSetChanged();
					}
					catch (Exception e)
					{}
					HomeActivity.getInstance().checkNewIM(toClean, null);
					SessionAndChannelView.getInstance().refreshChannelAndDialog();
					refreshLocationShareView();
					isHandled = true;
				}
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
		return isHandled;
	}

	@Override
	public void onMessageOutgoingSent(boolean isCustom, AirMessage message, boolean isSent)
	{
		try
		{
			if (!isCustom && message != null && session != null && TextUtils.equals(session.getSessionCode(), message.getSessionCode()))
			{
				if (isSent && message.getType() != AirMessage.TYPE_LOCATION_SHARE_STATE)
				{
					Sound.vibrate(20, getActivity());
					Sound.playSound(Sound.PLAYER_MSG_SENT, getActivity());
				}

				adapterMessage.notifyDataSetChanged();
				if (adapterMessage.getCount() > 0)
					lvMessage.setSelection(adapterMessage.getCount() - 1);
			}
		}
		catch (Exception e)
		{}
	}

	@Override
	public void onMessageUpdated(AirMessage message)
	{
		try
		{
			if (message != null && session != null && TextUtils.equals(session.getSessionCode(), message.getSessionCode()))
			{
				adapterMessage.notifyDataSetChanged();
			}
		}
		catch (Exception e)
		{}

	}

	@Override
	public void onMessageRecordPlayLoaded(boolean isOk, String msgCode, String resId)
	{
		try
		{
			View view = lvMessage.findViewWithTag(msgCode);
			if (view != null)
			{
				View pro = view.findViewById(R.id.loading);
				if (pro != null)
				{
					pro.clearAnimation();
					pro.setAnimation(null);
					pro.setVisibility(View.GONE);
				}
				ImageView record = (ImageView) view.findViewById(R.id.record_pic);
				if (record != null)
				{
					record.setVisibility(View.VISIBLE);
					// record.setSelected(true);
					record.setImageResource(ThemeUtil.getResourceId(R.attr.theme_msg_audio_play, getActivity()));
				}
			}
		}
		catch (Exception e)
		{}
	}

	@Override
	public void onMessageRecordPlayLoading(String msgCode, String resId)
	{
		try
		{
			View view = lvMessage.findViewWithTag(msgCode);
			if (view != null)
			{
				View record = view.findViewById(R.id.record_pic);
				View pro = view.findViewById(R.id.loading);
				if (record != null)
				{
					pro.clearAnimation();
					record.setVisibility(View.INVISIBLE);
				}
				if (pro != null)
				{
					pro.setVisibility(View.VISIBLE);
					pro.setAnimation(animRefresh);
				}
			}
		}
		catch (Exception e)
		{}
	}

	@Override
	public void onMessageRecordPlayStart(String msgCode, String resId)
	{
		if (currentMessage != null)
		{
			try
			{
				currentMessage.setState(AirMessage.STATE_NONE);
				View view = lvMessage.findViewWithTag(msgCode);
				View unRead = lvMessage.findViewWithTag(msgCode + "unRead");
				if (unRead != null)
					unRead.setVisibility(View.GONE);
				if (view != null)
				{
					View pro = view.findViewById(R.id.loading);
					if (pro != null)
					{
						pro.clearAnimation();
						pro.setAnimation(null);
						pro.setVisibility(View.GONE);
					}
					ImageView record = (ImageView) view.findViewById(R.id.record_pic);
					if (record != null)
					{
						record.setVisibility(View.VISIBLE);
						// record.setSelected(true);
						record.setImageResource(ThemeUtil.getResourceId(R.attr.theme_msg_audio_stop, getActivity()));
					}
					currentMessage.setRecordTimer(currentMessage.getImageLength());
					// AirMmiTimer.getInstance().TimerRegister(this, this,
					// false, true, 1000, true, null);
				}
				sessionSp.edit().putInt(SESSION_EVENT_KEY, sessionSp.getInt(SESSION_EVENT_KEY, 1) + 1).commit();
			}
			catch (Exception e)
			{}

		}
		try
		{
			if (session.getMessagePlayback() != null && TextUtils.equals(session.getMessagePlayback().getImageUri(), resId))
			{
				PTTFragment.getInstance().refreshPlayback();
			}
		}
		catch (Exception e)
		{}
	}

	@Override
	public void onMessageRecordPlayStop(String msgCode, String resId)
	{

		// AirMmiTimer.getInstance().TimerUnregister(this, this);
		// if (SetRecordPlayState(msgCode, false))
		{
			try
			{
				Sound.playSound(Sound.PLAYER_MEDIAN_REC_PLAY_STOP, false, getActivity());
				View view = lvMessage.findViewWithTag(msgCode);
				if (view != null)
				{
					View pro = view.findViewById(R.id.loading);
					pro.clearAnimation();
					pro.setAnimation(null);
					pro.setVisibility(View.GONE);

					ImageView record = (ImageView) view.findViewById(R.id.record_pic);
					if (record != null)
					{
						record.setVisibility(View.VISIBLE);
						record.setImageResource(ThemeUtil.getResourceId(R.attr.theme_msg_audio_play, getActivity()));
					}
					TextView text = (TextView) view.findViewById(R.id.record_time);
					if (text != null)
					{
						AirMessage msg = adapterMessage.getMessageByCode(msgCode);
						if (msg != null)
							text.setText(msg.getImageLength() + "''");
					}
				}
				sessionSp.edit().putInt(SESSION_EVENT_KEY, sessionSp.getInt(SESSION_EVENT_KEY, 1) + 1).commit();
				PTTFragment.getInstance().refreshPlayback();
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
		}
	}

	@Override
	public void onMmiTimer(Context context, Object userData)
	{
		try
		{
			if (currentMessage != null)
			{
				currentMessage.minusRecordTimer();
				View v = lvMessage.findViewWithTag(currentMessage.getMessageCode());
				if (v != null)
				{
					TextView text = (TextView) v.findViewById(R.id.record_time);
					if (text != null)
					{
						text.setText(currentMessage.getRecordTimer() + "''");
					}
				}
				// if (session.getMessagePlayback() != null &&
				// TextUtils.equals(session.getMessagePlayback().getImageUri(),
				// currentMessage.getImageUri()))
				// {
				// recPlaybackSeconds.setText(currentMessage.getRecordTimer()
				// +"''");
				// }

			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public void onMessageRecordPtt(AirSession session, AirMessage message, String msgCode, String resId)
	{
		try
		{
			if (message != null && session != null && TextUtils.equals(session.getSessionCode(), message.getSessionCode()))
			{
				boolean toClean = false;
				if (HomeActivity.getInstance().pageIndex == HomeActivity.PAGE_IM)
				{
					toClean = true;
				}
				HomeActivity.getInstance().checkNewIM(toClean, null);
				SessionAndChannelView.getInstance().refreshChannelAndDialog();
				// refreshMessageNewCount(toClean);
				PTTFragment.getInstance().refreshPlayback();
				adapterMessage.notifyDataSetChanged();
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public void onMessageRecordStart()
	{
		try
		{
			Sound.playSound(Sound.PLAYER_MEDIAN_REC_PLAY_START, false, getActivity());
			mvRecording.registerMessage(MacRecordingView.START_TIME, null);
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public void onMessageRecordStop(int seconds, String msgCode)
	{
		try
		{
			mvRecording.registerMessage(MacRecordingView.STOP_TIME, recordCancel);
			switch (seconds)
			{
				case AirtalkeeMessage.REC_RESULT_OK:
					adapterMessage.notifyDataSetChanged();
					break;
				case AirtalkeeMessage.REC_RESULT_ERR_SMALL:
					Util.Toast(getActivity(), getString(R.string.talk_rec_result_err_small));
					break;
				case AirtalkeeMessage.REC_RESULT_ERROR:
					Util.Toast(getActivity(), getString(R.string.talk_rec_result_error));
					break;
				case AirtalkeeMessage.REC_RESULT_CANCEL:
					Util.Toast(getActivity(), getString(R.string.talk_rec_result_cancel_str));
					break;
			}
			adapterMessage.notifyDataSetChanged();
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public void onMessageRecordTransfered(String msgCode, String resId)
	{

	}

	@Override
	public void onMessageListLoad(String sessionCode, List<AirMessage> messages)
	{
		int position = 10;
		try
		{
			adapterMessage.notifyDataSetChanged();
			adapterMessage.notifyDataSetInvalidated();
			if (messages != null)
			{
				position = messages.size();
			}
			lvMessage.onRefreshComplete();
			lvMessage.setHaveMore(false);
			Log.i(HomeActivity.class, "IMFragment onResume position" + position);
			if (flag)
				lvMessage.setSelectionFromTop(position, 0);
			else
				lvMessage.setSelection(adapterMessage.getCount());
		}
		catch (Exception e)
		{}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		System.out.println("1");
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (v.getId() == R.id.btn_voice)
		{
			if (session != null)
			{
				if (session.getType() == AirSession.TYPE_CHANNEL)
				{
					if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
						recordSend(v, event);
					else
						Util.Toast(getActivity(), getString(R.string.talk_channel_idle));
				}
				else
					recordSend(v, event);
			}
			else
				Util.Toast(getActivity(), getString(R.string.talk_channel_idle));
		}

		return true;
	}

	private void recordSend(View v, MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN)
		{
			recordCancel = false;
			startY = event.getY();
			Sound.vibrate(20, getActivity());
			btnVoice.setImageResource(R.drawable.ic_voice_talk);
			if (session.getType() == AirSession.TYPE_DIALOG)
				AirtalkeeMessage.getInstance().MessageRecordStart(session, true);
			else
				AirtalkeeMessage.getInstance().MessageRecordStart(session.getSessionCode(), true);
		}
		else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL)
		{
			btnVoice.setImageResource(R.drawable.ic_voice_idle);
			mvRecording.registerMessage(MacRecordingView.STOP_TIME, recordCancel);
			AirtalkeeMessage.getInstance().MessageRecordStop(recordCancel);
		}
		else if (event.getAction() == MotionEvent.ACTION_MOVE)
		{
			if (Math.abs(event.getY() - v.getHeight()) > v.getHeight() + 100)
			{
				if (!recordCancel)
				{
					mvRecording.registerMessage(MacRecordingView.RECORD_CANCEL, null);
					v.setPressed(false);
					recordCancel = true;
				}
			}
			else if (event.getY() >= startY - 10)
			{
				if (recordCancel)
				{
					mvRecording.registerMessage(MacRecordingView.RECORD_OK, null);
					recordCancel = false;
				}
				v.setPressed(true);
			}
		}
	}

	/**
	 * 主要处理发送图片消息
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		try
		{
			switch (requestCode)
			{
				case REQUEST_CODE_BROWSE_IMAGE:
				{
					if (resultCode == Activity.RESULT_OK)
					{
						try
						{
							System.gc();
							Bundle bundleData = data.getExtras();
							List<String> pathList = bundleData.getStringArrayList("picPath");
							if (pathList != null && pathList.size() > 0)
							{
								for (int i = 0; i < pathList.size(); i++)
								{
									String path = pathList.get(i);
									Bitmap tempBitmap = null;
									try
									{
										byte[] bitmapData = AirServices.iOperator.readByteFile("", path, true);
										tempBitmap = PicFactory.getNormalMaxImage(bitmapData);
									}
									catch (OutOfMemoryError e)
									{
										return;
									}
									byte bphoto[] = null;
									ByteArrayOutputStream streamOut = new ByteArrayOutputStream();

									tempBitmap.compress(CompressFormat.JPEG, 80, streamOut);
									tempBitmap.recycle();
									tempBitmap = null;
									bphoto = streamOut.toByteArray();
									streamOut.reset();
									streamOut.close();
									streamOut = null;
									AirtalkeeMessage.getInstance().MessageImageSend(session, bphoto, true);
									bphoto = null;
									System.gc();
								}
							}
						}
						catch (Exception e)
						{}
					}
					else if (resultCode == Activity.RESULT_CANCELED)
					{
						if (data != null)
						{
							String type = data.getExtras().getString("type");
							if (type.equals("imVideo"))
							{
								String status = Environment.getExternalStorageState();
								if (!status.equals(Environment.MEDIA_MOUNTED))
								{
									Util.Toast(getActivity(), getActivity().getString(R.string.insert_sd_card));
									return;
								}
								Intent serverIntent = new Intent(getActivity(), VideoCamera.class);
								serverIntent.putExtra("videoType", 0);
								startActivityForResult(serverIntent, Const.image_select.REQUEST_CODE_CREATE_VIDEO);
							}
						}
					}
					break;
				}
				case Const.image_select.REQUEST_CODE_CREATE_VIDEO:
				{
					if (resultCode == Activity.RESULT_OK)
					{
						try
						{
							System.gc();
							Bundle bundleData = data.getExtras();
							String filePath = bundleData.getString(VideoCamera.EXTRA_VIDEO_PATH);
							if (filePath != null)
							{
								byte[] videoData = null;
								try
								{
									videoData = AirServices.iOperator.readByteFile("", filePath, true);
								}
								catch (OutOfMemoryError e)
								{}
								AirtalkeeMessage.getInstance().MessageVideoSend(getActivity(), filePath, session, "", videoData, true);
								System.gc();
							}
						}
						catch (Exception e)
						{}
					}
					else if (resultCode == Activity.RESULT_CANCELED)
					{
						if (data != null)
						{
							String type = data.getExtras().getString("type");
							if (type.equals("imCamera"))
							{
								String status = Environment.getExternalStorageState();
								if (!status.equals(Environment.MEDIA_MOUNTED))
								{
									Util.Toast(getActivity(), getActivity().getString(R.string.insert_sd_card));
									return;
								}
								Intent localIntent = new Intent(getActivity(), AlbumChooseActivity.class);
								localIntent.putExtra("type", AlbumChooseActivity.TYPE_IM);
								localIntent.putExtra("action", AlbumChooseActivity.ACTION_CAMERA);
								startActivityForResult(localIntent, REQUEST_CODE_BROWSE_IMAGE);
							}
						}
					}
					break;
				}
				default:
					break;
			}
		}
		catch (Exception e)
		{
			// TODO: handle exception
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{

	}
}
