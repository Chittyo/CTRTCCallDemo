package com.example.myapplication.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;


import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
//import com.ruaho.cochat.rong.RongCloudManager;
//import com.ruaho.cochat.ui.activity.BaseActivity;
//import com.ruaho.echat.R;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.calllib.IRongCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.imlib.model.Conversation;

public class TestActivity extends AppCompatActivity implements View.OnClickListener{

    public static void start(Context context) {
        Intent intent = new Intent(context, TestActivity.class);
        context.startActivity(intent);
    }

    private String callId,groupId;
    private boolean isGroup,isVideo;

    private static final String TAG = "TestActivity";
    private static CallStatus currentStatus = CallStatus.OnFirst;


    private FrameLayout local;
    private FrameLayout remote;
    private TextView tv_hang_up;
    private LinearLayout ll_hangUp,ll_accept;
    private ImageView hang_up,accept;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
//        setFullScreenWindowLayout(getWindow());//todo
        initView();
        RongCallClient.getInstance().setVoIPCallListener(callListener);
        getIntentsData();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        RongCallClient.getInstance().setVoIPCallListener(null);
    }

    private void initView() {
        local = findViewById(R.id.local);
        remote = findViewById(R.id.remote);

        tv_hang_up = findViewById(R.id.tv_hang_up);
        accept = findViewById(R.id.iv_accept);
        hang_up = findViewById(R.id.iv_hang_up);
        ll_accept = findViewById(R.id.aty_rong_video_accept);
        ll_hangUp = findViewById(R.id.aty_rong_video_hang_up);
        accept.setOnClickListener(this);
        hang_up.setOnClickListener(this);
    }

    private void getIntentsData() {
        if (getIntent().getBooleanExtra("waitCall",false)) {
            currentStatus = CallStatus.BeCall;
        }else {
            callId = getIntent().getStringExtra("toUserIds");
            groupId = getIntent().getStringExtra("groupId");
            isGroup = getIntent().getBooleanExtra("isGroup",false);
            isVideo = getIntent().getBooleanExtra("isVideo",true);
//            RongCloudManager.startCall(callId,groupId,isGroup,!isVideo);//todo
            startCall(callId,groupId,isGroup,!isVideo);
//            RongCallClient.getInstance().startCall(conversationType, targetGroupId, userIds, null, mediaType, extra);
        }
//        changeUi();
    }

    /**
     * 发起单/多人通话
     * @param userIds 用户USER_CODE(多人用','拼接)
     * @param groupId 群组ID
     * @param isGroup 是否为群组
     * @param callType 通话类型:true-语音,false-视频
     */
    public static void startCall(String userIds,String groupId,boolean isGroup,boolean callType){
        String targetId = isGroup ? groupId : userIds;
        List<String> _userIds = new ArrayList<>();
        if (isGroup) {
            String[] userId = userIds.split(",");
            for (int i = 0; i < userId.length; i++) {
                _userIds.add(userId[i]);
            }
        }else {
            _userIds.add(userIds);
        }
        RongCallClient.getInstance().startCall(
                isGroup ? Conversation.ConversationType.GROUP : Conversation.ConversationType.PRIVATE,
                targetId,_userIds,null,
                callType ? RongCallCommon.CallMediaType.AUDIO : RongCallCommon.CallMediaType.VIDEO,
                null);
    }


    public static void startVideo(Context context, String userIds, String groupId, boolean isGroup, boolean isVideo) {
        Intent intent = new Intent(context, TestActivity.class);
        intent.putExtra("toUserIds",userIds);
        intent.putExtra("groupId",groupId);
        intent.putExtra("isGroup",isGroup);
        intent.putExtra("isVideo",isVideo);
        context.startActivity(intent);
    }

    public static void startVideo(Context context,boolean waitCall) {
        Intent intent = new Intent(context, TestActivity.class);
        intent.putExtra("waitCall",waitCall);
        context.startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_accept:
                acceptCall();
                break;
            case R.id.iv_hang_up:
                hangUpCall();
                finish();
                break;
        }
    }

    enum CallStatus {
        OnFirst,
        Idle,
        Calling,
        BeCall,
        OnCall
    }

    private IRongCallListener callListener = new IRongCallListener() {

        private void addLocalView(SurfaceView view) {
            local.removeAllViews();
            if (view != null){
                view.setZOrderOnTop(true);
                local.addView(view);
            }
        }

        private void addRemoteView(SurfaceView view) {
            remote.removeAllViews();
            if (view != null){
                remote.addView(view);
            }
        }

        private void clearViews() {
            local.removeAllViews();
            remote.removeAllViews();
        }

        /**
         * 电话已拨出
         *
         * @param session 通话实体
         * @param local 本地 camera 信息
         */
        @Override
        public void onCallOutgoing(RongCallSession session, SurfaceView local) {
            Log.d(TAG, "onCallOutgoing");
            currentStatus = CallStatus.Calling;
            changeUi();
        }

        /**
         * 已建立通话
         *
         * @param session 通话实体
         * @param local 本地 camera 信息
         */
        @Override
        public void onCallConnected(RongCallSession session, SurfaceView local) {
            Log.d(TAG, "onCallConnected");
            currentStatus = CallStatus.OnCall;
            changeUi();
            addLocalView(local);
        }

        /**
         * 通话结束
         *
         * @param session 通话实体
         * @param reason 通话中断原因
         */
        @Override
        public void onCallDisconnected(RongCallSession session, RongCallCommon.CallDisconnectedReason reason) {
            Log.d(TAG, "onCallDisconnected reason = " + reason);
            currentStatus = CallStatus.Idle;
            changeUi();
            clearViews();
        }

        /**
         * 被叫端正在振铃
         *
         * @param uid 振铃端用户id
         */
        @Override
        public void onRemoteUserRinging(String uid) {
            Log.d(TAG, "onRemoteUserRinging uid = " + uid);

        }

        @Override
        public void onRemoteUserAccept(String userId, RongCallCommon.CallMediaType mediaType) {

        }

        /**
         * 被叫端加入通话
         *
         * @param uid 加入的用户id
         * @param type 加入用户的媒体类型
         * @param ut 加入用户的类型
         * @param view 加入用户者的 camera 信息
         */
        @Override
        public void onRemoteUserJoined(String uid, RongCallCommon.CallMediaType type, int ut, SurfaceView view) {
            Log.d(TAG, "onRemoteUserRinging uid = " + uid);
            addRemoteView(view);
        }

        /**
         * 被叫端离开通话
         *
         * @param uid 离开的用户id
         * @param reason 离开原因
         */
        @Override
        public void onRemoteUserLeft(String uid, RongCallCommon.CallDisconnectedReason reason) {
            Log.d(TAG, "onRemoteUserLeft uid = " + uid);
            currentStatus = CallStatus.Idle;
            changeUi();
            clearViews();
        }

        /**
         * 通话过程中发生异常
         *
         * @param code 异常原因
         */
        @Override
        public void onError(RongCallCommon.CallErrorCode code) {
            Log.e(TAG, "onError code = " + code);
            currentStatus = CallStatus.Idle;
            changeUi();
            clearViews();
        }

        @Override
        public void onRemoteUserInvited(String uid, RongCallCommon.CallMediaType type) {
            Log.d(TAG, "onRemoteUserInvited uid = " + uid);
        }

        @Override
        public void onMediaTypeChanged(String uid, RongCallCommon.CallMediaType type, SurfaceView video) {
            Log.d(TAG, "onMediaTypeChanged uid = " + uid + ", type = " + type);
        }

        @Override
        public void onRemoteCameraDisabled(String uid, boolean disabled) {
            Log.d(TAG, "onRemoteCameraDisabled uid = " + uid + ", disabled = " + disabled);
        }

        @Override
        public void onRemoteMicrophoneDisabled(String uid, boolean disabled) {
            Log.d(TAG, "onRemoteMicrophoneDisabled uid = " + uid + ", disabled = " + disabled);
        }

        @Override
        public void onRemoteUserPublishVideoStream(String uid, String sid, String tag, SurfaceView surfaceView) {
            Log.d(TAG, "onRemoteUserPublishVideoStream uid = " + uid + ", sid = " + sid + ", tag = " + tag);
        }

        @Override
        public void onRemoteUserUnpublishVideoStream(String uid, String sid, String tag) {
            Log.d(TAG, "onRemoteUserUnpublishVideoStream uid = " + uid + ", sid = " + sid + ", tag = " + tag);
        }

        @Override
        public void onFirstRemoteVideoFrame(String uid, int height, int width) {
            Log.d(TAG, "onFirstRemoteVideoFrame uid = " + uid + ", height = " + height + ", width = " + width);
        }

        @Override
        public void onNetworkSendLost(int lossRate, int delay) {
            Log.d(TAG, "onNetworkSendLost lossRate = " + lossRate + ", delay = " + delay);
        }

        @Override
        public void onNetworkReceiveLost(String uid, int lossRate) {
            Log.d(TAG, "onNetworkReceiveLost uid = " + uid + ", lossRate = " + lossRate);
        }

        @Override
        public void onAudioLevelSend(String level) {
            Log.d(TAG, "onAudioLevelSend level = " + level);
        }

        @Override
        public void onAudioLevelReceive(HashMap<String, String> levels) {
            Log.d(TAG, "onAudioLevelReceive levels = " + levels);
        }
    };

    private void changeUi() {
        if (CallStatus.Idle == currentStatus) {
            finish();
            ll_hangUp.setVisibility(View.GONE);
            ll_accept.setVisibility(View.GONE);
        } else if (CallStatus.Calling == currentStatus) {
//            statusTextView.setText("呼叫中");
            tv_hang_up.setText("取消");
            ll_hangUp.setVisibility(View.VISIBLE);
            ll_accept.setVisibility(View.GONE);
        } else if (CallStatus.BeCall == currentStatus) {
//            statusTextView.setText("有人找你");
            tv_hang_up.setText("拒绝");
            ll_hangUp.setVisibility(View.VISIBLE);
            ll_accept.setVisibility(View.VISIBLE);
        } else if (CallStatus.OnCall == currentStatus) {
//            statusTextView.setText("通话中");
            tv_hang_up.setText("挂断");
            ll_hangUp.setVisibility(View.VISIBLE);
            ll_accept.setVisibility(View.GONE);
        }else {
            ll_hangUp.setVisibility(View.GONE);
            ll_accept.setVisibility(View.GONE);
        }
    }

    private void acceptCall() {
        if (RongCallClient.getInstance() != null && RongCallClient.getInstance().getCallSession() != null) {
            RongCallClient.getInstance().acceptCall(RongCallClient.getInstance().getCallSession().getCallId());
        }
    }

    private void hangUpCall() {
        if (RongCallClient.getInstance() != null && RongCallClient.getInstance().getCallSession() != null) {
            RongCallClient.getInstance().hangUpCall(RongCallClient.getInstance().getCallSession().getCallId());
        }
    }
}

