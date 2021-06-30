package com.example.myapplication;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.push.RongPushClient;
import io.rong.push.pushconfig.PushConfig;

public class App extends Application {
    public static final String APP_KEY = "lmxuhwagl6ddd";
    public static final String APP_SECRET = "X73cq2WP9keSot";
    private static Context context;

    public static Context getAppContext(){
        return context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        //推送（这个已经集成到callkit模块中了）
        PushConfig config = new PushConfig.Builder()
                .build();
        RongPushClient.setPushConfig(config);

        //音视频通话 CallKit
        RongIM.init(this, APP_KEY);

        //音视频会议 RTCLib ( CallKit 中已经包含 RTCLib 中的内容，所以不用重复注册)
//        RongIMClient.init(this, APP_KEY, false);
    }
}