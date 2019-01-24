package cn.wps.moffice.demo.util;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cn.wps.moffice.demo.MyApplication;
import cn.wps.moffice.demo.R;


public class ToastUtil {

    private static Toast mToast;

        public static void showLong(String string) {
        if (string == null || string.trim().equals("")) return;
        View layout = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.layout_toast, null);
        layout.getBackground().setAlpha(200);

        /*设置布局*/
        TextView textView = (TextView) layout.findViewById(R.id.text_content);
        textView.setText(string);

        if (mToast == null) {
            mToast = new Toast(MyApplication.getInstance());
        }
        mToast.setDuration(Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.BOTTOM, 0, ScreenUtils.dip2px(MyApplication.getInstance(),60f));
        mToast.setView(layout);
        mToast.show();
    }

    public static void showShort(String string) {
        if (string == null || string.trim().equals("")) return;
        View layout = LayoutInflater.from(MyApplication.getInstance()).inflate(R.layout.layout_toast, null);
        layout.getBackground().setAlpha(200);

        /*设置布局*/
        TextView textView = (TextView) layout.findViewById(R.id.text_content);
        textView.setText(string);

        if (mToast == null) {
            mToast = new Toast(MyApplication.getInstance());
        }
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.setGravity(Gravity.BOTTOM, 0, ScreenUtils.dip2px(MyApplication.getInstance(),60f));
        mToast.setView(layout);
        mToast.show();
    }


}
