package com.cmccpoc.activity.home.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.airtalkee.sdk.entity.AirImage;
import com.cmccpoc.R;
import com.cmccpoc.control.AirReportManager;

import java.util.ArrayList;
import java.util.List;

/**
 Created by Yao on 2017/6/14. */

public class AdapterHorizontalListView extends AdapterBase implements AdapterBase.OnImageLoadCompletedListener
{
    private List<AirImage> images = new ArrayList<AirImage>();
    private Context mContext;
    private LayoutInflater mInflater;
    private boolean isEdit = false;
    private OnAdapterHorizontalListViewListener listener = null;

    public interface OnAdapterHorizontalListViewListener
    {
        public void OnAdapterHorizontalListViewItemClose(int position);
        public void OnAdapterHorizontalListViewItemAdd();
    }

    public AdapterHorizontalListView(Context context, boolean isEdit)
    {
        this.mContext = context;
        this.isEdit = isEdit;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(mContext);
    }

    public AdapterHorizontalListView(Context context, List<AirImage> images, boolean isEdit)
    {
        this(context, isEdit);
        this.images = images;
    }

    public void setListener(OnAdapterHorizontalListViewListener listener)
    {
        this.listener = listener;
    }

    public void notifyList(List<AirImage> images)
    {
        this.images = images;
        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return images.size() + (isEdit && images.size() < AirReportManager.REPORT_IMAGE_MAX_CNT ? 1 : 0);
    }

    @Override
    public Object getItem(int position)
    {
        if (position < images.size())
            return images.get(position);
        else
            return null;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        if (convertView == null)
        {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.horizontal_list_item, null);
            holder.mImage = (ImageView) convertView.findViewById(R.id.img_list_item);
            holder.mClose = (ImageView) convertView.findViewById(R.id.img_close);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        final AirImage image = (AirImage) getItem(position);
        if (image != null)
        {
            holder.mImage.setImageURI(image.getFileUri());
            //displayImageByUrl(image.getFileUri().toString(), holder.mImage, null, null);
            holder.mImage.setOnClickListener(null);
            if (isEdit)
            {
                final int pos = position;
                holder.mClose.setVisibility(View.VISIBLE);
                if (listener != null)
                {
                    holder.mClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.OnAdapterHorizontalListViewItemClose(pos);
                        }
                    });
                }
            }
            else
                holder.mClose.setVisibility(View.GONE);
        }
        else
        {
            holder.mImage.setImageResource(R.drawable.icon_addpic_unfocused_small);
            holder.mClose.setVisibility(View.GONE);
            if (listener != null)
            {
                holder.mImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.OnAdapterHorizontalListViewItemAdd();
                    }
                });
            }
        }
        return convertView;
    }

    class ViewHolder
    {
        ImageView mImage;
        ImageView mClose;
    }

    @Override
    public void onImageLoadCompleted(String imageUri, int orientation, View v, int width, int height)
    {
        try
        {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
            switch (orientation)
            {
                case AdapterSessionMessage.ORIENTATION_HORIZONTAL:
                case AdapterSessionMessage.ORIENTATION_VERTICAL:
                {
                    params.width = width;
                    params.height = height;
                    break;
                }
                case AdapterSessionMessage.ORIENTATION_SQUARE:
                    params.width = height;
                    params.height = height;
                    break;
            }
            v.setLayoutParams(params);
        }
        catch (Exception e)
        {
            // TODO: handle exception
        }

    }
}
