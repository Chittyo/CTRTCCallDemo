package com.example.myapplication;

import android.app.Application;

import androidx.multidex.MultiDex;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.push.RongPushClient;
import io.rong.push.pushconfig.PushConfig;

public class App extends Application {
    public static final String APP_KEY = "lmxuhwagl6ddd";
    public static final String APP_SECRET = "X73cq2WP9keSot";

    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);

        //推送（这个已经集成到callkit模块中了）
        PushConfig config = new PushConfig.Builder()
                .build();
        RongPushClient.setPushConfig(config);

        //音视频通话 CallKit
        RongIM.init(this, APP_KEY);

        //音视频会议 RTCLib
        RongIMClient.init(this, APP_KEY, false);
    }
}