package com.example.myapplication.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

import cn.rongcloud.rtc.api.RCRTCConfig;
import cn.rongcloud.rtc.api.RCRTCEngine;
import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.RCRTCRoomConfig;
import cn.rongcloud.rtc.api.callback.IRCRTCResultCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCResultDataCallback;
import cn.rongcloud.rtc.api.callback.IRCRTCRoomEventsListener;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoInputStream;
import cn.rongcloud.rtc.api.stream.RCRTCVideoStreamConfig;
import cn.rongcloud.rtc.api.stream.RCRTCVideoView;
import cn.rongcloud.rtc.base.RCRTCMediaType;
import cn.rongcloud.rtc.base.RCRTCParamsType;
import cn.rongcloud.rtc.base.RCRTCRoomType;
import cn.rongcloud.rtc.base.RCRTCStreamType;
import cn.rongcloud.rtc.base.RTCErrorCode;
import cn.rongcloud.voicebeautifier.RCRTCVoiceBeautifierEngine;
import cn.rongcloud.voicebeautifier.RCRTCVoiceBeautifierPreset;

/**
 * 音视频会议
 */
public class MeetingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MeetingActivity.class.getName();
    public static final String KEY_ROOM_NUMBER = "room_number";
    public static final String KEY_IS_ENCRYPTION = "KEY_IS_ENCRYPTION";
    private static String roomId = "";
    private boolean isEncryption = false;
    // 本地预览远端用户，全屏显示 VideoView
    private FrameLayout flLocalUser, flRemoteUser, flFullscreen;
    private TextView tvHangUp;

    private RCRTCRoom rtcRoom = null;

    private IRCRTCRoomEventsListener roomEventsListener = new IRCRTCRoomEventsListener() {
        /**
         * 房间内远端用户发布资源通知
         *
         * @param rcrtcRemoteUser 远端用户
         * @param list            发布的资源（流中有 userId（用户 ID），tag（标识符），type（流类型），state（是否禁用） 等关键信息，可调用订阅接口，订阅其中的流）
         */
        @Override
        public void onRemoteUserPublishResource(RCRTCRemoteUser rcrtcRemoteUser, final List<RCRTCInputStream> list) {
            subscribeAVStream();
        }

        /**
         * 远端用户音频静默状态变更通知
         * @param rcrtcRemoteUser
         * @param rcrtcInputStream
         * @param b
         */
        @Override
        public void onRemoteUserMuteAudio(RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean b) {
        }

        /**
         * 远端用户视频静默状态变更通知
         * @param rcrtcRemoteUser
         * @param rcrtcInputStream
         * @param b
         */
        @Override
        public void onRemoteUserMuteVideo(RCRTCRemoteUser rcrtcRemoteUser, RCRTCInputStream rcrtcInputStream, boolean b) {
        }

        /**
         * 远端用户资源取消发布通知
         * @param rcrtcRemoteUser 远端用户
         * @param list 取消的资源（流中有 userId（用户 ID），tag（标识符），type（流类型），state（是否禁用）等关键信息，APP 可根据这些关键信息自定义化，无需再次调用取消订阅接口）
         */
        @Override
        public void onRemoteUserUnpublishResource(RCRTCRemoteUser rcrtcRemoteUser, List<RCRTCInputStream> list) {
        }

        /**
         * 远端用户加入房间通知
         * 有用户加入的回调，此时 user 不包含任何资源，只是标记有人加入，此时无法订阅这个人的流。
         *
         * @param rcrtcRemoteUser 远端用户
         */
        @Override
        public void onUserJoined(final RCRTCRemoteUser rcrtcRemoteUser) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MeetingActivity.this, ("用户:" + rcrtcRemoteUser.getUserId() + "加入会议"), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        /**
         * 用户离开房间
         *
         * @param rcrtcRemoteUser 远端用户
         */
        public void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser) {
            try {
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
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUserOffline(RCRTCRemoteUser rcrtcRemoteUser) {
        }

        @Override
        public void onPublishLiveStreams(List<RCRTCInputStream> list) {
        }

        @Override
        public void onUnpublishLiveStreams(List<RCRTCInputStream> list) {
        }

        /**
         * 自己退出房间。 例如断网退出等
         *
         * @param i 状态码
         */
        @Override
        public void onLeaveRoom(int i) {
            leaveRoom();
        }
    };

    /**
     * 配置rtc sdk
     */
    public void config(Context context, boolean isEncryption) {
        RCRTCConfig.Builder configBuilder = RCRTCConfig.Builder.create();
        //是否自动重连
//        configBuilder.enableAutoReconnect(true);
        // 是否硬解码
        configBuilder.enableHardwareDecoder(true);
        // 是否硬编码
        configBuilder.enableHardwareEncoder(true);
        // 是否使用自定义加密
        configBuilder.enableAudioEncryption(isEncryption);
        configBuilder.enableVideoEncryption(isEncryption);
        //设置本地视频采集的颜色空间， 默认 true : texture 方式采集，false : yuv 方式采集
//        configBuilder.enableEncoderTexture(false);

        // init 需结合 uninit 使用，否则有些配置无法重新初始化
        RCRTCEngine.getInstance().unInit();
        RCRTCEngine.getInstance().init(context, configBuilder.build());

        RCRTCVideoStreamConfig.Builder videoConfigBuilder = RCRTCVideoStreamConfig.Builder.create();
        // 设置分辨率
        videoConfigBuilder.setVideoResolution(RCRTCParamsType.RCRTCVideoResolution.RESOLUTION_720_1280);
        // 设置帧率
        videoConfigBuilder.setVideoFps(RCRTCParamsType.RCRTCVideoFps.Fps_30);
        /**
         * 设置最小码率，可根据分辨率RCRTCVideoResolution设置
         * {@link RCRTCParamsType.RCRTCVideoResolution)}
         */
        videoConfigBuilder.setMinRate(250);
        /**
         * 设置最大码率，可根据分辨率RCRTCVideoResolution设置
         * {@link RCRTCParamsType.RCRTCVideoResolution)}
         */
        videoConfigBuilder.setMaxRate(2200);
        RCRTCEngine.getInstance().getDefaultVideoStream().setVideoConfig(videoConfigBuilder.build());

        // 听筒播放
        RCRTCEngine.getInstance().enableSpeaker(false);
    }

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
        config(this, isEncryption);
        joinRoom(roomId);
    }

    public void joinRoom(String roomId) {
        RCRTCRoomConfig roomConfig = RCRTCRoomConfig.Builder.create()
                // 根据实际场景，选择音视频直播：LIVE_AUDIO_VIDEO 或音频直播：LIVE_AUDIO
                .setRoomType(RCRTCRoomType.MEETING)
                .build();
        RCRTCEngine.getInstance().joinRoom(roomId, roomConfig, new IRCRTCResultDataCallback<RCRTCRoom>() {
            @Override
            public void onSuccess(final RCRTCRoom rcrtcRoom) {
                //美声
                RCRTCVoiceBeautifierEngine.getInstance().enable(true, new IRCRTCResultCallback() {
                    @Override
                    public void onFailed(RTCErrorCode errorCode) {
                        Log.e(TAG,"--> 变音 onFailed errorCode="+errorCode);
                    }

                    @Override
                    public void onSuccess() {
                        RCRTCVoiceBeautifierEngine.getInstance().setPreset(RCRTCVoiceBeautifierPreset.GIRL);

                    }
                });

                // 远端用户，本地用户相关资源的获取都依赖RtcRoom
                rtcRoom = rcrtcRoom;
                // 注册房间回调
                rcrtcRoom.registerRoomListener(roomEventsListener);
                try {
                    // 加入房间成功，在 UI 线程设置本地用户显示的 View
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RCRTCVideoView rongRTCVideoView = new MeetVideoView(getApplicationContext()) {

                            };
                            RCRTCEngine.getInstance().getDefaultVideoStream().setVideoView(rongRTCVideoView);
                            flLocalUser.addView(rongRTCVideoView);
                            // 开始推流，本地用户发布
                            publishDefaultAVStream();
                            // 主动订阅远端用户发布的资源
                            subscribeAVStream();
                        }
                    });
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                try {
                    Log.e(TAG, "--> onJoinRoomFailed");
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 发布默认流
     */
    public void publishDefaultAVStream() {
        if (rtcRoom == null) {
            return;
        }
        RCRTCEngine.getInstance().getDefaultVideoStream().startCamera(null);
        rtcRoom.getLocalUser().publishDefaultStreams(new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                try {
                    Log.d(TAG, "--> onPublishSuccess");
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                try {
                    Log.d(TAG, "--> onPublishFailed");
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 主动订阅远端用户发布的流
     * 视频流需要用户设置用于显示载体的videoview
     */
    public void subscribeAVStream() {
        if (rtcRoom == null || rtcRoom.getRemoteUsers() == null) {
            return;
        }
        final List<RCRTCInputStream> inputStreams = new ArrayList<>();
        for (final RCRTCRemoteUser remoteUser : rtcRoom.getRemoteUsers()) {
            if (remoteUser.getStreams().size() == 0) {
                continue;
            }
            List<RCRTCInputStream> userStreams = remoteUser.getStreams();
            for (RCRTCInputStream inputStream : userStreams) {
                if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                    // 选择订阅大流或是小流。默认小流
                    ((RCRTCVideoInputStream) inputStream).setStreamType(RCRTCStreamType.NORMAL);
                }
            }
            inputStreams.addAll(remoteUser.getStreams());
        }

        if (inputStreams.size() == 0) {
            return;
        }
        rtcRoom.getLocalUser().subscribeStreams(inputStreams, new IRCRTCResultCallback() {
            @Override
            public void onSuccess() {
                try {
                    // 订阅远端用户发布资源成功，设置显示的 view，在 UI 线程中执行
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RCRTCVideoView videoView = new MeetVideoView(getApplicationContext()) {

                            };
                            for (RCRTCInputStream inputStream : inputStreams) {
                                if (inputStream.getMediaType() == RCRTCMediaType.VIDEO) {
                                    ((RCRTCVideoInputStream) inputStream).setVideoView(videoView);
                                    // 将远端视图添加至布局
                                    MeetingActivity.this.flRemoteUser.addView(videoView);
                                    break;
                                }
                            }
                        }
                    });
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(RTCErrorCode errorCode) {
                try {
                    Log.e(TAG,"--> onSubscribeFailed");
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveRoom();
        flFullscreen.removeAllViews();
        flLocalUser.removeAllViews();
        flRemoteUser.removeAllViews();
        RCRTCEngine.getInstance().unInit();
    }

    public void leaveRoom() {
        RCRTCEngine.getInstance().leaveRoom(new IRCRTCResultCallback() {
            @Override
            public void onFailed(RTCErrorCode rtcErrorCode) {
                Log.d(TAG, "--> leaveRoom - onFailed");
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "--> leaveRoom - onSuccess");
            }
        });
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
