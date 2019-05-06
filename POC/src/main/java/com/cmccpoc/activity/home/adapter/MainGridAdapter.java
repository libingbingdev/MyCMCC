package com.cmccpoc.activity.home.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cmccpoc.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/10/8.
 */

public class MainGridAdapter extends BaseAdapter{
    Context mContext;
    ArrayList<String> mList;
    public MainGridAdapter(Context context, ArrayList<String> list){
        this.mContext=context;
        this.mList=list;

    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        HodlerView hodler = null;
        if (view == null)
        {
            view = LayoutInflater.from(mContext).inflate(R.layout.main_gv_item_layout, null);
            hodler = new HodlerView(view);
            view.setTag(hodler);
        }
        else
            {
            hodler = (HodlerView) view.getTag();

            }
        hodler.tvName.setText(mList.get(i));
        return view;

    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    class HodlerView {
        public TextView tvName;


        public HodlerView(View convertView) {
            tvName = (TextView) convertView.findViewById(R.id.gv_item_text);
        }
    }
}
