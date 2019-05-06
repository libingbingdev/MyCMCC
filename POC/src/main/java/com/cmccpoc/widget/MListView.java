package com.cmccpoc.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.cmccpoc.R;


public class MListView extends ListView implements OnScrollListener, OnClickListener
{

	public final static int RELEASE_TO_REFRESH = 0;
	public final static int PULL_TO_REFRESH = 1;
	public final static int REFRESHING = 2;
	public final static int DONE = 3;
	public final static int LOADING = 4;
	private final static int RATIO = 3;
	private LayoutInflater inflater;
	private View layoutHead;
	private LinearLayout headView;

	private boolean isRecored;

	private int headContentWidth;
	private int headContentHeight;

	private int startY;
	private int firstItemIndex;

	public int state;

	private OnRefreshListener refreshListener;

	private boolean isRefreshable;

	public void setRefreshable(boolean isRefreshable)
	{
		this.isRefreshable = isRefreshable;
	}

	public int getHeadContentHeight()
	{
		return headContentHeight;
	}

	public void setHeadContentHeight(int headContentHeight)
	{
		this.headContentHeight = headContentHeight;
	}

	public MListView(Context context)
	{
		super(context);
		init(context);
	}

	public MListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	private void init(Context context)
	{
		setCacheColorHint(context.getResources().getColor(R.color.transparent));
		inflater = LayoutInflater.from(context);

		headView = (LinearLayout) inflater.inflate(R.layout.m_listview_head, null);
		layoutHead = headView.findViewById(R.id.talk_layout_channel_edit);

		headView.findViewById(R.id.talk_btn_channel_add).setOnClickListener(this);
		headView.findViewById(R.id.talk_btn_channel_edit_name).setOnClickListener(this);
		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();
		headContentWidth = headView.getMeasuredWidth();

		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		headView.invalidate();
		Log.v("size", "width:" + headContentWidth + " height:" + headContentHeight);
		addHeaderView(headView, null, false);
		setOnScrollListener(this);
		state = DONE;
		isRefreshable = false;
	}

	public void onScroll(AbsListView arg0, int firstVisiableItem, int arg2, int arg3)
	{
		firstItemIndex = firstVisiableItem;
	}

	public void onScrollStateChanged(AbsListView arg0, int arg1)
	{}

	public boolean onTouchEvent(MotionEvent event)
	{
		if (isRefreshable)
		{
			switch (event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
					if (firstItemIndex == 0 && !isRecored)
					{
						isRecored = true;
						startY = (int) event.getY();
					}
					break;

				case MotionEvent.ACTION_UP:

					if (state != REFRESHING && state != LOADING)
					{
						if (state == DONE)
						{
						}
						if (state == PULL_TO_REFRESH)
						{
							state = DONE;
							changeHeaderViewByState();
						}
						if (state == RELEASE_TO_REFRESH)
						{
							state = REFRESHING;
							changeHeaderViewByState();
							onRefresh();
						}
					}

					isRecored = false;

					break;

				case MotionEvent.ACTION_MOVE:
					int tempY = (int) event.getY();

					if (!isRecored && firstItemIndex == 0)
					{
						isRecored = true;
						startY = tempY;
					}

					if (state != REFRESHING && isRecored && state != LOADING)
					{
						if (state == RELEASE_TO_REFRESH)
						{
							setSelection(0);
							if (((tempY - startY) / RATIO < headContentHeight) && (tempY - startY) > 0)
							{
								state = PULL_TO_REFRESH;
								changeHeaderViewByState();
							}
							else if (tempY - startY <= 0)
							{
								state = DONE;
								changeHeaderViewByState();
							}
							else
							{
							}
						}
						if (state == PULL_TO_REFRESH)
						{
							setSelection(0);
							if ((tempY - startY) / RATIO >= headContentHeight)
							{
								state = RELEASE_TO_REFRESH;
								changeHeaderViewByState();
							}
							else if (tempY - startY <= 0)
							{
								state = DONE;
								changeHeaderViewByState();
							}
						}

						if (state == DONE)
						{
							if (tempY - startY > 0)
							{
								state = PULL_TO_REFRESH;
								changeHeaderViewByState();
							}
						}

						if (state == PULL_TO_REFRESH)
						{
							headView.setPadding(0, -1 * headContentHeight + (tempY - startY) / RATIO, 0, 0);

						}

						if (state == RELEASE_TO_REFRESH)
						{
							headView.setPadding(0, (tempY - startY) / RATIO - headContentHeight, 0, 0);
						}

					}

					break;
			}
		}

		return super.onTouchEvent(event);
	}

	public void changeHeaderViewByState()
	{
		switch (state)
		{
			case RELEASE_TO_REFRESH:
				layoutHead.setVisibility(View.VISIBLE);
				break;
			case PULL_TO_REFRESH:
				layoutHead.setVisibility(View.VISIBLE);
				break;
			case REFRESHING:
				headView.setPadding(0, 0, 0, 0);
				layoutHead.setVisibility(View.VISIBLE);
				break;
			case DONE:
				layoutHead.setVisibility(View.GONE);
				headView.setPadding(0, -1 * headContentHeight, 0, 0);
				break;
		}
		headView.invalidate();
	}

	public void setOnRefreshListener(OnRefreshListener refreshListener)
	{
		this.refreshListener = refreshListener;
		//isRefreshable = true;
	}

	public interface OnRefreshListener
	{
		public void onRefresh();

		public void onClick(View v);
	}

	public void onRefreshComplete()
	{
		state = DONE;
		changeHeaderViewByState();
	}

	private void onRefresh()
	{
		if (refreshListener != null)
		{
			refreshListener.onRefresh();
		}
	}

	@SuppressWarnings("deprecation")
	private void measureView(View child)
	{
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null)
		{
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0)
		{
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		}
		else
		{
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void setAdapter(BaseAdapter adapter)
	{
		super.setAdapter(adapter);
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		refreshListener.onClick(v);
	}

}
