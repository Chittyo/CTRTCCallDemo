package com.example.myapplication.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.callkit.BaseCallActivity;
import io.rong.callkit.RongCallKit;
import io.rong.calllib.CallVideoFrame;
import io.rong.calllib.IRongCallListener;
import io.rong.calllib.IVideoFrameListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.imlib.model.Conversation;

/**
 * 音视频通话
 */
public class CallKitActivity extends BaseCallActivity implements View.OnClickListener{

    private static final String TAG = CallKitActivity.class.getName();
    private Button btnMultiPersonCall, btnSinglePersonCall;
    private RadioGroup rgUsers;
    public static final String USER_ID = "user_id";
    public static final String ROOM_ID = "room_id";
    private String roomId = "1001", userId = "001", targetId = "002";

    //实时获取本端用户视频帧回调，并打印视频帧数据大小
    private IVideoFrameListener videoFrameListener = new IVideoFrameListener() {
        @Override
        public CallVideoFrame processVideoFrame(CallVideoFrame callVideoFrame) {
//            Log.e(TAG, "--> processVideoFrame 视频帧数据 callVideoFrame.getData() = "+callVideoFrame.getData());
//            Log.e(TAG, "--> processVideoFrame 视频帧数据 callVideoFrame.getCurrentCaptureDataType() = "+callVideoFrame.getCurrentCaptureDataType());
//            Log.e(TAG, "--> processVideoFrame 视频帧数据 callVideoFramecallVideoFrame.getHeight() = "+callVideoFrame.getHeight());
//            Log.e(TAG, "--> processVideoFrame 视频帧数据 callVideoFrame.getWidth() = "+callVideoFrame.getWidth());
//            Log.e(TAG, "--> processVideoFrame 视频帧数据 callVideoFrame.getOesTextureId() = "+callVideoFrame.getOesTextureId());
//            Log.e(TAG, "--> processVideoFrame 视频帧数据 callVideoFrame.getRotation() = "+callVideoFrame.getRotation());
//            Log.e(TAG, "--> processVideoFrame 视频帧数据 callVideoFrame.getTimestamp() = "+callVideoFrame.getTimestamp());
//            Log.e(TAG, "--> processVideoFrame 视频帧数据 callVideoFrame.getTransformMatrix() = "+callVideoFrame.getTransformMatrix());

            return null;
        }
    };

    public static void start(Context context, String roomId, String userId) {
        Intent intent = new Intent(context, CallKitActivity.class);
        intent.putExtra(ROOM_ID, roomId);
        intent.putExtra(USER_ID, userId);
        context.startActivity(intent);
    }

    @Override
    public void onCallOutgoing(RongCallSession callProfile, SurfaceView localVideo) {
        super.onCallOutgoing(callProfile, localVideo);
        Log.e(TAG,"--> callkit 通话监听 onCallOutgoing callProfile="+callProfile);
    }

    @Override
    public void onCallConnected(RongCallSession callProfile, SurfaceView localVideo) {
        super.onCallConnected(callProfile, localVideo);
        Log.e(TAG,"--> callkit 通话监听 onCallConnected callProfile="+callProfile);
    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_kit);

        RongCallClient.getInstance().registerVideoFrameListener(videoFrameListener);

        btnSinglePersonCall = findViewById(R.id.btnSinglePersonCall);
        btnMultiPersonCall = findViewById(R.id.btnMultiPersonCall);
        rgUsers = findViewById(R.id.rgUsers);
        btnSinglePersonCall.setOnClickListener(this);
        btnMultiPersonCall.setOnClickListener(this);

        Intent intent = getIntent();
        roomId = intent.getStringExtra(ROOM_ID);
        userId = intent.getStringExtra(USER_ID);

        rgUsers.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rbUser001:
                        targetId = "001";
                        break;
                    case R.id.rbUser002:
                        targetId = "002";
                        break;
                    case R.id.rbUser003:
                        targetId = "003";
                        break;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RongCallClient.getInstance().unregisterVideoFrameObserver();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSinglePersonCall:
                //发起单人通话
                RongCallKit.startSingleCall(CallKitActivity.this, targetId, RongCallKit.CallMediaType.CALL_MEDIA_TYPE_VIDEO);
                break;
            case R.id.btnMultiPersonCall:
                //发起多人通话，注意⚠️要现在融云控制台创建群组并加入用户才🉑️
                RongCallKit.CallMediaType mediaType = RongCallKit.CallMediaType.CALL_MEDIA_TYPE_VIDEO;
                ArrayList<String> userIds = new ArrayList<>();
                userIds.add("001");
                userIds.add("002");
                userIds.add("003");
                RongCallKit.startMultiCall(CallKitActivity.this, Conversation.ConversationType.GROUP, roomId, mediaType, userIds);
                break;
            default:
                break;

        }
    }
}
