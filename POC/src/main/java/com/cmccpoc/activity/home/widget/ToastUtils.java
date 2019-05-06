package com.cmccpoc.activity.home.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.cmccpoc.R;

public class ToastUtils {
    /**
     * 带图片的吐司提示
     * 通过参数传递，可是设置吐司的图片和文字内容
     * @param text
     */
    public static void showCustomImgToast(String text, int imgResId , Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.toast_view, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.toast_image);
        imageView.setImageResource(imgResId);
        TextView t = (TextView) view.findViewById(R.id.toast_text);
        t.setText(text);
        Toast toast = null;
        if (toast != null) {
            toast.cancel();
        }
        toast = new Toast(context);
        toast.setGravity(Gravity.FILL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.show();
    }

}