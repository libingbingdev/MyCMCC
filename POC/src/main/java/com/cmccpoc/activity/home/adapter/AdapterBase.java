package com.cmccpoc.activity.home.adapter;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.airtalkee.sdk.controller.MessageController;
import com.airtalkee.sdk.entity.AirMessage;
import com.airtalkee.sdk.util.Log;
import com.cmccpoc.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * 自定义基础适配器
 * @author Yao
 */
public abstract class AdapterBase extends BaseAdapter
{
	public static final int ORIENTATION_VERTICAL = 0;
	public static final int ORIENTATION_HORIZONTAL = 1;
	public static final int ORIENTATION_SQUARE = 2;

	protected interface OnImageLoadCompletedListener
	{
		/**
		 * 当图片加载完成时
		 * @param orientation 图片方向
		 * @param v view
		 * @param width 宽
		 * @param height 高
		 */
		public void onImageLoadCompleted(String imageUri, int orientation, View v, int width, int height);
	}

	DisplayImageOptions options;
	protected ImageLoader imageLoader = null;

	public AdapterBase()
	{
		// TODO Auto-generated constructor stub
		imageLoader = ImageLoader.getInstance();
		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.image_default)
				.showImageForEmptyUri(R.drawable.image_default)
				.showImageOnFail(R.drawable.image_default)
				.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.displayer(new RoundedBitmapDisplayer(0))
				.considerExifParams(true)
				.build();
	}

	/**
	 *  展示Url图片
	 * @param Url Url地址
	 * @param iv 图片View
	 * @param listener 
	 */
	public void displayImageByUrl(String Url, ImageView iv, final String messageCode, final OnImageLoadCompletedListener listener)
	{
		imageLoader.displayImage(Url, iv, options, new SimpleImageLoadingListener()
		{
			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason)
			{
			}
			
			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
			{
				// 图片加载完成后，重新设置图片方向与大小
				if (listener != null)
				{
					if (loadedImage != null)
					{
						int orientation = ORIENTATION_SQUARE;
						int width = loadedImage.getWidth();
						int height = loadedImage.getHeight();
						if (width > height)
						{
							orientation = ORIENTATION_HORIZONTAL;
						}
						else if (width == height)
						{
							orientation = ORIENTATION_SQUARE;
						}
						else
						{
							orientation = ORIENTATION_VERTICAL;
						}

						Log.i(AdapterBase.class, "onLoadingComplete width" + loadedImage.getWidth() + "height" + loadedImage.getHeight());
						listener.onImageLoadCompleted(imageUri, orientation, view, width, height);
					}
				}

			}

			@Override
			public void onLoadingCancelled(String imageUri, View view)
			{
				// TODO Auto-generated method stub
			}
		});
	}

}
