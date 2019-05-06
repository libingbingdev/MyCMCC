package com.cmccpoc.activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.airtalkee.sdk.util.IOoperate;
import com.cmccpoc.R;
import com.cmccpoc.widget.ijkPlayer.IjkVideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class ActivityVideoPlayer extends Activity implements OnClickListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener
{
	public static final String PARAM_PATH = "path";
	public static final String PARAM_TYPE = "type";

	public static final int VIDEO_TYPE_LOCAL = 0;
	public static final int VIDEO_TYPE_URL = 1;

	private ImageView ivBack;
	private VideoView mVideoPlayer;
	private MediaController mVideoController;
	private View topPannel;
	private ProgressBar mVideoLoading;
	private TextView mVideoLoadingText;
	private boolean mVideoDownloading = false;
	private boolean mVideoRunning = false;
	
	private String videoPath;
	private int videoType = VIDEO_TYPE_LOCAL;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_player);
		savedInstanceState = getIntent().getExtras();
		if (savedInstanceState != null)
		{
			videoPath = savedInstanceState.getString("path");
			videoType = savedInstanceState.getInt("type", VIDEO_TYPE_LOCAL);
		}

		IjkMediaPlayer.loadLibrariesOnce(null);
		IjkMediaPlayer.native_profileBegin("libijkplayer.so");

		initView();
	}
	
	private void initView()
	{
		ivBack = (ImageView) findViewById(R.id.iv_close);
		ivBack.setOnClickListener(this);
		mVideoPlayer = (VideoView) findViewById(R.id.video_player);
		mVideoPlayer.setOnPreparedListener(this);
		mVideoPlayer.setOnCompletionListener(this);
		mVideoPlayer.setOnErrorListener(this);
		mVideoPlayer.setOnInfoListener(this);
		mVideoLoading = (ProgressBar) findViewById(R.id.video_loading);
		mVideoLoadingText = (TextView) findViewById(R.id.video_loading_text);
		if (videoType == VIDEO_TYPE_LOCAL)
			mVideoPlayer.setVideoPath(videoPath);
		else if (videoType == VIDEO_TYPE_URL)
		{
			//mVideoPlayer.setVideoPath(videoPath);
			mVideoLoading.setVisibility(View.VISIBLE);
			downloadRun();
		}
		mVideoController = new MediaController(this);
		mVideoPlayer.setMediaController(mVideoController);
		topPannel = findViewById(R.id.rl_video_top);
		//topPannel.setAlpha(200);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		mVideoRunning = true;
		if (!mVideoDownloading)
		{
			VideoPlay();
		}
	}
	
	@Override
	public void finish()
	{
		VideoStop();
		mVideoRunning = false;
		super.finish();
	}

	private void VideoPlay()
	{
		if (mVideoPlayer.isPlaying())
			mVideoPlayer.stopPlayback();
		mVideoPlayer.start();
	}

	private void VideoStop()
	{
		if (mVideoPlayer.isPlaying())
			mVideoPlayer.stopPlayback();

		IjkMediaPlayer.native_profileEnd();
	}

	//===================================================
	//
	// Video download
	//
	//===================================================

	private void downloadRun()
	{
		if (!mVideoDownloading)
		{
			mVideoDownloading = true;
			Runnable r = new Runnable()
			{
				public void run()
				{
					try
					{
						downloadDo();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			};
			new Thread(r).start();
		}
	}

	private void downloadDo()
	{
		mVideoDownloading = true;

		try
		{
			float datesum = 0;
			float downsum = 0.1f;
			int size;
			URL myURL = new URL(videoPath);
			FileOutputStream fos = null;

			IOoperate iop = new IOoperate();
			videoPath = IOoperate.FOLDER_PATH + IOoperate.VIDEO_PATH + "/videostore_cache.mp4";
			iop.deleteFile(videoPath);
			File file = new File(videoPath);

			fos = new FileOutputStream(file);
			URLConnection conn = myURL.openConnection();
			conn.connect();
			datesum = conn.getContentLength();
			InputStream is = conn.getInputStream();
			if (is != null)
			{
				byte buf[] = new byte[256];
				int count = 0;
				do
				{
					count++;
					int numread = is.read(buf);
					if (numread <= 0)
					{
						break;
					}
					fos.write(buf, 0, numread);
					downsum = file.length();
					size = (int) ((downsum / datesum) * 100);
					if ((count % 50) == 0)
					{
						Message msg = handle.obtainMessage();
						msg.what = 0;
						msg.arg1 = size;
						handle.sendMessage(msg);
					}
				}
				while (mVideoRunning);
				is.close();

				Message msg = handle.obtainMessage();
				msg.what = 1;
				msg.arg1 = 0;
				handle.sendMessage(msg);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		mVideoDownloading = false;
	}

	Handler handle = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
			if (msg.what == 0)
			{
				mVideoLoadingText.setText(msg.arg1 + "%");
			}
			else if (msg.what == 1)
			{
				mVideoPlayer.setVideoPath(videoPath);
				mVideoLoadingText.setText("");
				if (mVideoRunning)
					VideoPlay();
			}
		};
	};

	//===================================================


	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.iv_close:
				finish();
				break;
		}
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mVideoLoading.setVisibility(View.GONE);
	}

	@Override
	public void onCompletion(MediaPlayer mp) {

	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mVideoLoading.setVisibility(View.GONE);
		finish();
		return false;
	}

	@Override
	public boolean onInfo(MediaPlayer mp, int what, int extra) {
		return false;
	}
}
