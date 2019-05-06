package com.cmccpoc.activity;

import java.io.Serializable;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.adapter.AdapterAlbum;
import com.cmccpoc.entity.ImageBucket;
import com.cmccpoc.util.AlbumHelper;
import com.cmccpoc.util.Const;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Util;
import com.cmccpoc.widget.PhotoCamera;

/**
 * 相册选择Activity。
 * 获取系统相册的内容，赋值到当前自定义相册列表中
 * @author Yao
 */
public class AlbumChooseActivity extends Activity implements OnItemClickListener, OnClickListener
{
	public static final String EXTRA_IMAGE_LIST = "imagelist";
	public static final int TYPE_REPORT = 1;
	public static final int TYPE_IM = 2;
	public static final int ACTION_ALBUM = 1;
	public static final int ACTION_CAMERA = 2;
	
	List<ImageBucket> dataList;
	GridView gridView;
	AdapterAlbum adapter;
	AlbumHelper helper;
	private int type = TYPE_REPORT;
	private int action = ACTION_ALBUM;
	public static Bitmap bimap;
	private Uri picUriTemp = null; // 原图uri
	private String picPathTemp = ""; // 原图path
	private boolean toCamera = false;

	private static AlbumChooseActivity mInstance;

	public static AlbumChooseActivity getInstance()
	{
		return mInstance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_album_choose);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null)
		{
			type = bundle.getInt("type", TYPE_REPORT);
			action = bundle.getInt("action", ACTION_ALBUM);
		}
		helper = AlbumHelper.getHelper();
		helper.init(this);
		initData();
		initView();
		mInstance = this;
		if (action == ACTION_CAMERA)
		{
			picPathTemp = Util.getImageTempFileName();
			// picUriTemp = Uri.fromFile(new File(picPathTemp));
			Intent itCamera = new Intent(this, PhotoCamera.class);
			itCamera.putExtra(MediaStore.EXTRA_OUTPUT, picPathTemp);
			itCamera.putExtra("type", TYPE_IM);
			itCamera.putExtra("from", "choose");
			startActivityForResult(itCamera, Const.image_select.REQUEST_CODE_CREATE_IMAGE);
		}
	}

	/**
	 * 初始化相册列表
	 */
	private void initData()
	{
		dataList = helper.getImagesBucketList(true);
		bimap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_addpic_unfocused);
	}

	/**
	 * 初始化view视图
	 */
	private void initView()
	{
		TextView ivTitle = (TextView) findViewById(R.id.tv_main_title);
		ivTitle.setText(R.string.talk_album);

		View btnLeft = findViewById(R.id.menu_left_button);
		ImageView ivLeft = (ImageView) findViewById(R.id.bottom_left_icon);
		ivLeft.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_close, this));
		btnLeft.setOnClickListener(this);

		RelativeLayout ivRightLay = (RelativeLayout) findViewById(R.id.talk_menu_right_button);
		ImageView ivRight = (ImageView) findViewById(R.id.bottom_right_icon);
		ivRight.setImageResource(ThemeUtil.getResourceId(R.attr.theme_ic_topbar_camera, this));
		ivRightLay.setOnClickListener(this);

		gridView = (GridView) findViewById(R.id.gv_albums);
		// adapter = new AdapterAlbum(this, dataList,type);
		adapter = new AdapterAlbum(this, dataList);
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(this);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		Intent intent = new Intent(this, AlbumEnterActivity.class);
		intent.putExtra("type", type);
		intent.putExtra(EXTRA_IMAGE_LIST, (Serializable) dataList.get(position).imageList);
		startActivityForResult(intent, Const.image_select.REQUEST_CODE_BROWSE_IMAGE);
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.menu_left_button:
			{
				finish();
				break;
			}
			case R.id.talk_menu_right_button:
			{
				if (type == TYPE_IM)
				{
					picPathTemp = Util.getImageTempFileName();
					Intent itCamera = new Intent(this, PhotoCamera.class);
					itCamera.putExtra(MediaStore.EXTRA_OUTPUT, picPathTemp);
					itCamera.putExtra("type", TYPE_IM);
					itCamera.putExtra("from", "choose");
					startActivityForResult(itCamera, Const.image_select.REQUEST_CODE_CREATE_IMAGE);
				}
				else if (type == TYPE_REPORT)
				{
					toCamera = true;
					picPathTemp = Util.getImageTempFileName();
					Intent itCamera = new Intent(this, PhotoCamera.class);
					itCamera.putExtra(MediaStore.EXTRA_OUTPUT, picPathTemp);
					itCamera.putExtra("type", TYPE_REPORT);
					itCamera.putExtra("from", "choose");
					startActivityForResult(itCamera, Const.image_select.REQUEST_CODE_CREATE_IMAGE);
				}
				break;
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK)
		{
			boolean create = false;
			Bundle bundleData = data.getExtras();
			if (bundleData != null)
				create = bundleData.getBoolean("create", false);
			data.putExtra("create", toCamera || create);
			setResult(Activity.RESULT_OK, data);
			finish();
		}
		else if (resultCode == RESULT_CANCELED)
		{
			if (data != null)
			{
				String type = data.getStringExtra("type");
				if (type.equals("imVideo"))
				{
					data.putExtra("type", type);
					setResult(RESULT_CANCELED, data);
					finish();
				}
				else if ("imAlbum".equals(type))
				{
					return;
				}
			}
		}
		else
		{
			return;
		}
		/*
		 * switch (requestCode) { // 自定义相册 case
		 * Const.image_select.REQUEST_CODE_BROWSE_IMAGE: // 自定义相机 case
		 * Const.image_select.REQUEST_CODE_CREATE_IMAGE: { // ArrayList<String>
		 * pathList = new ArrayList<String>(); // pathList.add(picPathTemp); //
		 * data.putExtra("picPath", pathList); setResult(Activity.RESULT_OK,
		 * data); finish(); break; } }
		 */
	}
}
