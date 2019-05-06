package com.cmccpoc.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;
import com.cmccpoc.activity.AlbumChooseActivity;

/**
 * Bitmap缓存类
 * @author Yao
 */
public class BitmapCache extends Activity
{

	public Handler h = new Handler();
	public final String TAG = getClass().getSimpleName();
	private HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();

	/**
	 * 添加到Map列表
	 * @param path 路径
	 * @param bmp Bitmap对象
	 */
	public void put(String path, Bitmap bmp)
	{
		if (!TextUtils.isEmpty(path) && bmp != null)
		{
			imageCache.put(path, new SoftReference<Bitmap>(bmp));
		}
	}

	/**
	 * 展示图片Bitmap
	 * @param iv imageView控件
	 * @param thumbPath 缩略图路径
	 * @param sourcePath 资源路径
	 * @param callback
	 */
	public void displayBmp(final ImageView iv, final String thumbPath, final String sourcePath, final ImageCallback callback)
	{
		if (TextUtils.isEmpty(thumbPath) && TextUtils.isEmpty(sourcePath))
		{
			Log.e(TAG, "no paths pass in");
			return;
		}

		final String path;
		final boolean isThumbPath;
		if (!TextUtils.isEmpty(thumbPath))
		{
			path = thumbPath;
			isThumbPath = true;
		}
		else if (!TextUtils.isEmpty(sourcePath))
		{
			path = sourcePath;
			isThumbPath = false;
		}
		else
		{
			// iv.setImageBitmap(null);
			return;
		}

		if (imageCache.containsKey(path))
		{
			SoftReference<Bitmap> reference = imageCache.get(path);
			Bitmap bmp = reference.get();
			if (bmp != null)
			{
				if (callback != null)
				{
					callback.imageLoad(iv, bmp, sourcePath);
				}
				iv.setImageBitmap(bmp);
				Log.d(TAG, "hit cache");
				return;
			}
		}
		iv.setImageBitmap(null);

		new Thread()
		{
			Bitmap thumb;

			public void run()
			{

				try
				{
					if (isThumbPath)
					{
						thumb = BitmapFactory.decodeFile(thumbPath);
						if (thumb == null)
						{
							thumb = revitionImageSize(sourcePath);
						}
					}
					else
					{
						thumb = revitionImageSize(sourcePath);
					}
				}
				catch (Exception e)
				{

				}
				if (thumb == null)
				{
					thumb = AlbumChooseActivity.bimap;
				}
				Log.e(TAG, "-------thumb------" + thumb);
				put(path, thumb);

				if (callback != null)
				{
					h.post(new Runnable()
					{
						@Override
						public void run()
						{
							callback.imageLoad(iv, thumb, sourcePath);
						}
					});
				}
			}
		}.start();

	}

	/**
	 * 修正图片大小
	 * @param path 图片路径
	 * @return Bitmap对象
	 * @throws IOException IO异常
	 */
	public Bitmap revitionImageSize(String path) throws IOException
	{
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(new File(path)));
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, options);
		in.close();
		int i = 0;
		Bitmap bitmap = null;
		while (true)
		{
			if ((options.outWidth >> i <= 256) && (options.outHeight >> i <= 256))
			{
				in = new BufferedInputStream(new FileInputStream(new File(path)));
				options.inSampleSize = (int) Math.pow(2.0D, i);
				options.inJustDecodeBounds = false;
				bitmap = BitmapFactory.decodeStream(in, null, options);
				break;
			}
			i += 1;
		}
		return bitmap;
	}

	
	public interface ImageCallback
	{
		/**
		 * 图片加载
		 * @param imageView imageView控件
		 * @param bitmap Bitmap图
		 * @param params 多参数
		 */
		public void imageLoad(ImageView imageView, Bitmap bitmap, Object... params);
	}
}
