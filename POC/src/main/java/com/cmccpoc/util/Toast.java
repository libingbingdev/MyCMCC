package com.cmccpoc.util;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.cmccpoc.R;

/**
 * 自定义Toast弹窗
 * @author Yao
 */
public class Toast extends android.widget.Toast
{
	private static View toastView = null;
	private static TextView toastText = null;
	private static ImageView toastImg = null;
	private static ProgressBar toastPb = null;
	private static Toast toast = null;
	public static boolean isDebug=false;

	public Toast(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}

	public static Toast makeText1(Context context, int icon, String text, int duration)
	{
		return make(context, icon, text, duration);
	}

	public static Toast makeText1(Context context, String textString, int duration)
	{
		return make(context, 0, textString, duration);
	}

	public static Toast makeText1(Context context, boolean isProgressbar, String textString, int duration)
	{
		return make(context, isProgressbar, textString, duration);
	}

	/**
	 * 构建Toast
	 * @param context 上下文
	 * @param isProgressbar 显示loading标记
	 * @param textString 文本内容
	 * @param duration 显示时长
	 * @return
	 */
	private static Toast make(Context context, boolean isProgressbar, String textString, int duration)
	{
		if (context != null)
		{
			try
			{
				if (toastView == null || toastText == null)
				{
					toastView = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
					toastText = (TextView) toastView.findViewById(R.id.toast_text);
					toastImg = (ImageView) toastView.findViewById(R.id.toast_img);
					toastPb = (ProgressBar) toastView.findViewById(R.id.toast_pb);
				}

				toastText.setText(textString);
				toastImg.setVisibility(View.GONE);
				if (isProgressbar)
				{
					toastPb.setVisibility(View.VISIBLE);
				}
				else
				{
					toastPb.setVisibility(View.GONE);
				}
				if (toast == null)
				{
					toast = new Toast(context);
					toast.setGravity(Gravity.CENTER, 0, Gravity.TOP);
					toast.setMargin(0, 0);
				}

				toast.setDuration(duration);
				toast.setView(toastView);

			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
		}
		return toast;
	}

	/**
	 * 构建Toast
	 * @param context 上下文
	 * @param icon 图标
	 * @param textString 文本内容
	 * @param duration 显示时长
	 * @return Toast对象
	 */
	private static Toast make(Context context, int icon, String textString, int duration)
	{
		if (context != null)
		{
			try
			{
				if (toastView == null || toastText == null)
				{
					toastView = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
					toastText = (TextView) toastView.findViewById(R.id.toast_text);
					toastImg = (ImageView) toastView.findViewById(R.id.toast_img);
					toastPb = (ProgressBar) toastView.findViewById(R.id.toast_pb);
				}

				toastText.setText(textString);
				toastPb.setVisibility(View.GONE);
				if (icon != 0)
				{
					toastImg.setVisibility(View.VISIBLE);
					toastImg.setImageResource(icon);
				}
				else
				{
					toastImg.setVisibility(View.GONE);
				}
				if (toast == null)
				{
					toast = new Toast(context);
					toast.setGravity(Gravity.CENTER, 0, Gravity.TOP);
					toast.setMargin(0, 0);
				}

				toast.setDuration(duration);
				toast.setView(toastView);

			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
		}
		return toast;
	}

}
