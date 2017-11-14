package com.example.hang.myapplication;

/**
 * Created by hang on 11/14/17.
 */

import android.app.Application;
import android.content.Context;

public class MainApplication extends Application{
    /**
     * 全局的上下文
     */
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    /**
     * 获取context
     * @return
     */
    public static Context getContext(){
        return mContext;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

}
