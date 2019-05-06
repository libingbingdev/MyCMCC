package com.cmccpoc.activity.home.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.airtalkee.sdk.AirtalkeeChannel;
import com.airtalkee.sdk.entity.AirChannel;
import com.airtalkee.sdk.entity.AirSession;
import com.cmccpoc.R;
import com.cmccpoc.activity.home.widget.CheckedView;
import com.cmccpoc.control.AirSessionControl;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * 多频道监听列表 适配器
 *
 * @author Yao
 */
public class AdapterMultichannel extends BaseAdapter {
    Context mContext;

    public ArrayList<String> airchannelList=new ArrayList<String>();
    public AdapterMultichannel(Context mContext, ArrayList<String> data) {
        this.mContext = mContext;
        airchannelList=data;
        //显示用户此前录入的数据

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HodlerView hodler = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.listitem_multichannel, null);
            hodler = new HodlerView(convertView);
            convertView.setTag(hodler);
        } else
            hodler = (HodlerView) convertView.getTag();
        try {
            hodler.fill((AirChannel) getItem(position));
        } catch (Exception e) {
        }
        return convertView;
    }

    class HodlerView {
        public TextView tvName;
        public CheckBox cvCheck;


        public HodlerView(View convertView) {
            tvName = (TextView) convertView.findViewById(R.id.tv_name);
            cvCheck = (CheckBox) convertView.findViewById(R.id.cv_check);

        }

        /**
         * 填充View
         *
         * @param item 频道Entity
         */
        public void fill(final AirChannel item) {

            if (item != null) {
                tvName.setText(item.getDisplayName());
                if(airchannelList.contains(item.getId()) ){
                    cvCheck.setChecked(true);
                }else{
                    cvCheck.setChecked(false);
                }

               


                if (item.getSession() != null && item.getSession().getSessionState() == AirSession.SESSION_STATE_DIALOG) {


                } else {

                }



            }
        }
    }

    @Override
    public int getCount() {
        if (AirtalkeeChannel.getInstance().getChannels() == null) return 0;
        return AirtalkeeChannel.getInstance().getChannels().size();
    }

    @Override
    public Object getItem(int position) {
        AirChannel ch = null;
        try {
            ch = AirtalkeeChannel.getInstance().getChannels().get(position);
        } catch (Exception e) {
        }
        return ch;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }



}
