package com.cmccpoc.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.LineHeightSpan;
import android.text.style.ReplacementSpan;
import com.cmccpoc.R;

/**
 * 表情符号帮助类
 * @author Yao
 */
public class Smilify
{
	private boolean m_isfontHeight;

	/**
	 * 内部类:表情容器
	 * @author Yao
	 */
	private final class SmileySpan extends ReplacementSpan implements LineHeightSpan
	{
		private String mSmiley;
		private Drawable drawable;
		private String[] array;

		public SmileySpan(String smileyCode)
		{
			mSmiley = smileyCode.toLowerCase();
			drawable = getSmileyDrawable();
		}

		@SuppressWarnings("unused")
		public SmileySpan(String smileyCode, int index)
		{
			mSmiley = smileyCode.toLowerCase();
			drawable = getSmileyDrawable2(index);
		}

		@Override
		public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint)
		{
			try
			{
				if (drawable != null)
				{
					int height = drawable.getIntrinsicHeight();
					int width = drawable.getIntrinsicWidth();
					if (m_isfontHeight)
					{
						height = (int) (paint.descent() - paint.ascent());
						width = height;
					}
					int baseline = y + (int) paint.descent();

					drawable.setBounds((int) x, baseline - height, (int) x + width, baseline);
					drawable.draw(canvas);
				}
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
		}

		@Override
		public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm)
		{
			//			Drawable drawable = getSmileyDrawable();
			if (drawable == null)
			{
				return 0;
			}
			else
			{
				int height = drawable.getIntrinsicHeight();
				int width = drawable.getIntrinsicWidth();
				if (m_isfontHeight)
				{
					height = (int) (paint.descent() - paint.ascent());
					width = height;
				}
				return width;
			}
		}

		@Override
		public void chooseHeight(CharSequence text, int start, int end, int istartv, int v, Paint.FontMetricsInt fm)
		{
			if (!m_isfontHeight)
			{
				if (start <= ((Spanned) text).getSpanStart(this) && ((Spanned) text).getSpanEnd(this) <= end)
				{
					//				Drawable drawable = getSmileyDrawable();
					int ht;
					if (drawable == null)
					{
						ht = 0;
					}
					else
					{
						ht = drawable.getIntrinsicHeight();
					}

					int need = ht - (fm.descent - fm.ascent);
					if (need > 0)
					{
						fm.ascent -= need;
						fm.top -= need;
					}

					need = ht - (fm.bottom - fm.top);
					if (need > 0)
					{
						fm.bottom += need;
					}
				}
			}
		}

		/**
		 * <p>
		 * Get drawable of mSmiley accroding to mSmiley code
		 * </p>
		 * 
		 * @return drawable of mSmiley image, or null if not found
		 */
		private Drawable getSmileyDrawable()
		{
			if (array == null)
			{
				array = mContext.getResources().getStringArray(R.array.smiley_code_array);
			}
			int index = 0;//
			for (String str : array)
			{
				if (str.equals(mSmiley))
				{
					break;
				}
				else
				{
					index++;
				}
			}
			TypedArray resIds = mContext.getResources().obtainTypedArray(R.array.smiley_resid_array);
			int resId = resIds.getResourceId(index, 0);
			if (resId == 0)
			{
				return null;
			}
			else
			{
				return mContext.getResources().getDrawable(resId);
			}

		}

		private Drawable getSmileyDrawable2(int index)
		{
			TypedArray resIds = mContext.getResources().obtainTypedArray(R.array.smiley_resid_array);
			int resId = resIds.getResourceId(index, 0);
			if (resId == 0)
			{
				return null;
			}
			else
			{
				return mContext.getResources().getDrawable(resId);
			}
		}
	}

	// singleton pattern
	private static Smilify instance = null;

	private Pattern mSmileyPattern;
	private Context mContext;

	/**
	 * <p>
	 * Private constructor, singleton pattern
	 * </p>
	 * 
	 * @param context
	 *            Context
	 */
	private Smilify(Context context)
	{
		mContext = context;
		StringBuffer buf = new StringBuffer();
		String[] array = context.getResources().getStringArray(R.array.smiley_code_array);
		//		Arrays.sort(array);
		for (String smiley : array)
		{
			smiley = smiley.replace("|", "\\|");
			int length = smiley.length();
			for (int i = 0; i < length; i++)
			{
				char c = smiley.charAt(i);
				if ('a' <= c && c <= 'z')
				{
					buf.append('[').append(c).append((char) (c - 'a' + 'A')).append(']');
				}
				else if ('A' <= c && c <= 'Z')
				{
					buf.append('[').append(c).append((char) (c - 'A' + 'a')).append(']');
				}
				else
				{
					buf.append(c);
				}
			}
			buf.append('|');
		}
		if (buf.length() > 0)
		{
			buf.deleteCharAt(buf.length() - 1);
		}
		String escaped = buf.toString().replace(")", "\\)").replace("(", "\\(").replace("^", "\\^").replace("$", "\\$").replace("*", "\\*").replace("?", "\\?").replace("{", "\\{")
			.replace("}", "\\}").replace("+", "\\+").replace("&", "\\&");
		mSmileyPattern = Pattern.compile(escaped);
	}

	/**
	 * <p>
	 * Get single instance of Smilify
	 * <p>
	 * 
	 * @param context
	 *            Context
	 * @return single instance of Smilify
	 */
	public static Smilify getInstance(Context context)
	{
		if (instance == null)
		{
			instance = new Smilify(context);
		}
		return instance;
	}

	public static String smilifFilter(String text)
	{
		if (text.length() == 100)
		{
			if (text.charAt(text.length() - 4) == '(' && text.charAt(text.length() - 1) == ')')
			{
				return text;
			}
			else if (text.charAt(text.length() - 3) == '(' && text.charAt(text.length() - 1) == ')')
			{
				return text;
			}
			else
			{
				if (text.charAt(text.length() - 2) == '(')
				{
					return text.substring(0, text.length() - 2);
				}
				else if (text.charAt(text.length() - 3) == '(')
				{
					return text.substring(0, text.length() - 3);
				}
				else if (text.charAt(text.length() - 1) == '(' || text.charAt(text.length() - 1) == ')')
				{
					return text.substring(0, text.length() - 1);
				}
				else
				{
					return text;
				}
			}
		}
		else
		{
			return text;
		}
	}

	/**
	 * 添加一个表情
	 * @param spannable Spannable对象
	 */
	public void addSmiley(Spannable spannable)
	{
		Matcher m = mSmileyPattern.matcher(spannable);
		while (m.find())
		{
			int start = m.start();
			int end = m.end();
			String smileyCode = spannable.subSequence(start, end).toString();
			SmileySpan smileySpan = new SmileySpan(smileyCode);
			spannable.setSpan(smileySpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
	}
}
