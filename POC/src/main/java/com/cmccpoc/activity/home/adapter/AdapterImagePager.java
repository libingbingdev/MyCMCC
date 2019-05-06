package com.cmccpoc.activity.home.adapter;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.cmccpoc.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * IM消息--图片消息原图展示容器
 * 
 * @author Yao
 */
public class AdapterImagePager extends PagerAdapter
{
	private Context mContext;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private List<String> images;
	private LayoutInflater inflater;
	DisplayImageOptions options;

	public AdapterImagePager(List<String> images, Context context)
	{
		this.images = images;
		this.mContext = context;
		inflater = LayoutInflater.from(context);
		options = new DisplayImageOptions.Builder().showImageForEmptyUri(R.drawable.msg_image).showImageOnFail(R.drawable.msg_image).resetViewBeforeLoading(true).cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY).bitmapConfig(Bitmap.Config.RGB_565).considerExifParams(true).displayer(new FadeInBitmapDisplayer(300)).build();
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		container.removeView((View) object);
	}

	@Override
	public int getCount()
	{
		return images.size();
	}

	@Override
	public Object instantiateItem(ViewGroup view, int position)
	{
		View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
		assert imageLayout != null;
		ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
		final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);

		// String url = images.get(position).contains("http://") ?
		// images.get(position) : Util.getPhotoUrl(images.get(position));
		String url = images.get(position);

		imageLoader.displayImage(url, imageView, options, new SimpleImageLoadingListener()
		{
			@Override
			public void onLoadingStarted(String imageUri, View view)
			{
				spinner.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason)
			{
				/*
				 * String message = null; switch (failReason.getType()) { case
				 * IO_ERROR: message = "Input/Output error"; break; case
				 * DECODING_ERROR: message = "Image can't be decoded"; break;
				 * case NETWORK_DENIED: message = "Downloads are denied"; break;
				 * case OUT_OF_MEMORY: message = "Out Of Memory error"; break;
				 * case UNKNOWN: message = "Unknown error"; break; } //
				 * Toast.makeText(ImagePagerActivity.this, // message,
				 * Toast.LENGTH_SHORT).show();
				 */
				spinner.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage)
			{
				spinner.setVisibility(View.GONE);
			}
		});

		view.addView(imageLayout, 0);
		imageView.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				((Activity) mContext).finish();
			}
		});
		return imageLayout;
	}

	@Override
	public boolean isViewFromObject(View view, Object object)
	{
		return view.equals(object);
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader)
	{
	}

	@Override
	public Parcelable saveState()
	{
		return null;
	}
}
