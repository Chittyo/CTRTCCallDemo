package com.example.myapplication.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.util.List;

import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RTCErrorCode;

/**
 * 音视频会议
 */
public class MeetingActivity extends AppCompatActivity implements View.OnClickListener, MeetingPresenter.MeetingCallback {
    private static final String TAG = MeetingActivity.class.getName();
    public static final String KEY_ROOM_NUMBER = "room_number";
    public static final String KEY_IS_ENCRYPTION = "KEY_IS_ENCRYPTION";
    private static String roomId = "";
    private boolean isEncryption = false;
    MeetingPresenter meetingPresenter;
    // 本地预览远端用户，全屏显示 VideoView
    private FrameLayout flLocalUser, flRemoteUser, flFullscreen;
    private TextView tvHangUp;


    public static void start(Context context, String roomId, boolean isEncryption) {
        Intent intent = new Intent(context, MeetingActivity.class);
        intent.putExtra(KEY_ROOM_NUMBER, roomId);
        intent.putExtra(KEY_IS_ENCRYPTION, isEncryption);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting);
        flLocalUser = findViewById(R.id.flLocalUser);
        flRemoteUser = findViewById(R.id.flRemoteUser);
        flFullscreen = findViewById(R.id.flFullscreen);
        tvHangUp = findViewById(R.id.tvHangUp);
        tvHangUp.setOnClickListener(this);

        Intent intent = getIntent();
        roomId = intent.getStringExtra(KEY_ROOM_NUMBER);
        isEncryption = intent.getBooleanExtra(KEY_IS_ENCRYPTION, false);
        meetingPresenter = new MeetingPresenter();
        meetingPresenter.attachView(this);
        meetingPresenter.config(this, isEncryption);
        meetingPresenter.joinRoom(roomId);

        initTitle();
        initLocalVideoView();
    }

    private void initTitle() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle((isEncryption ? "加密会议" : "普通会议") + ":" + roomId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        meetingPresenter.leaveRoom();
        meetingPresenter.detachView();
        flFullscreen.removeAllViews();
        flLocalUser.removeAllViews();
        flRemoteUser.removeAllViews();
        RCRTCEngine.getInstance().unInit();
    }

    /**
     * 初始化本地视频
     */
    private void initLocalVideoView() {
        // 初始化视图
        RCRTCVideoView localVideoView = new RCRTCVideoView(getApplicationContext());
        // 绑定视图
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoView(localVideoView);
        // 打开摄像机
        RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvHangUp:
                finish();
                break;
        }
    }

    @Override
    public void onJoinRoomSuccess(RCRTCRoom rcrtcRoom) {
        // 加入房间成功，在 UI 线程设置本地用户显示的 View
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RCRTCVideoView rongRTCVideoView = new MeetVideoView(getApplicationContext()) {
                    public boolean onTouchEvent(MotionEvent event) {
                        super.onTouchEvent(event);
                        return true;
                    }
                };
                RCRTCEngine.getInstance().getDefaultVideoStream().setVideoView(rongRTCVideoView);
                flLocalUser.addView(rongRTCVideoView);
                // 本地用户发布
                meetingPresenter.publishDefaultAVStream();
                // 主动订阅远端用户发布的资源
                meetingPresenter.subscribeAVStream();
            }
        });
    }

    @Override
    public void onJoinRoomFailed(RTCErrorCode rtcErrorCode) {

    }

    @Override
    public void onPublishSuccess() {

    }

    @Override
    public void onPublishFailed() {

    }

    @Override
    public void onSubscribeSuccess(List<RCRTCInputStream> inputStreamList) {
        // 订阅远端用户发布资源成功，设置显示的 view，在 UI 线程中执行
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RCRTCVideoView videoView = new MeetVideoView(getApplicationContext()) {
                    public boolean onTouchEvent(MotionEvent event) {
                        super.onTouchEvent(event);
                        // 远端用户视频全屏切换
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            if (this.getParent() == flRemoteUser) {
                                flRemoteUser.removeView(this);
                                flFullscreen.addView(this);
                            } else {
                                flFullscreen.removeView(this);
                                flRemoteUser.addView(this);
                            }
                        }
                        return true;
                    }
                };
                for (RCRTCInputStream inputStream : inputStreamList) {
                    if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                        ((RCRTCVideoInputStream) inputStream).setVideoView(videoView);
                        // 将远端视图添加至布局
                        MeetingActivity.this.flRemoteUser.addView(videoView);
                        break;
                    }
                }
            }
        });
    }

    @Override
    public void onSubscribeFailed() {

    }

    @Override
    public void onUserJoined(RCRTCRemoteUser rcrtcRemoteUser) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MeetingActivity.this, ("用户:" + rcrtcRemoteUser.getUserId() + "加入会议"), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RCRTCVideoView rongRTCVideoView = flRemoteUser.findViewWithTag(MeetVideoView.class.getName());
                // 远端用户离开时, videoview 在 mFlRemoteUser上，删除挂载在 mFlRemoteUser 上的 videoview
                if (null != rongRTCVideoView) {
                    flRemoteUser.removeAllViews();
                    rongRTCVideoView = flFullscreen.findViewWithTag(MeetVideoView.class.getName());
                    // 远端用户离开时，如果本地预览正处于全屏状态自动退出全屏
                    if (rongRTCVideoView != null) {
                        flFullscreen.removeAllViews();
                        flLocalUser.addView(rongRTCVideoView);
                    }
                } else {
                    // 远端用户离开时 , videoview 在 mFlFull 上，删除挂载在 mFlFull 上的 videoview
                    flFullscreen.removeAllViews();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // 返回按钮响应事件
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 继承RCRTCVideoView,可以重写RCRTCVideoView方法定制特殊需求，
     * 例如本例中重写onTouchEvent实现点击全屏
     */
    class MeetVideoView extends RCRTCVideoView {
        public MeetVideoView(Context context) {
            super(context);
            this.setTag(MeetVideoView.class.getName());
        }
    }
}
