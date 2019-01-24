package cn.wps.moffice.demo;

import android.app.Application;
import android.util.Log;

import com.amitshekhar.DebugDB;

/**
 * Created by LiuYi on 2019/1/22.
 */
public class MyApplication extends Application {
    private static Application instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance =this;
        Log.v("------->database:",DebugDB.getAddressLog());
        ;
    }

    public static Application getInstance() {
        return instance;
    }
}
