package com.cmccpoc.widget;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.cmccpoc.activity.AlbumChooseActivity;
import com.cmccpoc.activity.MenuReportAsPicActivity;
import com.cmccpoc.config.Config;
import com.cmccpoc.listener.OnMmiVideoKeyListener;
import com.cmccpoc.receiver.ReceiverVideoKey;
import com.cmccpoc.util.Sound;
import com.cmccpoc.util.Toast;

/**
 * 上报图片时，选择拍照上传时显示的自定义Camera控件
 * 
 * @author Yao
 */
public class PhotoCamera extends Activity implements OnClickListener, Callback, OnMmiVideoKeyListener
{
	// 拍照按钮
	private ImageView mButtonStart;
	// 闪光灯
	private ImageView mButtonFlash;
	private ImageView tvClose;
	private ImageView mButtonToAlbum;
	private ImageView mButtonToVideo;
	private RelativeLayout rlTopbars;
	private RelativeLayout rlBottombars;
	private ImageView ivSure, ivClose;
	private String picPathTemp = "";

	private Camera camera;
	private SurfaceHolder surfaceHolder;
	private SurfaceView mSurfaceView;

	private int type;
	private String from;
	// private Context mContext;
	private boolean isTaking = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// 设置长亮
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// 没有标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.photo_camera);
		mButtonStart = (ImageView) findViewById(R.id.start);
		mButtonFlash = (ImageView) findViewById(R.id.flash);
		mSurfaceView = (SurfaceView) findViewById(R.id.surface);
		surfaceHolder = mSurfaceView.getHolder();
		tvClose = (ImageView) findViewById(R.id.close);
		ivClose = (ImageView) findViewById(R.id.bottom_close);
		ivSure = (ImageView) findViewById(R.id.sure);
		mButtonToAlbum = (ImageView) findViewById(R.id.to_album);
		mButtonToVideo = (ImageView) findViewById(R.id.to_camera);
		rlTopbars = (RelativeLayout) findViewById(R.id.topbars);
		rlTopbars.getBackground().setAlpha(80);
		rlBottombars = (RelativeLayout) findViewById(R.id.bottombars);
		rlBottombars.getBackground().setAlpha(80);

		mButtonToAlbum.setOnClickListener(this);
		mButtonToVideo.setOnClickListener(this);
		mButtonStart.setOnClickListener(this);
		// mButtonFlash.setOnClickListener(this);
		tvClose.setOnClickListener(this);
		ivClose.setOnClickListener(this);
		ivSure.setOnClickListener(this);

		surfaceHolder.addCallback(this);

		savedInstanceState = getIntent().getExtras();
		if (savedInstanceState != null)
		{
			picPathTemp = savedInstanceState.getString(MediaStore.EXTRA_OUTPUT);
			type = savedInstanceState.getInt("type");
			from = savedInstanceState.getString("from");
			// mContext = (Context) savedInstanceState.get("context");
		}
		/*
		 * if (type == AlbumChooseActivity.TYPE_IM) {
		 * mButtonToAlbum.setVisibility(View.INVISIBLE);
		 * mButtonToVideo.setVisibility(View.INVISIBLE); }
		 */
	}

	@Override
	protected void onResume() {
		super.onResume();
		ReceiverVideoKey.setOnMmiVideoKeyListener(this);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.flash:
				break;
			case R.id.start:
				if (camera != null)
				{
					if (!isTaking)
					{
						isTaking = true;
						mButtonStart.setClickable(false);
						mButtonStart.setEnabled(false);
						takePhoto();
					}
					else
					{
						if(Toast.isDebug) Toast.makeText1(this, "正在拍照...请勿重复点击", Toast.LENGTH_SHORT).show();
					}
				}
				break;
			case R.id.close:
			case R.id.bottom_close:
				setResult(RESULT_CANCELED);
				onVideoKeyPressed = false;
				finish();
				break;
			case R.id.sure:
			{
				switch (type)
				{
					case AlbumChooseActivity.TYPE_IM:
					{
						Intent data = new Intent();
						ArrayList<String> pathList = new ArrayList<String>();
						pathList.add(picPathTemp);
						data.putExtra("picPath", pathList);
						setResult(Activity.RESULT_OK, data);
						finish();
						break;
					}
					case AlbumChooseActivity.TYPE_REPORT:
					{
						Intent data = new Intent(this, MenuReportAsPicActivity.class);
						ArrayList<String> pathList = new ArrayList<String>();
						pathList.add(picPathTemp);
						data.putExtra("picPath", pathList);
						setResult(RESULT_OK, data);
						finish();
						break;
					}
				}
				break;
			}
			case R.id.to_album:
			{
				if (camera != null)
				{
					switch (type)
					{
						case AlbumChooseActivity.TYPE_IM:
						{
							if (from.equals("choose"))
							{
								setResult(RESULT_CANCELED);
							}
							else if (from.equals("enter"))
							{
								Intent it = new Intent();
								it.putExtra("type", "imAlbum");
								setResult(RESULT_CANCELED, it);
							}
							finish();
							break;
						}
						case AlbumChooseActivity.TYPE_REPORT:
						{
							camera.release();
							Intent itImage = new Intent();
							itImage.putExtra("type", "image");
							setResult(Activity.RESULT_CANCELED, itImage);
							finish();
							break;
						}
					}
				}
				break;
			}
			case R.id.to_camera:
			{
				if (camera != null)
				{
					switch (type)
					{
						case AlbumChooseActivity.TYPE_IM:
						{
							camera.release();
							Intent intent = new Intent();
							intent.putExtra("type", "imVideo");
							setResult(RESULT_CANCELED, intent);
							finish();
							break;
						}
						case AlbumChooseActivity.TYPE_REPORT:
						{
							camera.release();
							Intent it = new Intent(this, VideoCamera.class);
							it.putExtra("videoType", 1);
							startActivity(it);
							finish();
							break;
						}
					}
				}
				break;
			}
		}
	}

	private void takePhoto()
	{
		Parameters params = camera.getParameters();
		// 如果不是三星手机（S6）
		if (!Config.model.startsWith("SM"))
		{
			if (params.getFlashMode() != null)
				params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);// 自动闪光
		}
		params.setPictureFormat(ImageFormat.JPEG);// 图片格式
		List<Size> preSizes = params.getSupportedPreviewSizes();
		Size preSize = preSizes.get(0);
		params.setPreviewSize(preSize.width, preSize.height);
		params.setJpegQuality(100);
		List<Size> picSizes = params.getSupportedPictureSizes();
		Size picSize = null;
		String model = Config.model;
		if (model.contains("NXT-TL00")) // 华为MT8
			picSize = picSizes.get(5);
		else
			picSize = picSizes.get(1);
		params.setPictureSize(picSize.width, picSize.height);
		camera.setParameters(params);// 将参数设置到我的camera
		camera.autoFocus(new AutoFocusCallback()
		{
			@Override
			public void onAutoFocus(boolean success, Camera camera)
			{
				try
				{
					camera.takePicture(null, null, jpeg);
				}
				catch (Exception e)
				{
					Log.e(PhotoCamera.class, "PhotoCamera take photo and set params error");
					isTaking = false;
				}
				mButtonStart.setClickable(true);
				mButtonStart.setEnabled(true);
			}
		});
	}

	/**
	 * 刷新拍照按钮状态
	 * 
	 * @param state
	 *            状态
	 */
	public void refreshStartButton(int state)
	{
		switch (state)
		{
			case 0:
				mButtonStart.setImageResource(R.drawable.btn_report_video_stop);
				mButtonFlash.setVisibility(View.GONE);
				// tvClose.setVisibility(View.GONE);
				ivSure.setVisibility(View.GONE);
				mButtonToAlbum.setVisibility(View.GONE);
				mButtonToVideo.setVisibility(View.GONE);
				break;
			case 1:
				mButtonStart.setImageResource(R.drawable.btn_report_video_stop);
				mButtonStart.setVisibility(View.INVISIBLE);
				mButtonFlash.setVisibility(View.GONE);
				ivSure.setVisibility(View.VISIBLE);
				ivClose.setVisibility(View.VISIBLE);
				mButtonToAlbum.setVisibility(View.GONE);
				mButtonToVideo.setVisibility(View.GONE);
				rlTopbars.setVisibility(View.GONE);
				break;
		}
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (camera != null)
		{
			camera.release();
			camera = null;
		}
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		finish();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		try
		{
			if (camera == null)
				camera = Camera.open();
			if (camera != null)
			{
				camera.setPreviewDisplay(holder);
				camera.setDisplayOrientation(getPreviewDegree(PhotoCamera.this));// 设置相机方向
				camera.startPreview();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		
	}

	PictureCallback jpeg = new PictureCallback()
	{
		@Override
		public void onPictureTaken(byte[] data, Camera camera)
		{
			// 此处调用系统声音
			Sound.playSound(Sound.PLAYER_TAKE_PHOTO, PhotoCamera.this);
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			int preDegree = getPreviewDegree(PhotoCamera.this);
			bitmap = rotaingImageView(preDegree, bitmap);
			File file = new File(picPathTemp);
			BufferedOutputStream bos;
			try
			{
				bos = new BufferedOutputStream(new FileOutputStream(file));
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
				bos.write(data); // 写入sd卡中
				bos.close(); // 关闭输出流
				Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				Uri uri = Uri.fromFile(new File(picPathTemp));// 固定写法
				intent.setData(uri);
				sendBroadcast(intent);
				camera.stopPreview();
				camera.release();
				refreshStartButton(1);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				isTaking = false;
			}
		}
	};

	/**
	 * 获取预览界面的角度
	 * 
	 * @param activity 界面
	 * @return 角度
	 */
	public static int getPreviewDegree(Activity activity)
	{
		// 获得手机的方向
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degree = 0;
		// 根据手机的方向计算相机预览画面应该选择的角度
		switch (rotation)
		{
			case Surface.ROTATION_0:
				degree = 90;
				break;
			case Surface.ROTATION_90:
				degree = 0;
				break;
			case Surface.ROTATION_180:
				degree = 270;
				break;
			case Surface.ROTATION_270:
				degree = 180;
				break;
		}
		Log.i(PhotoCamera.class, "PhotoCamera PreviewDegree degree = " + degree);
		return degree;
	}

	/**
	 * 获取图片角度
	 * 
	 * @param path 图片路径
	 * @return 角度
	 */
	public int getPictureDegree()
	{
		int degree = 0;
		try
		{
			if (picPathTemp != null && !picPathTemp.equals(""))
			{
				ExifInterface exifInterface = new ExifInterface(picPathTemp);
				int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
				switch (orientation)
				{
					case ExifInterface.ORIENTATION_UNDEFINED:
						degree = 90;
						break;
					case ExifInterface.ORIENTATION_ROTATE_90:
						degree = 0;
						break;
					case ExifInterface.ORIENTATION_ROTATE_180:
						degree = 270;
						break;
					case ExifInterface.ORIENTATION_ROTATE_270:
						degree = 180;
						break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		Log.i(PhotoCamera.class, "PhotoCamera PictureDegree degree = " + degree);
		return degree;
	}

	/**
	 * 调整图片角度
	 * 
	 * @param angle 角度值
	 * @param bitmap Bitmap对象
	 * @return new bitmap
	 */
	public Bitmap rotaingImageView(int angle, Bitmap bitmap)
	{
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		// 创建新的图片
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}

	private boolean onVideoKeyPressed = false;
	@Override
	public void onVideoKey()
	{
	}
}
