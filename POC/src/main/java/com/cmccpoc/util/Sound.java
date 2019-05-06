package com.cmccpoc.util;

import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.Settings;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;

/************************************************
 * 
 * Sound handler
 * 
 ************************************************/

public class Sound
{

	public static final int PLAYER_IDS_MAX = 23;

	private static final int[] PLAYER_SOUNDS = { R.raw.sound_media_me_on, // PLAYER_MEDIA_ME_ON
		R.raw.sound_media_me_off, // PLAYER_MEDIA_ME_OFF
		R.raw.sound_media_other_on, // PLAYER_MEDIA_OTHER_ON
		R.raw.sound_media_other_off, // PLAYER_MEDIA_OTHER_OFF
		R.raw.sound_media_knock, // PLAYER_MEDIA_KNOCK
		R.raw.sound_media_error, // PLAYER_MEDIA_ERROR
		R.raw.sound_callbegin, // PALYER_CALL_BEGIN
		R.raw.sound_callend, // PALYER_CALL_END
		R.raw.sound_callerror, // PALYER_CALL_ERROR
		R.raw.sound_call_dial, // PLAYER_CALL_DIAL
		R.raw.sound_media_talk_prepare,
		R.raw.sound_media_rec_play_start, 
		R.raw.sound_media_rec_play_stop, 
		R.raw.sound_call_dial_incoming, 
		R.raw.sound_msg_sent, 
		R.raw.sound_pti,
		R.raw.sound_media_me_on_low,
		R.raw.sound_media_me_off_low,
		R.raw.sound_media_other_on_low,
		R.raw.sound_take_photo,// take photo
		R.raw.wangfei,
		R.raw.sound_sweep
		};

	public static final int PLAYER_MEDIA_ME_ON = 0;
	public static final int PLAYER_MEDIA_ME_OFF = 1;
	public static final int PLAYER_MEDIA_OTHER_ON = 2;
	public static final int PLAYER_MEDIA_OTHER_OFF = 3;
	public static final int PLAYER_MEDIA_KNOCK = 4;
	public static final int PLAYER_MEDIA_ERROR = 5;
	public static final int PLAYER_CALL_BEGIN = 6;
	public static final int PLAYER_CALL_END = 7;
	public static final int PLAYER_CALL_ERROR = 8;
	public static final int PLAYER_CALL_DIAL = 9;
	public static final int PLAYER_MEDIAN_TALK_PREPARE = 10;
	public static final int PLAYER_MEDIAN_REC_PLAY_START = 11;
	public static final int PLAYER_MEDIAN_REC_PLAY_STOP = 12;
	public static final int PLAYER_INCOMING_RING = 13;
	public static final int PLAYER_MSG_SENT = 14;
	public static final int PLAYER_PTI = 15;
	public static final int PLAYER_MEDIA_ME_ON_LOW = 16;
	public static final int PLAYER_MEDIA_ME_OFF_LOW = 17;
	public static final int PLAYER_MEDIA_OTHER_ON_LOW = 18;
	public static final int PLAYER_TAKE_PHOTO = 19;
	public static final int PLAYER_VOLUM_TEST=20;
	public static final int PLAYER_VOLUM_TEST2=21;
	public static final int PLAYER_NEWINFO = 22;

	public static MediaPlayer[] mediaPlayer = null;
	private static boolean alert = true;
	private static Vibrator vibrator = null;
	private static String currentSystemMusic = "";

	public static void setSoundAlert(boolean silent)
	{
		alert = silent;
	}

	public static boolean isAlert()
	{
		return alert;
	}

	/**
	 * 震动
	 * @param context 上下文
	 */
	public static void vibrate(Context context)
	{
		if (context != null)
		{
			vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
			if (vibrator != null)
				vibrator.vibrate(50);
		}
	}

	/**
	 * 震动
	 * @param msecond 震动时长
	 * @param context 上下文
	 */
	public static void vibrate(int msecond, Context context)
	{
		if (context != null)
		{
			vibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
			if (vibrator != null)
				vibrator.vibrate(msecond);
		}
	}

	public static void playSound(int playerId, Context context)
	{
		playSound(playerId, false, context,null);
	}

	public static void playSound(int playerId, Context context,OnCompletionListener listener)
	{
		playSound(playerId, false, context, listener);
	}
	public static void playSound(int playerId, boolean isLooping, Context context)
	{
		playSound(playerId, isLooping, context, null);
	}
	
	/**
	 * 播放音频
	 * @param playerId 音频资源Id
	 * @param isLooping 是否循环
	 * @param context
	 * @param listener
	 */
	public static void playSound(int playerId, boolean isLooping, Context context,OnCompletionListener listener)
	{
		try
		{
			boolean defaultPlayerId = false;
			Log.i(Sound.class, "startSound-begin");
			if (mediaPlayer == null)
			{
				mediaPlayer = new MediaPlayer[PLAYER_IDS_MAX];
			}
			Log.i(Sound.class, "startSound");
			switch (playerId)
			{
				case PLAYER_NEWINFO:
				case PLAYER_INCOMING_RING:
				{
					if (context != null)
					{
						String name = (playerId == PLAYER_INCOMING_RING) ? Settings.System.RINGTONE : Settings.System.NOTIFICATION_SOUND;
						String systemMuc = Settings.System.getString(context.getContentResolver(), name);
						if (mediaPlayer[playerId] == null || !systemMuc.equals(currentSystemMusic))
						{
							currentSystemMusic = systemMuc;
							mediaPlayer[playerId] = new MediaPlayer();
							String path = UriUtil.getPath(context, Uri.parse(currentSystemMusic));
							mediaPlayer[playerId].setDataSource(path);
							mediaPlayer[playerId].prepare();
						}
						if (mediaPlayer[playerId] != null)
						{
							mediaPlayer[playerId].start();
							mediaPlayer[playerId].setLooping(isLooping);
						}
					}
					break;
				}
				default:
				{
					defaultPlayerId = true;
					break;
				}
			}

			if (defaultPlayerId)
			{
				if (mediaPlayer[playerId] == null)
				{
					mediaPlayer[playerId] = MediaPlayer.create(context, PLAYER_SOUNDS[playerId]);
				}
				Log.i(Sound.class, "startSound() playerId=" + playerId);
				if (mediaPlayer[playerId] != null)
				{
					if (!mediaPlayer[playerId].isPlaying())
					{
						mediaPlayer[playerId].start();
						mediaPlayer[playerId].setLooping(isLooping);
						mediaPlayer[playerId].setOnCompletionListener(listener);
					}
				}
				else
				{
					Log.w(Sound.class, "startSound() playerId=" + playerId + "create failed!");
				}
			}
		}
		catch (Exception e)
		{
			Log.e(Sound.class, " Exception startSound��Error =" + e.toString());
		}
		// soundPlaying = false;
	}


	/**
	 * 停止播放
	 * @param playerId 音频资源Id
	 */
	public static void stopSound(int playerId)
	{
		try
		{
			if (mediaPlayer != null && mediaPlayer[playerId] != null && mediaPlayer[playerId].isPlaying())
			{
				mediaPlayer[playerId].stop();
			}
			mediaPlayer[playerId] = null;
		}
		catch (Exception e)
		{
			Log.e(Sound.class, " Exception stopSound��Error =" + e.toString());
		}
	}

	/**
	 * 音频是否在播放
	 * @param playerId 音频资源Id
	 * @return
	 */
	public static boolean soundIsPlaying(int playerId)
	{
		boolean playing = false;
		try
		{
			if (mediaPlayer != null && mediaPlayer[playerId] != null)
			{
				playing = true;
			}
		}
		catch (Exception e)
		{
			Log.e(Sound.class, " Exception stopSound��Error =" + e.toString());
		}
		return playing;
	}
}
