package com.cmccpoc.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import com.cmccpoc.R;

/**
 * 下拉刷新IM消息列表，并获取显示更早的消息记录
 * @author Yao
 */
public class PullToRefreshListView extends ListView implements OnScrollListener
{

	public final static int RELEASE_To_REFRESH = 0;
	public final static int PULL_To_REFRESH = 1;
	public final static int REFRESHING = 2;
	public final static int DONE = 3;

	private final static int RATIO = 1;
	private LayoutInflater inflater;
	private LinearLayout headView;

	private ProgressBar progressBar;

	private boolean isRecored;

	private int headContentWidth;
	private int headContentHeight;

	private int startY;
	private int firstItemIndex;
	private int visibleCount;
	public int state;

	private boolean isHaveMore = false;

	private OnPullToRefreshListener refreshListener;

	public PullToRefreshListView(Context context)
	{
		super(context);
		init(context);
	}

	public PullToRefreshListView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}

	public void setHaveMore(boolean isHaveMore)
	{
		this.isHaveMore = isHaveMore;
	}

	/**
	 * 初始化控件
	 * @param context 上下文
	 */
	private void init(Context context)
	{
		setCacheColorHint(context.getResources().getColor(android.R.color.transparent));
		inflater = LayoutInflater.from(context);

		headView = (LinearLayout) inflater.inflate(R.layout.pull_to_refresh_listview_head, null);

		progressBar = (ProgressBar) headView.findViewById(R.id.head_progressBar);
		measureView(headView);
		headContentHeight = headView.getMeasuredHeight();
		headContentWidth = headView.getMeasuredWidth();

		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		headView.invalidate();
		Log.v("size", "width:" + headContentWidth + " height:" + headContentHeight);
		addHeaderView(headView, null, false);
		setOnScrollListener(this);
		state = DONE;
	}

	/**
	 * 滑动时触发
	 */
	public void onScroll(AbsListView arg0, int firstVisiableItem, int visibleItemCount, int totalItemCount)
	{
		firstItemIndex = firstVisiableItem;
		visibleCount = visibleItemCount;
	}

	/**
	 * 滚动状态改变时
	 */
	public void onScrollStateChanged(AbsListView arg0, int scrollState)
	{
		Log.i("m", "firstItemIndex" + firstItemIndex);

		if (firstItemIndex == 0 && scrollState == OnScrollListener.SCROLL_STATE_IDLE)
		{
			if (state == DONE)
			{
				state = REFRESHING;
				changeHeaderViewByState();
				onRefresh();
			}
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		try
		{
			switch (event.getAction())
			{
				case MotionEvent.ACTION_DOWN:
				{
					if (firstItemIndex == 0 && !isRecored)
					{
						isRecored = true;
						startY = (int) event.getY();
					}
					break;
				}
				case MotionEvent.ACTION_UP:
				{
					if (state != REFRESHING)
					{
						if (state == DONE)
						{
						}
						if (state == PULL_To_REFRESH)
						{
							state = DONE;
							changeHeaderViewByState();
						}
						if (state == RELEASE_To_REFRESH)
						{

							state = REFRESHING;
							changeHeaderViewByState();
							onRefresh();
						}
					}

					isRecored = false;
					break;
				}
				case MotionEvent.ACTION_MOVE:
				{
					int tempY = (int) event.getY();
					if (!isRecored && firstItemIndex == 0)
					{
						isRecored = true;
						startY = tempY;
					}

					if (state != REFRESHING && isRecored)
					{
						switch (state)
						{
							case RELEASE_To_REFRESH:
							{
								if (((tempY - startY) / RATIO < headContentHeight) && (tempY - startY) > 0)
								{
									state = PULL_To_REFRESH;
									changeHeaderViewByState();
								}
								else if (tempY - startY <= 0)
								{
									state = DONE;
									changeHeaderViewByState();
								}
								break;
							}
							case PULL_To_REFRESH:
							{
								if ((tempY - startY) / RATIO >= headContentHeight)
								{
									state = RELEASE_To_REFRESH;
									changeHeaderViewByState();
								}
								else if (tempY - startY <= 0)
								{
									state = DONE;
									changeHeaderViewByState();
								}
								break;
							}
							case DONE:
							{
								if (tempY - startY > 0)
								{
									state = PULL_To_REFRESH;
									changeHeaderViewByState();
								}
								break;
							}
						}
					}

					if (state == PULL_To_REFRESH)
					{
						headView.setPadding(0, -1 * headContentHeight + (tempY - startY) / RATIO, 0, 0);
					}
					if (state == RELEASE_To_REFRESH)
					{
						headView.setPadding(0, (tempY - startY) / RATIO - headContentHeight, 0, 0);
					}
					break;
				}
			}
			return super.onTouchEvent(event);
		}
		catch (Exception e)
		{
			// TODO: handle exception
			return false;
		}
	}

	/**
	 * 根据状态改变头部View显示
	 */
	public void changeHeaderViewByState()
	{

		switch (state)
		{
			case RELEASE_To_REFRESH:
				if (isHaveMore)
					progressBar.setVisibility(View.VISIBLE);
				break;
			case PULL_To_REFRESH:
				if (isHaveMore)
					progressBar.setVisibility(View.VISIBLE);
				break;

			case REFRESHING:
				headView.setPadding(0, 0, 0, 0);
				progressBar.setVisibility(View.VISIBLE);
				break;
			case DONE:
				headView.setPadding(0, -1 * headContentHeight, 0, 0);
				progressBar.setVisibility(View.INVISIBLE);
				break;
		}

		headView.invalidate();
	}

	public void setOnRefreshListener(OnPullToRefreshListener refreshListener)
	{
		this.refreshListener = refreshListener;
	}

	public interface OnPullToRefreshListener
	{
		public void onPullToRefresh(int firstVisibleItem, int visibleCount);
	}
	
	/**
	 * 刷新完成
	 */
	public void onRefreshComplete()
	{
		state = DONE;
		changeHeaderViewByState();
	}

	/**
	 * 正在刷新
	 */
	private void onRefresh()
	{
		if (refreshListener != null)
		{
			refreshListener.onPullToRefresh(firstItemIndex, visibleCount);
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
		adapter.notifyDataSetChanged();
	}

}
