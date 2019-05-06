package com.cmccpoc.activity.home.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.airtalkee.sdk.entity.AirImage;
import com.cmccpoc.R;
import com.cmccpoc.control.AirReportManager;
import com.cmccpoc.entity.ImageItem;
import com.cmccpoc.util.BitmapCache;
import com.cmccpoc.util.BitmapCache.ImageCallback;
import com.cmccpoc.config.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图片列表 适配器
 * @author Yao
 */
public class AdapterPhoto extends BaseAdapter
{
	final String TAG = getClass().getSimpleName();
	private static final int TYPE_REPORT = 1;
	private static final int TYPE_IM = 2;
	Context mContext;
	private int type = TYPE_REPORT;
	List<ImageItem> dataList;// 图片列表
	public Map<Integer, String> map = new HashMap<Integer, String>();
	BitmapCache bitampCache;
	private Handler mHandler;
	private int selectTotal = 0;
	private int lastImagePosition = -1;// 记录上一次图片标志位，-1为空
	private Holder lastImageHolder;// 记录上一次图片标志holder
	private TextCallback textcallback = null;

	public static interface TextCallback
	{
		public void onTextListen(int count);
	}

	ImageCallback callback = new ImageCallback()
	{
		@Override
		public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params)
		{
			if (imageView != null && bitmap != null)
			{
				String url = (String) params[0];
				if (url != null && url.equals((String) imageView.getTag()))
				{
					((ImageView) imageView).setImageBitmap(bitmap);
				}
				else
				{
					Log.e(TAG, "callback, bmp not match");
				}
			}
			else
			{
				Log.e(TAG, "callback, bmp null");
			}
		}
	};

	public void setTextCallback(TextCallback listener)
	{
		textcallback = listener;
	}

	public AdapterPhoto(Context mContext, List<ImageItem> list, Handler mHandler)
	{
		this.mContext = mContext;
		dataList = list;
		bitampCache = new BitmapCache();
		this.mHandler = mHandler;
	}

	public AdapterPhoto(Context mContext, List<ImageItem> list, int type, Handler mHandler)
	{
		this(mContext, list, mHandler);
		this.type = type;
	}

	public void putSelections(List<AirImage> list)
	{
		if (list != null)
		{
			if (dataList != null)
			{
				for (int i = 0; i < dataList.size(); i ++)
				{
					for (int x = 0; x < list.size(); x ++)
					{
						if (TextUtils.equals(dataList.get(i).imagePath, list.get(x).getFileFullName()))
						{
							dataList.get(i).isSelected = true;
							map.put(i, list.get(x).getFileFullName());
						}
					}
				}
			}
			selectTotal += list.size();
		}
	}

	public int getSelectionCount()
	{
		return selectTotal;
	}

	@Override
	public int getCount()
	{
		int count = 0;
		if (null != dataList)
		{
			count = dataList.size();
		}
		return count;
	}

	@Override
	public Object getItem(int position)
	{
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		final Holder holder;
		if (convertView == null)
		{
			holder = new Holder();
			convertView = View.inflate(mContext, R.layout.listitem_photo, null);
			// convertView =View.inflate(mContext,R.layout.listitem_photo,null);
			holder.ivPhoto = (ImageView) convertView.findViewById(R.id.iv_album_picture);
			holder.cbSelected = (CheckBox) convertView.findViewById(R.id.cb_album_picture);
			holder.cbSelected.setVisibility(View.VISIBLE);
			holder.cbSelected.setEnabled(false);
			convertView.setTag(holder);
		}
		else
		{
			holder = (Holder) convertView.getTag();
		}
		final ImageItem item = (ImageItem) getItem(position);
		holder.ivPhoto.setTag(item.imagePath);
		bitampCache.displayBmp(holder.ivPhoto, item.thumbnailPath, item.imagePath, callback);
		holder.cbSelected.setChecked(!TextUtils.isEmpty(map.get(position)));
		holder.ivPhoto.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				imageClick(position, holder, item);
			}
		});
		return convertView;
	}

	class Holder
	{
		private ImageView ivPhoto;
		private CheckBox cbSelected;
	}

	/**
	 * 点击选中图片
	 * 若果是IM消息，则可以多选图片，最多9张
	 * 若是上报记录，则只能选中一张
	 * @param position 位置
	 * @param holder holder
	 * @param item 图片项目
	 */
	private void imageClick(final int position, final Holder holder, final ImageItem item)
	{
		String path = dataList.get(position).imagePath;
		if ((selectTotal) < AirReportManager.REPORT_IMAGE_MAX_CNT)
		{
			item.isSelected = !item.isSelected;
			if (item.isSelected)
			{
				holder.cbSelected.setChecked(true);
				selectTotal++;
				if (textcallback != null)
					textcallback.onTextListen(selectTotal);
				map.put(position, path);
			}
			else
			{
				holder.cbSelected.setChecked(false);
				selectTotal--;
				if (textcallback != null)
					textcallback.onTextListen(selectTotal);
				map.remove(position);
			}
		}
		else if ((selectTotal) >= AirReportManager.REPORT_IMAGE_MAX_CNT)
		{
			if (item.isSelected)
			{
				item.isSelected = !item.isSelected;
				holder.cbSelected.setChecked(false);
				selectTotal--;
				if (textcallback != null)
					textcallback.onTextListen(selectTotal);
				map.remove(position);
			}
			else
			{
				Message message = Message.obtain(mHandler, 0);
				message.sendToTarget();
			}
		}
	}

}
