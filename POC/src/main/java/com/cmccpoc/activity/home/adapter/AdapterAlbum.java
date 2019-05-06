package com.cmccpoc.activity.home.adapter;

import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.cmccpoc.R;
import com.cmccpoc.entity.ImageBucket;
import com.cmccpoc.util.BitmapCache;
import com.cmccpoc.util.BitmapCache.ImageCallback;

/**
 * 自定义相册 适配器
 * @author Yao
 */
public class AdapterAlbum extends BaseAdapter
{
	final String TAG = getClass().getSimpleName();
	public static final int TYPE_REPORT = 1;
	public static final int TYPE_IM = 2;

	Context mContext;
	private int type = TYPE_REPORT;
	// 图片集列表
	List<ImageBucket> dataList;
	BitmapCache bitampCache;
	ImageCallback callback = new ImageCallback()
	{
		@Override
		public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params)
		{
			if (null != imageView && null != bitmap)
			{
				String url = params[0].toString();
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

	public AdapterAlbum(Context mContext, List<ImageBucket> list)
	{
		this.mContext = mContext;
		dataList = list;
		bitampCache = new BitmapCache();
	}

	public AdapterAlbum(Context mContext, List<ImageBucket> list, int type)
	{
		this(mContext, list);
		this.type = type;
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
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		Holder holder;
		if (convertView == null)
		{
			holder = new Holder();
			convertView = View.inflate(mContext, R.layout.listitem_album, null);
			// convertView = View.inflate(mContext, R.layout.listitem_photo,
			// null);
			holder.ivPhoto = (ImageView) convertView.findViewById(R.id.iv_album_image);
			holder.tvName = (TextView) convertView.findViewById(R.id.tv_album_name);
			holder.tvCount = (TextView) convertView.findViewById(R.id.tv_album_count);
			convertView.setTag(holder);
		}
		else
		{
			holder = (Holder) convertView.getTag();
		}
		final ImageBucket item = (ImageBucket) getItem(position);
		holder.tvCount.setText(item.count + "");
		holder.tvName.setText(item.bucketName);
		if (item.imageList != null && item.imageList.size() > 0)
		{
			String thumbPath = item.imageList.get(0).thumbnailPath;
			String sourcePath = item.imageList.get(0).imagePath;
			holder.ivPhoto.setTag(sourcePath);
			bitampCache.displayBmp(holder.ivPhoto, thumbPath, sourcePath, callback);
		}
		else
		{
			holder.ivPhoto.setImageBitmap(null);
		}
		return convertView;
	}

	class Holder
	{
		private ImageView ivPhoto;
		private TextView tvName;
		private TextView tvCount;
	}

}
