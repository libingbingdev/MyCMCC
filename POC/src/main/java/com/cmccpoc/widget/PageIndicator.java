package com.cmccpoc.widget;

import java.lang.reflect.Field;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.cmccpoc.R;

/**
 * app主界面中的页面切换的三个标点
 * left：频道成员 middle：对讲界面 right：IM消息
 * @author Yao
 */
public class PageIndicator extends LinearLayout
{
	public static final int INDICATOR_TYPE_CIRCLE = 0;
	public static final int INDICATOR_TYPE_FRACTION = 1;
	public static final String ACTION_PAGE_CHANGED = "com.airtalkee.ACTION_PAGE_CHANGED";
	public static final String EXTRA_PAGE_INDEX = "EXTRA_PAGE_INDEX";

	/**
	 * 枚举：指示器类型
	 * 可以指定不同的形状
	 * @author Yao
	 */
	public enum IndicatorType
	{
		CIRCLE(INDICATOR_TYPE_CIRCLE), FRACTION(INDICATOR_TYPE_FRACTION), UNKNOWN(-1);
		private int type;
		IndicatorType(int type)
		{
			this.type = type;
		}
		public static IndicatorType of(int value)
		{
			switch (value)
			{
				case INDICATOR_TYPE_CIRCLE:
					return CIRCLE;
				case INDICATOR_TYPE_FRACTION:
					return FRACTION;
				default:
					return UNKNOWN;
			}
		}
	}

	public static final int DEFAULT_INDICATOR_SPACING = 15;
	private int mActivePosition = -1;
	private int mIndicatorSpacing;
	private boolean mIndicatorTypeChanged = false;

	private IndicatorType mIndicatorType = IndicatorType.of(INDICATOR_TYPE_CIRCLE);
	private ViewPager mViewPager;

	public PageIndicator(Context context)
	{
		this(context, null);
	}

	public PageIndicator(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public PageIndicator(Context context, AttributeSet attrs, int defStyleAttr)
	{
		super(context, attrs, defStyleAttr);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PageIndicator, 0, 0);
		try
		{
			mIndicatorSpacing = a.getDimensionPixelSize(R.styleable.PageIndicator_indicator_spacing, DEFAULT_INDICATOR_SPACING);
			int indicatorTypeValue = a.getInt(R.styleable.PageIndicator_indicator_type, mIndicatorType.type);
			mIndicatorType = IndicatorType.of(indicatorTypeValue);
		}
		finally
		{
			a.recycle();
		}

		init();
	}

	/**
	 * 初始化View
	 */
	private void init()
	{
		setOrientation(HORIZONTAL);
		if (!(getLayoutParams() instanceof FrameLayout.LayoutParams))
		{
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
			params.gravity = Gravity.BOTTOM | Gravity.START;
			setLayoutParams(params);
		}
	}

	/**
	 * 设置ViewPager
	 * @param pager ViewPager
	 */
	public void setViewPager(ViewPager pager)
	{
		mViewPager = pager;
		setIndicatorType(mIndicatorType);
	}

	/**
	 * 设置指示器类型
	 * @param indicatorType 类型
	 */
	public void setIndicatorType(IndicatorType indicatorType)
	{
		mIndicatorType = indicatorType;
		mIndicatorTypeChanged = true;
		if (mViewPager != null)
		{
			addIndicator(mViewPager.getAdapter().getCount());
		}
	}

	/**
	 * 移除指示器
	 */
	private void removeIndicator()
	{
		removeAllViews();
	}

	/**
	 * 添加分页指示器
	 * @param count 分页数
	 */
	private void addIndicator(int count)
	{
		removeIndicator();
		if (count <= 0)
			return;
		if (mIndicatorType == IndicatorType.CIRCLE)
		{
			for (int i = 0; i < count; i++)
			{
				ImageView img = new ImageView(getContext());
				LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				params.leftMargin = mIndicatorSpacing;
				params.rightMargin = mIndicatorSpacing;
				img.setImageResource(R.drawable.circle_indicator_stroke);
				addView(img, params);
			}
		}
		else if (mIndicatorType == IndicatorType.FRACTION)
		{
			TextView textView = new TextView(getContext());
			textView.setTextColor(Color.WHITE);
			// int padding = dp2px(getContext(), 10);
			int padding = 10;
			textView.setPadding(padding, padding >> 1, padding, padding >> 1);
			textView.setBackgroundResource(R.drawable.fraction_indicator_bg);
			textView.setTag(count);
			LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			addView(textView, params);
		}
		updateIndicator(mViewPager.getCurrentItem());
	}

	/**
	 * 更新换页标点显示
	 * @param position 页面位置
	 */
	private void updateIndicator(int position)
	{
		if (mIndicatorTypeChanged || mActivePosition != position)
		{
			mIndicatorTypeChanged = false;
			if (mIndicatorType == IndicatorType.CIRCLE)
			{
				if (mActivePosition == -1)
				{
					((ImageView) getChildAt(position)).setImageResource(R.drawable.circle_indicator_solid);
					mActivePosition = position;
					return;
				}
				// 非当前页面
				((ImageView) getChildAt(mActivePosition)).setImageResource(R.drawable.circle_indicator_stroke);
				// 当前页面
				((ImageView) getChildAt(position)).setImageResource(R.drawable.circle_indicator_solid);
			}
			else if (mIndicatorType == IndicatorType.FRACTION)
			{
				TextView textView = (TextView) getChildAt(0);
				// noinspection RedundantCast
				textView.setText(String.format("%d/%d", position + 1, Integer.parseInt(textView.getTag().toString())));
			}
			mActivePosition = position;
		}
	}

	/**
	 * {@link android.support.v4.view.ViewPager#setOnPageChangeListener(ViewPager.OnPageChangeListener)}
	 * is deprecated. We could keep a list of listeners by
	 * {@link android.support.v4.view.ViewPager#addOnPageChangeListener(ViewPager.OnPageChangeListener)}
	 * .
	 */

	@SuppressWarnings("unused")
	private ViewPager.OnPageChangeListener getOnPageChangeListener(ViewPager pager)
	{
		try
		{
			Field f = pager.getClass().getDeclaredField("mOnPageChangeListener");
			f.setAccessible(true);
			return (ViewPager.OnPageChangeListener) f.get(pager);
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private int dp2px(Context context, int dpValue)
	{
		return (int) context.getResources().getDisplayMetrics().density * dpValue;
	}

	/**
	 * 当页面切换时
	 * @param position
	 */
	public void onPageChanged(int position)
	{
		updateIndicator(position);
	}

}
