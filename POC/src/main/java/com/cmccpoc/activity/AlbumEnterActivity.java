package com.cmccpoc.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cmccpoc.R;
import com.cmccpoc.activity.home.adapter.AdapterPhoto;
import com.cmccpoc.activity.home.adapter.AdapterPhoto.TextCallback;
import com.cmccpoc.config.Config;
import com.cmccpoc.control.AirReportManager;
import com.cmccpoc.entity.ImageItem;
import com.cmccpoc.util.AlbumHelper;
import com.cmccpoc.util.Bimp;
import com.cmccpoc.util.Const;
import com.cmccpoc.util.ThemeUtil;
import com.cmccpoc.util.Util;
import com.cmccpoc.widget.PhotoCamera;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 照片列表
 在AlbumChooseActivity中，点击一个相册，即可进入当前Activity
 当前列表在初始化时会判断是要发送IM消息，还是要上报记录，这两个用的同一个图片列表与相册列表
 @author Yao */
public class AlbumEnterActivity extends Activity implements OnClickListener
{
    public static final String EXTRA_IMAGE_LIST = "imagelist";
    private final String KEYSTR = "picPath";
    private static final int TYPE_REPORT = 1;
    private static final int TYPE_IM = 2;

    List<ImageItem> dataList;
    GridView gridView;
    AdapterPhoto adapter;
    AlbumHelper helper;
    private Button btSend;
    private int type = TYPE_REPORT;
    // private Uri picUriTemp = null; // 原图uri
    private String picPathTemp = ""; // 原图path

    Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 0:
                    Toast.makeText(AlbumEnterActivity.this, "最多选择" + AirReportManager.REPORT_IMAGE_MAX_CNT + "张图片", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_enter);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null)
        {
            type = bundle.getInt("type");
        }
        helper = AlbumHelper.getHelper();
        helper.init(this);
        dataList = (List<ImageItem>) getIntent().getSerializableExtra(EXTRA_IMAGE_LIST);
        initView();
    }

    /**
     初始化view视图
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

        btSend = (Button) findViewById(R.id.bt_photo_confirm);
        btSend.setOnClickListener(this);
        if (type == TYPE_IM)
        {
            btSend.setText(getString(R.string.talk_photo_unselected_tip));
        }
        else if (type == TYPE_REPORT)
        {
            btSend.setText(getString(R.string.talk_photo_unselected_tip));
        }

        gridView = (GridView) findViewById(R.id.gv_pictures);
        adapter = new AdapterPhoto(this, dataList, type, mHandler);
        adapter.setTextCallback(new TextCallback()
        {
            @Override
            public void onTextListen(int count)
            {
                switch (type)
                {
                    case TYPE_IM:
                    {
                        if (count > 0)
                        {
                            btSend.setText(getString(R.string.talk_photo_selected_tip) + "(" + count + ")");
                            btSend.setBackground(getResources().getDrawable(R.drawable.selector_button_album));
                        }
                        else
                        {
                            btSend.setText("未选择");
                            btSend.setBackground(getResources().getDrawable(R.drawable.bg_album_gray));
                        }

                        break;
                    }
                    case TYPE_REPORT:
                    {
                        if (count > 0)
                        {
                            btSend.setText("确认");
                            btSend.setBackground(getResources().getDrawable(R.drawable.selector_button_album));
                        }
                        else
                        {
                            btSend.setText("未选择");
                            btSend.setBackground(getResources().getDrawable(R.drawable.bg_album_gray));
                        }
                        break;
                    }
                }
            }
        });
        if (type == TYPE_REPORT)
            adapter.putSelections(AirReportManager.getInstance().mImagesOrg);
        if (adapter.getSelectionCount() > 0)
        {
            if (type == TYPE_IM)
            {
                btSend.setText(getString(R.string.talk_photo_selected_tip) + "(" + adapter.getSelectionCount() + ")");
                btSend.setBackground(getResources().getDrawable(R.drawable.selector_button_album));
            }
            else if (type == TYPE_REPORT)
            {
                btSend.setText("确认");
                btSend.setBackground(getResources().getDrawable(R.drawable.selector_button_album));
            }
        }

        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.bt_photo_confirm:
            {
                Collection<String> c = adapter.map.values();
                Intent data = new Intent();
                if (c.size() > 0)
                {
                    ArrayList<String> list = new ArrayList<String>();
                    Iterator<String> it = c.iterator();
                    while (it.hasNext())
                    {
                        list.add(it.next());
                    }
                    if (Bimp.act_bool)
                    {
                        setResult(Activity.RESULT_OK);
                        Bimp.act_bool = false;
                    }
                    for (int i = 0; i < list.size(); i++)
                    {
                        if (Bimp.bmp.size() <= AirReportManager.REPORT_IMAGE_MAX_CNT)
                        {
                            try
                            {
                                Bitmap bm = Bimp.revitionImageSize(list.get(i));
                                Bimp.bmp.add(bm);
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    data.putExtra(KEYSTR, list);
                    setResult(Activity.RESULT_OK, data);
                    finish();
                }
                else
                {
                    com.cmccpoc.util.Toast.makeText1(this, "请至少选择一张图片", Toast.LENGTH_LONG).show();
                    return;
                }
                adapter.map.clear();
                break;
            }
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
                    itCamera.putExtra("from", "enter");
                    startActivityForResult(itCamera, Const.image_select.REQUEST_CODE_CREATE_IMAGE);
                }
                else if (type == TYPE_REPORT)
                {
                    picPathTemp = Util.getImageTempFileName();
                    Intent itCamera = new Intent(this, PhotoCamera.class);
                    itCamera.putExtra(MediaStore.EXTRA_OUTPUT, picPathTemp);
                    itCamera.putExtra("type", TYPE_REPORT);
                    itCamera.putExtra("from", "enter");
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
        switch (requestCode)
        {
            // 自定义相机
            case Const.image_select.REQUEST_CODE_CREATE_IMAGE:
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    setResult(Activity.RESULT_OK, data);
                    finish();
                }
                else if (resultCode == Activity.RESULT_CANCELED)
                {
                    if (data != null)
                    {
                        String type = data.getExtras().getString("type");
                        if ("imVideo".equals(type))
                        {
                            setResult(Activity.RESULT_CANCELED, data);
                        }
                        else if ("imAlbum".equals(type))
                        {
                            setResult(Activity.RESULT_CANCELED, data);
                        }
                        finish();
                    }
                }
                break;
            }
        }
    }
}
