package com.cmccpoc.activity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeAccount;
import com.airtalkee.sdk.AirtalkeeReport;
import com.airtalkee.sdk.entity.AirImage;
import com.airtalkee.sdk.util.IOoperate;
import com.airtalkee.sdk.util.Log;
import com.airtalkee.sdk.util.PicFactory;
import com.airtalkee.sdk.util.Utils;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.adapter.AdapterHorizontalListView;
import com.cmccpoc.activity.home.widget.AlertDialog.DialogListener;
import com.cmccpoc.activity.home.widget.ReportProgressAlertDialog;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirReportManager;
import com.cmccpoc.control.AirTaskCaseManager;
import com.cmccpoc.entity.AirTaskCase;
import com.cmccpoc.listener.OnMmiLocationListener;
import com.cmccpoc.listener.OnMmiReportListener;
import com.cmccpoc.location.AirLocation;
import com.cmccpoc.services.AirServices;
import com.cmccpoc.util.BitmapUtil;
import com.cmccpoc.util.Const;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Toast;
import com.cmccpoc.util.Util;
import com.cmccpoc.widget.HorizontialListView;
import com.cmccpoc.widget.PhotoCamera;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;


/**
 * 上报图片 主要包括：上传图片资源，可选压缩或高清的
 *
 * @author Yao
 */
public class MenuReportAsPicActivity extends ActivityBase implements
		OnClickListener, OnMmiLocationListener, OnCheckedChangeListener,
		DialogListener, OnMmiReportListener, AdapterHorizontalListView.OnAdapterHorizontalListViewListener
{

	private EditText report_detail;
	private ImageView report_image;
    private HorizontialListView hlImageList;
	private Button btn_post;
	private RadioGroup rgSelect;
	private RadioButton rbHigh, rbCompress;
	private boolean isUploading = false;

	private final String REPORT_FILE_ORG = "AIR-REPORT-";

    private AdapterHorizontalListView imagesAdapter;

	private boolean isHighQuality = false;

	private AirTaskCase mTaskCase = null;

	private String type = null;
	private String reportCode;
	ReportProgressAlertDialog reportDialog;
	public static boolean finishFlag;
	private static MenuReportAsPicActivity mInstance;

	/**
	 * 获取MenuReportAsPicActivity实例对象
	 *
	 * @return
	 */
	public static MenuReportAsPicActivity getInstance()
	{
		return mInstance;
	}

	private android.widget.Toast myToast = null;

	protected ImageLoader imageLoader = ImageLoader.getInstance();
	DisplayImageOptions options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.msg_image).showImageOnFail(R.drawable.msg_image).resetViewBeforeLoading(true).cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true).displayer(new FadeInBitmapDisplayer(300)).build();

	@Override
	protected void onCreate(Bundle bundle)
	{
		// TODO Auto-generated method stub
		super.onCreate(bundle);
		finishFlag = false;
		setRequestedOrientation(Config.screenOrientation);
		setContentView(R.layout.activity_tool_report_as_pic);
		doInitView();

		refreshUI();

		bundle = getIntent().getExtras();
		if (bundle != null)
		{
			String taskId = bundle.getString("taskId");
			if (!TextUtils.isEmpty(taskId))
				mTaskCase = AirTaskCaseManager.getInstance().getTask(taskId);
			type = bundle.getString("type");
		}
		loadCamera(type);
		mInstance = this;
		AirReportManager.getInstance().setReportListener(this);
	}

	/**
	 * 加载系统Camera
	 *
	 * @param type
	 *            类型：照相 or 相册
	 */
	private void loadCamera(String type)
	{
		if (type != null)
		{
			if (type.equals("camera"))
			{
				Intent it = new Intent(this, PhotoCamera.class);
				it.putExtra(MediaStore.EXTRA_OUTPUT, Util.getImageTempFileName(REPORT_FILE_ORG));
				it.putExtra("type", AlbumChooseActivity.TYPE_REPORT);
				startActivityForResult(it, Const.image_select.REQUEST_CODE_CREATE_IMAGE);
				// system photograph
				// Intent i = new
				// Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				// i.putExtra(MediaStore.EXTRA_OUTPUT, picUriTemp);
				// startActivityForResult(i,
				// Const.image_select.REQUEST_CODE_CREATE_IMAGE);
			}
			else if (type.equals("image"))
			{
				String status = Environment.getExternalStorageState();
				if (!status.equals(Environment.MEDIA_MOUNTED))
				{
					Util.Toast(this, getString(R.string.talk_insert_sd_card));
					return;
				}
				Intent localIntent = new Intent(this, AlbumChooseActivity.class);
				localIntent.putExtra("type", AlbumChooseActivity.TYPE_REPORT);
				// 调用系统相册
				// Intent localIntent = new
				// Intent("android.intent.action.GET_CONTENT", null);
				// localIntent.setType("image/*");
				startActivityForResult(localIntent, Const.image_select.REQUEST_CODE_BROWSE_IMAGE);
			}
		}
	}

	/**
	 * 初始化绑定控件Id
	 */
	private void doInitView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_tools_report_pic);
		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_back, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setVisibility(View.GONE);
		ivRightLay.setVisibility(View.INVISIBLE);
		findViewById(R.id.report_item_panel).setOnClickListener(this);

		report_detail = (EditText) findViewById(R.id.report_detail);
		report_image = (ImageView) findViewById(R.id.report_image);
        hlImageList = (HorizontialListView) findViewById(R.id.hl_image_list);
        imagesAdapter = new AdapterHorizontalListView(this, true);
		imagesAdapter.setListener(this);
        hlImageList.setAdapter(imagesAdapter);
		btn_post = (Button) findViewById(R.id.report_btn_post);
		rgSelect = (RadioGroup) findViewById(R.id.report_file_rg);
		rgSelect.setOnCheckedChangeListener(this);
		rbHigh = (RadioButton) findViewById(R.id.report_file_big);
		rbCompress = (RadioButton) findViewById(R.id.report_file_small);

		report_image.setOnClickListener(this);
		btn_post.setOnClickListener(this);
	}

	private final int HANDLER_ID_UI_REFRESH = 1;

	private Handler mUiHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if (msg.what == HANDLER_ID_UI_REFRESH)
			{
				refreshUI();
				removeDialog(R.id.talk_dialog_waiting);
				btn_post.setEnabled(true);
			}
		}
	};

	/**
	 * 刷新UI状态
	 */
	private void refreshUI()
	{
		try
		{
			if (isUploading)
			{
				AirImage img = AirReportManager.getInstance().mImages.get(0);
				report_detail.setEnabled(false);
				// report_image.setImageURI(picUri);
				if (img != null && img.getFileUri() != null)
					imageLoader.displayImage(img.getFileUri().toString(), report_image);
			}
			else
			{
				AirImage imgOrg = AirReportManager.getInstance().mImagesOrg.get(0);
				AirImage img = AirReportManager.getInstance().mImages.get(0);
				report_detail.setEnabled(true);

				int sizeTotalOrg = 0;
				int sizeTotalCmp = 0;
				for (int i = 0; i < AirReportManager.getInstance().mImagesOrg.size(); i ++)
					sizeTotalOrg += AirReportManager.getInstance().mImagesOrg.get(i).getSize();
				for (int i = 0; i < AirReportManager.getInstance().mImages.size(); i ++)
					sizeTotalCmp += AirReportManager.getInstance().mImages.get(i).getSize();
				if (sizeTotalCmp > sizeTotalOrg)
					sizeTotalCmp = sizeTotalOrg;
				report_image.setVisibility(View.GONE);
				hlImageList.setVisibility(View.VISIBLE);
				imagesAdapter.notifyList(AirReportManager.getInstance().mImages);
				if (imgOrg != null && imgOrg.getFileUri() != null) // 高清
					rbHigh.setText(getString(R.string.talk_tools_report_high) + "   " + MenuReportActivity.sizeMKB(sizeTotalOrg));
				if (img != null && img.getFileUri() != null) // 压缩
					rbCompress.setText(getString(R.string.talk_tools_report_compress) + "   " + MenuReportActivity.sizeMKB(sizeTotalCmp));
            }
        }
		catch (Exception e)
		{}
	}

	@Override
	public void finish()
	{
		super.finish();
		try
		{
			AirReportManager.getInstance().setReportListener(null);
			AirReportManager.getInstance().mImagesOrg.clear();
			AirReportManager.getInstance().mImages.clear();
			reportDialog.cancel();
			// AirServices.iOperator.deleteFile(picPath);
		}
		catch (Exception e)
		{}
	}

	protected Dialog onCreateDialog(int id)
	{
		if (id == R.id.talk_dialog_waiting)
		{
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage(getString(R.string.talk_tip_waiting_take_photo));
			dialog.setCancelable(false);
			return dialog;
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
			case R.id.menu_left_button:
			case R.id.bottom_left_icon:
				finish();
				break;
			case R.id.report_btn_post:
			{
				finishFlag = true;
				reportPost();
				break;
			}
			case R.id.report_image:
			{
				// pictureQualitySelect(v.getId());
				break;
			}
			case R.id.image_pic:
			{
				break;
			}
			case R.id.report_item_panel:
			{
				Util.hideSoftInput(this);
				break;
			}
		}
	}

	/**
	 * 开始上报
	 */
	public void reportPost()
	{
		if (isUploading)
		{
			Util.Toast(this, getString(R.string.talk_report_uploading));
			return;
		}
		if (AirReportManager.getInstance().mImagesOrg.size() == 0)
		{
			Util.Toast(this, getString(R.string.talk_report_upload_pic_err_select_pic));
			return;
		}
		isUploading = true;
		Util.hideSoftInput(this);
		refreshUI();

		myToast = Toast.makeText1(this, true, getString(R.string.talk_report_upload_getting_gps), Toast.LENGTH_LONG);
		myToast.show();
		Log.i(MenuReportAsPicActivity.class, "[REPORT-PIC] reportPost start");
		AirLocation.getInstance(this).onceGet(this, 30);
	}

	protected void onActivityResult(int requestCode, int resultCode, final Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode)
		{
			case Const.image_select.REQUEST_CODE_CREATE_IMAGE:
			{
				if (resultCode == RESULT_OK)
				{
					if (data != null)
					{
						Bundle bundleData = data.getExtras();
						final ArrayList<String> images = (ArrayList<String>)bundleData.get("picPath");
						showDialog(R.id.talk_dialog_waiting);
						btn_post.setEnabled(false);
						Thread m = new Thread()
						{
							@Override
							public void run()
							{
								putIntoList(images.get(0));

								Message msg = mUiHandler.obtainMessage();
								msg.what = HANDLER_ID_UI_REFRESH;
								mUiHandler.sendMessage(msg);
							}
						};
						m.start();
					}
				}
				else
				{
					if (data != null)
					{
						Bundle bundle = data.getExtras();
						if (bundle != null && "image".equals(bundle.getString("type")))
						{
							String status = Environment.getExternalStorageState();
							if (!status.equals(Environment.MEDIA_MOUNTED))
							{
								Util.Toast(this, getString(R.string.talk_insert_sd_card));
								return;
							}
							Intent localIntent = new Intent(this, AlbumChooseActivity.class);
							localIntent.putExtra("type", AlbumChooseActivity.TYPE_REPORT);
							startActivityForResult(localIntent, Const.image_select.REQUEST_CODE_BROWSE_IMAGE);
						}
					}
					else
					{
//						mImagesOrg.clear();
//						mImages.clear();
//						finish();
						if (AirReportManager.getInstance().mImagesOrg.size() == 0)
							finish();
					}
				}
				break;
			}
			case Const.image_select.REQUEST_CODE_BROWSE_IMAGE:
				if (resultCode == RESULT_OK)
				{
					Bundle bundleData = data.getExtras();
					final ArrayList<String> images = (ArrayList<String>)bundleData.get("picPath");
					//final boolean create = bundleData.getBoolean("create", false);
					if (images != null && images.size() > 0)
					{
						showDialog(R.id.talk_dialog_waiting);
						btn_post.setEnabled(false);
						Log.i(MenuReportAsPicActivity.class, "Selected images count = " + images.size());
						Thread m = new Thread()
						{
							@Override
							public void run()
							{
								for (int i = 0; i < images.size(); i ++)
								{
									boolean found = false;
									for (int x = 0; x < AirReportManager.getInstance().mImagesOrg.size(); x ++)
									{
										if (TextUtils.equals(AirReportManager.getInstance().mImagesOrg.get(x).getFileFullName(), images.get(i)))
										{
											found = true;
											break;
										}
									}
									if (!found)
										putIntoList(images.get(i));
								}

								Message msg = mUiHandler.obtainMessage();
								msg.what = HANDLER_ID_UI_REFRESH;
								mUiHandler.sendMessage(msg);
							}
						};
						m.start();
					}
					else
						Util.Toast(this, getString(R.string.talk_report_upload_pic_err_select_pic));
				}
				else
				{
					if (AirReportManager.getInstance().mImagesOrg.size() == 0)
						finish();
				}
				break;
			default:
				break;
		}
	}

	private void putIntoList(String image)
	{
		// Orignal Image
		//--------------------------------------
		String fileNameOrg = image;
		AirImage imgOrg = new AirImage();
		String date_string = "";
		String strLat = "";
		String strLng = "";
		try
		{
			ExifInterface exifInterface = new ExifInterface(image);
			if (!TextUtils.isEmpty(exifInterface.getAttribute(ExifInterface.TAG_DATETIME)))
				date_string = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);// 拍摄时间
			//String deviceName = exifInterface.getAttribute(ExifInterface.TAG_MAKE);// 设备品牌
			//String deviceModel = exifInterface.getAttribute(ExifInterface.TAG_MODEL); // 设备型号
			if (!TextUtils.isEmpty(exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE)))
				strLat = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
			if (!TextUtils.isEmpty(exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)))
				strLng = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
			String latRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
			String lngRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
			if (!TextUtils.isEmpty(strLat) && !TextUtils.isEmpty(latRef))
				strLat = "" + pictureConvertLocationToFloat(strLat, latRef);
			if (!TextUtils.isEmpty(strLng) && !TextUtils.isEmpty(lngRef))
				strLng = "" + pictureConvertLocationToFloat(strLng, lngRef);
			Log.i(MenuReportAsPicActivity.class,
					"Picture EXIF: (" + image + ") " +
							"TAG_DATETIME=" + date_string + " " +
							"TAG_GPS_LATITUDE=" + strLat + " TAG_GPS_LONGITUDE=" + strLng);
		}
		catch (Exception e)
		{
			Log.e(MenuReportAsPicActivity.class, "Picture EXIF: Get error (" + image + ")");
			e.printStackTrace();
		}
		if (TextUtils.isEmpty(date_string))
		{
			File file = new File(image);
			if (file != null)
			{
				Date date = new Date(file.lastModified());
				if (date != null)
				{
					SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					date_string = sfd.format(date);
					Log.i(MenuReportAsPicActivity.class, "Picture (" + image + "): ModifiedDate=" + date_string);
				}
			}
			if (TextUtils.isEmpty(date_string))
			{
				date_string = Utils.getDate() + " " + Utils.getTime();
				Log.i(MenuReportAsPicActivity.class, "Picture (" + image + "): CurrentDate=" + date_string);
			}
		}
		imgOrg.setExifDate(date_string);
		imgOrg.setExifLocLat(strLat);
		imgOrg.setExifLocLng(strLng);

		imgOrg.setFileFullName(fileNameOrg);
		imgOrg.setFileUri(Uri.fromFile(new File(imgOrg.getFileFullName())));
		imgOrg.setType(AirtalkeeReport.RESOURCE_TYPE_PICTURE);
		imgOrg.setSize(AirServices.iOperator.getFileSize("", imgOrg.getFileFullName(), true));
		AirReportManager.getInstance().mImagesOrg.add(imgOrg);

		// Comp Image
		//--------------------------------------
		AirImage img = new AirImage();
		img.setFileFullName(pictureNameGenerateForComp(image));
		img.setFileUri(Uri.fromFile(new File(img.getFileFullName())));
		img.setType(AirtalkeeReport.RESOURCE_TYPE_PICTURE);
		try
		{
			File file = new File(img.getFileFullName());
			if (!file.exists())
				pictureResize(imgOrg.getFileFullName(), img, 80);
		}
		catch (Exception e)
		{
		}
		img.setSize(AirServices.iOperator.getFileSize("", img.getFileFullName(), true));
		img.setExifDate(date_string);
		img.setExifLocLat(strLat);
		img.setExifLocLng(strLng);
		AirReportManager.getInstance().mImages.add(img);
	}

	private float pictureConvertLocationToFloat(String rationalString, String ref)
	{
		String[] parts = rationalString.split(",");

		String[] pair;
		pair = parts[0].split("/");
		double degrees = Double.parseDouble(pair[0].trim())
				/ Double.parseDouble(pair[1].trim());

		pair = parts[1].split("/");
		double minutes = Double.parseDouble(pair[0].trim())
				/ Double.parseDouble(pair[1].trim());

		pair = parts[2].split("/");
		double seconds = Double.parseDouble(pair[0].trim())
				/ Double.parseDouble(pair[1].trim());

		double result = degrees + (minutes / 60.0) + (seconds / 3600.0);
		if ((ref.equals("S") || ref.equals("W"))) {
			return (float) -result;
		}
		return (float) result;
	}

	private String pictureNameGenerateForComp(String OrgfileFullName)
	{
		String name = "";
		if (OrgfileFullName != null)
		{
			String filepath = IOoperate.FOLDER_PATH + IOoperate.IMAGES_PATH;
			String filename = "";
			int idx = OrgfileFullName.lastIndexOf('/');
			filename = OrgfileFullName.substring(idx+1, OrgfileFullName.length());
			name = filepath + "/COMP-" + filename;
			Log.i(MenuReportAsPicActivity.class, "[REPORT-PIC] OrgName: " + OrgfileFullName + " CompName: " + name);
		}
		return name;
	}

	private String pictureNameGenerateForMark(String OrgfileFullName)
	{
		String name = "";
		if (OrgfileFullName != null)
		{
			String filepath = IOoperate.FOLDER_PATH + IOoperate.IMAGES_PATH;
			String filename = "";
			int idx = OrgfileFullName.lastIndexOf('/');
			filename = OrgfileFullName.substring(idx+1, OrgfileFullName.length());
			name = filepath + "/MARK-" + filename;
			Log.i(MenuReportAsPicActivity.class, "[REPORT-PIC] OrgName: " + OrgfileFullName + " MarkName: " + name);
		}
		return name;
	}

	private void pictureMark(String fullName, String fullNameForMark)
	{
		if (AirServices.getInstance() != null)
		{
			byte[] picture = null;
			picture = AirServices.getInstance().iOperator.readByteFile("", fullName, true);
			if (picture != null)
			{
				try
				{
					Bitmap mark = BitmapFactory.decodeResource(getResources(), Config.app_icon_login);
					Bitmap img = PicFactory.getNormalImage(picture);
					if (mark != null)
					{
						Bitmap markScale = PicFactory.imageScale(mark, img.getWidth() / 2, (38 * img.getWidth() / 2) / 230);
						Bitmap water = PicFactory.createWaterMark(img, markScale);
						picture = PicFactory.Bitmap2Bytes(water, Bitmap.CompressFormat.JPEG, 100);
						markScale.recycle();
						water.recycle();
					}
					img.recycle();
					mark.recycle();
				}
				catch (Exception e)
				{
					picture = null;
				}
			}

			if (picture != null)
			{
				AirServices.getInstance().iOperator.imageWrite("", fullNameForMark, picture, false);
			}
		}
	}

	/**
	 * 计算图片大小
	 */
	private void pictureResize(String filePath, AirImage img, int quality)
	{
		if (!isHighQuality)
		{
			Bitmap picBitmap = BitmapUtil.getimage(filePath);
			if (picBitmap != null)
			{
				byte[] bitmapData = null;
				ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
				picBitmap.compress(CompressFormat.JPEG, quality, streamOut);
				bitmapData = streamOut.toByteArray();

				if (TextUtils.equals(filePath, img.getFileFullName()))
					AirServices.iOperator.deleteFile(img.getFileFullName());
				AirServices.iOperator.imageWrite("", img.getFileFullName(), bitmapData, false);

				// 刷新相册缓存
				Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				Uri uri = Uri.fromFile(new File(img.getFileFullName()));// 固定写法
				intent.setData(uri);
				sendBroadcast(intent);

				try
				{
					streamOut.reset();
					streamOut.close();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				streamOut = null;
				picBitmap.recycle();
				System.gc();
			}
		}
	}

	private void reportImage(int locType, double latitude, double longitude, double altitude, float speed, String time, String address)
	{
		if (isUploading)
		{
			if (myToast != null)
				myToast.cancel();
			List<AirImage> images = null;
			String size = "";
			int sizeTotalOrg = 0;
			int sizeTotalCmp = 0;
			for (int i = 0; i < AirReportManager.getInstance().mImagesOrg.size(); i ++)
				sizeTotalOrg += AirReportManager.getInstance().mImagesOrg.get(i).getSize();
			for (int i = 0; i < AirReportManager.getInstance().mImages.size(); i ++)
				sizeTotalCmp += AirReportManager.getInstance().mImages.get(i).getSize();
			if (sizeTotalCmp > sizeTotalOrg)
				sizeTotalCmp = sizeTotalOrg;
			if (isHighQuality)
			{
				images = AirReportManager.getInstance().mImagesOrg;
				size = MenuReportActivity.sizeMKB(sizeTotalOrg);
			}
			else
			{
				images = AirReportManager.getInstance().mImages;
				size = MenuReportActivity.sizeMKB(sizeTotalCmp);
			}

			String detail = report_detail.getText().toString();
			if (mInstance != null)
			{
				reportDialog = new ReportProgressAlertDialog(this, size);
				try
				{
					reportDialog.show();
				}
				catch (Exception e)
				{}
			}

			detail = report_detail.getText().toString();
			if (Config.marketCode != Config.MARKET_CODE_CMCC && !TextUtils.isEmpty(images.get(0).getExifDate()))
			{
				if (!TextUtils.isEmpty(detail))
					detail += "\r\n\r\n";
				detail += getString(R.string.talk_report_upload_capture_time) + " " + images.get(0).getExifDate();
			}

			String taskId = "";
			String taskCode = "";
			String taskName = "";
			String taskCar = "";
			if (mTaskCase != null)
			{
				taskId = mTaskCase.getTaskId();
				taskCode = mTaskCase.getCaseCode();
				taskName = mTaskCase.getCaseName();
				taskCar = mTaskCase.getCarNo();
			}

			Log.i(MenuReportAsPicActivity.class, "ReportPicture: TASK[" + taskId + "][" + taskName + "] imageSize=" + images.size() + " text=[" + report_detail.getText().toString() + "] x=[" + latitude + "] y=[" + longitude + "]");
			AirReportManager.getInstance().ReportMulti(taskId, images, detail, locType, latitude, longitude);
			isUploading = false;
		}
	}

	@Override
	public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time)
	{

	}

	@Override
	public void onLocationChanged(boolean isOk, int id, int type, double latitude, double longitude, double altitude, float speed, String time, String address)
	{
		Log.i(MenuReportAsPicActivity.class, "[REPORT-PIC] onLocationChanged start");
		if (id == AirLocation.AIR_LOCATION_ID_ONCE)
		{
			reportImage(type, latitude, longitude, altitude, speed, time, address);
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId)
	{
		int rid = group.getCheckedRadioButtonId();
		switch (rid)
		{
			case R.id.report_file_big:
			{
				//if (rbHigh.isChecked())
				{
					isHighQuality = true;
				}
				break;
			}
			case R.id.report_file_small:
			{
				//if (rbHigh.isChecked())
				{
					isHighQuality = false;
				}
				break;
			}
			default:
				break;
		}
	}

	@Override
	public void onClickOk(int id, Object obj)
	{
		this.finish();
	}

	@Override
	public void onClickOk(int id, boolean isChecked)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void onClickCancel(int id)
	{
		AirReportManager.getInstance().ReportRetry(reportCode);
	}

	@Override
	public void onMmiReportResourceListRefresh()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onMmiReportDel()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onMmiReportProgress(int progress)
	{
		reportDialog.setFileProgress(progress);
	}

	@Override
	public void OnAdapterHorizontalListViewItemClose(int position)
	{
		AirReportManager.getInstance().mImages.remove(position);
		AirReportManager.getInstance().mImagesOrg.remove(position);
		imagesAdapter.notifyDataSetChanged();
	}

	@Override
	public void OnAdapterHorizontalListViewItemAdd()
	{
		Intent it = new Intent(this, PhotoCamera.class);
		it.putExtra(MediaStore.EXTRA_OUTPUT, Util.getImageTempFileName(REPORT_FILE_ORG));
		it.putExtra("type", AlbumChooseActivity.TYPE_REPORT);
		startActivityForResult(it, Const.image_select.REQUEST_CODE_CREATE_IMAGE);
	}
}
