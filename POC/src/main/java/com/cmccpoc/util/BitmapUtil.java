package com.cmccpoc.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

/**
 * Bitmap处理工具类
 * @author Yao
 */
public class BitmapUtil
{
	/**
	 * 获取图片Bitmap对象
	 * @param srcPath 路径
	 * @return Bitmap对象
	 */
	public static Bitmap getimage(String srcPath)
	{
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;// 这里设置高度为800f
		float ww = 480f;// 这里设置宽度为480f
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > ww)
		{// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		}
		else if (w < h && h > hh)
		{// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置缩放比例
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
	}

	/**
	 * 压缩图片
	 * @param image Bitmap对象
	 * @return 压缩后的图
	 */
	private static Bitmap compressImage(Bitmap image)
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
			int options = 100;
			while (baos.toByteArray().length / 1024 > 200)
			{ // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
				baos.reset();// 重置baos即清空baos
				image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
				options -= 5;// 每次都减少5
			}
			if (!image.isRecycled())
			{
				image.recycle();
			}
			ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中

			Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
			return bitmap;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 根据缩略的宽高比例和设定的最大宽高设置图片的宽高 长图会截取中间一段
	 * @param context 上下文
	 * @param key 路径
	 * @param width 宽度
	 * @param height 高度
	 * @return
	 */
	public static synchronized Bitmap readBitmap(Context context, String key, int width, int height)
	{
		File bitmapFile = null;
		Bitmap bitmap = null;
		boolean toLongOrWidth = false;
		boolean widthbigger = false;
		bitmapFile = new File(key);
		if (bitmapFile != null)
		{

			BitmapFactory.Options options = new Options();
			options.inJustDecodeBounds = true;
			try
			{
				BitmapFactory.decodeStream(new FileInputStream(bitmapFile), null, options);
				int outheight = options.outHeight;
				int outwidth = options.outWidth;
				float rate;
				if (outheight > outwidth)
				{
					rate = (float) outheight / (float) outwidth;
					if (rate > 2.0)
					{
						toLongOrWidth = true;
					}
					height = (int) (width * rate);
					widthbigger = false;
				}
				else
				{
					rate = (float) outwidth / (float) outheight;
					if (rate > 2.0)
					{
						toLongOrWidth = true;
					}
					width = (int) (height * rate);
					widthbigger = true;
				}

				int size = calculateInSampleSize(options, width, height);
				options.inSampleSize = size;
				options.inJustDecodeBounds = false;
				bitmap = BitmapFactory.decodeStream(new FileInputStream(bitmapFile), null, options);
				Bitmap dst = Bitmap.createScaledBitmap(bitmap, width, height, false);
				if (toLongOrWidth)
				{
					if (widthbigger)
					{
						int oldwidth = width;
						width = (int) ((float) height * (float) 1.77);
						dst = Bitmap.createBitmap(dst, (oldwidth - width) / 2, 0, width, height);

					}
					else
					{
						int oldheight = height;
						height = (int) ((float) width * (float) 1.77);
						dst = Bitmap.createBitmap(dst, 0, (oldheight - height) / 2, width, height);
					}
					Bitmap newb = Bitmap.createBitmap(dst.getWidth(), dst.getHeight(), Config.ARGB_8888);
					Canvas canvasTmp = new Canvas(newb);
					canvasTmp.drawColor(Color.TRANSPARENT);
					Paint p = new Paint();
					Typeface font = Typeface.create("宋体", Typeface.BOLD);
					p.setAntiAlias(true); // 设置画笔为无锯齿
					p.setColor(Color.WHITE);
					p.setTypeface(font);
					p.setTextSize(dp2px(context, 12));
					canvasTmp.drawBitmap(dst, 0, 0, p);
					Paint p2 = new Paint();
					p2.setColor(Color.parseColor("#77000000"));
					Rect r = new Rect(0, dst.getHeight() - dp2px(context, 24), dst.getWidth(), dst.getHeight());
					canvasTmp.drawRect(r, p2);
					canvasTmp.drawText("长图", dst.getWidth() / 2 - dp2px(context, 12), dst.getHeight() - dp2px(context, 6), p);
					canvasTmp.save(Canvas.ALL_SAVE_FLAG);
					canvasTmp.restore();
					dst = newb;
				}
				return dst;

			}
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return bitmap;
	}

	/**
	 * 估算大小
	 * @param options 选项
	 * @param reqWidth 宽
	 * @param reqHeight 高
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth)
		{

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
	
	/**
	 * dp 转换 px
	 * @param context 上下文
	 * @param dpValue dp值
	 * @return
	 */
	private static int dp2px(Context context, int dpValue)
	{
		return (int) context.getResources().getDisplayMetrics().density * dpValue;
	}
	
	
}
