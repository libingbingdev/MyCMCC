package com.cmccpoc.control;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.widget.ImageView;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.util.Log;

/**
 * poc语音通道管理器
 * @author Yao
 */
@SuppressLint("InlinedApi")
public class VoiceManager
{
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_TOAST = 5;

	private static VoiceManager instance;
	private BroadcastReceiver receiverBtConnectState = null;
	private BroadcastReceiver receiverBtState = null;

	boolean isReStart = false;

	private IOoperate io;
	private Context context;
	private int mode = AudioManager.MODE_NORMAL;

	private void setMode(int mode)
	{
		Log.d(VoiceManager.class, "voice: setMode mode=[" + mode + "]");
		this.mode = mode;
	}

	public int getMode()
	{
		return this.mode;
	}

	public interface OnBtScoChangeListener
	{
		public void onScoStateChange(int state);
	}

	public interface OnModeSetListener
	{
		public void onModeSet();
	}

	private VoiceManager()
	{
	}

	private VoiceManager(Context context, ImageView ivMode)
	{
		this.context = context;
		io = new IOoperate();
		setMode(io.getInt("mode", AudioManager.MODE_NORMAL));
	}

	public static VoiceManager newInstance(Context context)
	{
		newInstance(context, null);
		return instance;
	}

	public static VoiceManager newInstance(Context context, ImageView ivMode)
	{
		if (instance == null)
		{
			instance = new VoiceManager(context, ivMode);
		}
		return instance;
	}

	public void setModeContext(ImageView ivMode, Context context)
	{
		this.context = context;
	}

	public static VoiceManager getInstance()
	{
		return instance;
	}

	public void release()
	{
		if (context != null)
		{
			try
			{
				if (receiverBtState != null)
					context.unregisterReceiver(receiverBtState);
				if (receiverBtConnectState != null)
					context.unregisterReceiver(receiverBtConnectState);
			}
			catch (Exception e)
			{

			}
		}
	}

	/**
	 * 切换到听筒
	 */
	public void doChangeVoiceCall()
	{
		Log.d(VoiceManager.class, "voice:  doChangeVoiceCall");
		changeMode(AudioManager.MODE_IN_CALL);
	}

	/**
	 * 切换到扬声器
	 */
	public void doChangeSpeaker()
	{
		Log.d(VoiceManager.class, "voice:  doChangeSpeaker");
		changeMode(AudioManager.MODE_NORMAL);
	}

	private void changeMode(int mode)
	{
		Log.d(VoiceManager.class, "voice: changeMode mode=[" + mode + "]");
		setMode(mode);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// TODO Auto-generated method stub
	}


}
