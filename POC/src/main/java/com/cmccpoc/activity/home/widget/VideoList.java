package com.cmccpoc.activity.home.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeMediaVideoControl;
import com.airtalkee.sdk.AirtalkeeSessionManager;
import com.airtalkee.sdk.entity.AirSession;
import com.airtalkee.sdk.entity.AirVideoShare;

import java.util.List;
import com.cmccpoc.R;
import com.cmccpoc.control.AirSessionControl;


public class VideoList implements View.OnClickListener, AdapterView.OnItemClickListener
{
    private Activity mActivity;
    private VideoListSelectListener mListener = null;

    private RelativeLayout mPanel, mPanelSpace;
    private LinearLayout mVideoPanel;
    private ImageView mVideoSiderOpen, mVideoSiderClose;
    private ListView mVideoList;
    private VideoListAdapter mAdapter;

    private boolean isShowing = false;


    public VideoList(Activity activity, VideoListSelectListener listener)
    {
        mActivity = activity;
        mListener = listener;

        // Load view
        mPanel = (RelativeLayout) activity.findViewById(R.id.talk_video_layout);
        mPanelSpace = (RelativeLayout) activity.findViewById(R.id.talk_video_space);
        mPanelSpace.setOnClickListener(this);
        mPanelSpace.setVisibility(View.GONE);
        mVideoPanel = (LinearLayout) activity.findViewById(R.id.talk_video_list);
        mVideoPanel.setVisibility(View.GONE);
        mVideoSiderOpen = (ImageView) activity.findViewById(R.id.talk_video_sider_open);
        mVideoSiderOpen.setVisibility(View.GONE);
        mVideoSiderOpen.setOnClickListener(this);
        mVideoSiderClose = (ImageView) activity.findViewById(R.id.talk_video_sider_close);
        mVideoSiderClose.setOnClickListener(this);
        mVideoList = (ListView) activity.findViewById(R.id.talk_video_list_content);
        mVideoList.setOnItemClickListener(this);
        activity.findViewById(R.id.talk_video_list_title).setOnClickListener(this);

        mAdapter = new VideoListAdapter(activity);
        mVideoList.setAdapter(mAdapter);
    }

    public void setVisible(boolean isVisible)
    {
        mPanel.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    public void activeSet(String sessionCode, String owner)
    {
        AirtalkeeMediaVideoControl.getInstance().VideoRealtimeSetActvie(sessionCode, owner);
    }

    public void activePlay()
    {
        Message msg = mHandler.obtainMessage();
        msg.what = HANDLER_PLAY_ACTIVE;
        mHandler.sendMessageDelayed(msg, 300);
    }

    public void activeClean()
    {
        AirtalkeeMediaVideoControl.getInstance().VideoRealtimeCleanActvie();
        viewRefresh();
    }

    public void viewRefresh()
    {
        if (mAdapter.getCount() > 0)
        {
            if (isShowing)
            {
                mPanelSpace.setVisibility(View.VISIBLE);
                mVideoPanel.setVisibility(View.VISIBLE);
                mVideoSiderOpen.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
            }
            else
            {
                mPanelSpace.setVisibility(View.GONE);
                mVideoPanel.setVisibility(View.GONE);
                mVideoSiderOpen.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            isShowing = false;
            mPanelSpace.setVisibility(View.GONE);
            mVideoPanel.setVisibility(View.GONE);
            mVideoSiderOpen.setVisibility(View.GONE);
        }

        if (mListener != null)
        {
            if (AirtalkeeMediaVideoControl.getInstance().VideoRealtimeGetActvie() == null)
                mListener.onVideoListNoSelect();
        }
    }

    private static final int HANDLER_PLAY_ACTIVE = 10;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            super.handleMessage(msg);
            if (msg.what == HANDLER_PLAY_ACTIVE)
            {
                AirVideoShare share = AirtalkeeMediaVideoControl.getInstance().VideoRealtimeGetActvie();
                if (mListener != null && share != null)
                    mListener.onVideoListSelect(share);
            }
        }
    };

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.talk_video_sider_open:
            {
                isShowing = true;
                viewRefresh();
                break;
            }
            case R.id.talk_video_space:
            case R.id.talk_video_sider_close:
            {
                isShowing = false;
                viewRefresh();
                break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        AirVideoShare videoShare = (AirVideoShare) mAdapter.getItem(position);
        if (videoShare != null)
        {
            AirtalkeeMediaVideoControl.getInstance().VideoRealtimeSetActvie(position);
            if (mListener != null)
                mListener.onVideoListSelect(videoShare);
            isShowing = false;
            viewRefresh();
        }
    }

    /***********************************
     *
     *  VideoListAdapter
     *
     ***********************************/

    private class VideoListAdapter extends BaseAdapter
    {
        private Context mContext;
        private List<AirVideoShare> videoShares;

        public VideoListAdapter(Context context)
        {
            this.mContext = context;
            this.videoShares = AirtalkeeMediaVideoControl.getInstance().VideoRealtimeGetShareList();
        }

        @Override
        public int getCount()
        {
            return videoShares == null ? 0 : videoShares.size();
        }

        @Override
        public Object getItem(int position)
        {
            return videoShares == null ? null : videoShares.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            VideoListAdapter.ViewHolder holder = null;
            if (convertView == null)
            {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.video_list_item, null);
                holder = new VideoListAdapter.ViewHolder(convertView);
                convertView.setTag(holder);
            }
            else
                holder = (VideoListAdapter.ViewHolder) convertView.getTag();
            try
            {
                holder.fill((AirVideoShare) getItem(position), position);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            return convertView;
        }

        class ViewHolder
        {
            RelativeLayout panel;
            ImageView button;
            TextView txtName;
            TextView txtSession;

            public ViewHolder(View convertView)
            {
                panel = (RelativeLayout) convertView.findViewById(R.id.talk_video_item);
                button = (ImageView) convertView.findViewById(R.id.talk_video_connect);
                txtName = (TextView) convertView.findViewById(R.id.talk_video_item_name);
                txtSession = (TextView) convertView.findViewById(R.id.talk_video_item_session);
            }

            public void fill(final AirVideoShare videoShare, final int position)
            {
                if (videoShare != null)
                {
                    if (videoShare.isActive())
                        panel.setBackgroundResource(R.drawable.bg_msgbox_select);
                    else
                        panel.setBackgroundResource(R.drawable.bg_msgbox);
                    txtName.setText(videoShare.getOwnerName());
                    txtSession.setText(videoShare.getSessionName());

                    if (AirSession.sessionType(videoShare.getSessionCode()) == AirSession.TYPE_CHANNEL)
                    {
                        final AirSession session = AirtalkeeSessionManager.getInstance().SessionMatch(videoShare.getSessionCode());
                        if (session != null)
                        {
                            if (session.getSessionState() == AirSession.SESSION_STATE_DIALOG)
                            {
                                button.setImageResource(R.drawable.ic_listen_yellow);
                                button.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        AirSessionControl.getInstance().SessionChannelOut(session.getSessionCode());
                                        notifyDataSetChanged();
                                    }
                                });
                            }
                            else
                            {
                                button.setImageResource(R.drawable.ic_listen);
                                button.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        AirSessionControl.getInstance().SessionChannelIn(session.getSessionCode());
                                        notifyDataSetChanged();
                                    }
                                });
                            }
                            button.setVisibility(View.VISIBLE);

                        }
                        else
                            button.setVisibility(View.GONE);
                    }
                    else
                    {
                        button.setVisibility(View.GONE);
                    }

                }
            }
        }
    }
}
