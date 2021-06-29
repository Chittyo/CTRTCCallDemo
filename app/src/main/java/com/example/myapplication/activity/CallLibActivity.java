package com.example.myapplication.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import com.example.myapplication.R;
import com.example.myapplication.base.AppManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.calllib.IRongCallListener;
import io.rong.calllib.IRongReceivedCallListener;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.imlib.model.Conversation;

public class CallLibActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = CallLibActivity.class.getName();

    private FrameLayout flLocal, flRemote;
    private EditText etTargetUserId, etTargetUserId2, etTargetGroupId;
    private Button btnCall, btnAccept, btnHangUp;
    private TextView tvStatus;
    private RadioGroup rgUsers;
    private Group groupIds;

    private static CallStatus currentStatus = CallStatus.Idle;
    private Conversation.ConversationType conversationType = Conversation.ConversationType.PRIVATE;

    private IRongReceivedCallListener receivedCallListener = new IRongReceivedCallListener() {

        /**
         * 来电回调
         * @param callSession 通话实体
         */
        @Override
        public void onReceivedCall(RongCallSession callSession) {
            Log.d(TAG, "--> onReceivedCall");
            currentStatus = CallStatus.BeCall;
            changeStatus();
        }

        /**
         * targetSDKVersion 大于等于 23 时检查权限的回调。当 targetSDKVersion 小于 23 的时候不需要实现。
         * 在这个回调里用户需要使用Android6.0新增的动态权限分配接口requestCallPermissions通知用户授权，
         * 然后在onRequestPermissionResult回调里根据用户授权或者不授权分别回调
         * RongCallClient.getInstance().onPermissionGranted()和
         * RongCallClient.getInstance().onPermissionDenied()来通知CallLib。

         * @param callSession 通话实体
         */
        @Override
        public void onCheckPermission(RongCallSession callSession) {

        }
    };

    private IRongCallListener callListener = new IRongCallListener() {

        private void addLocalView(SurfaceView view) {
            flLocal.removeAllViews();
            flLocal.addView(view);
        }

        private void addRemoteView(SurfaceView view) {
            flRemote.removeAllViews();
            flRemote.addView(view);
        }

        private void clearViews() {
            flLocal.removeAllViews();
            flRemote.removeAllViews();
        }

        /**
         * 电话已拨出。
         * 主叫端拨出电话后，通过回调 onCallOutgoing 通知当前 call 的详细信息。
         *
         * @param callSession 通话实体。
         * @param localVideo  本地 camera 信息。
         */
        @Override
        public void onCallOutgoing(RongCallSession callSession, SurfaceView localVideo) {
            Log.d(TAG, "--> onCallOutgoing");
            currentStatus = CallStatus.Calling;
            changeStatus();
        }

        /**
         * 已建立通话。
         * 通话接通时，通过回调 onCallConnected 通知当前 call 的详细信息。
         *
         * @param callSession 通话实体。
         * @param localVideo  本地 camera 信息。
         */
        @Override
        public void onCallConnected(RongCallSession callSession, SurfaceView localVideo) {
            Log.d(TAG, "--> onCallConnected");
            currentStatus = CallStatus.OnCall;
            changeStatus();
            addLocalView(localVideo);
        }

        /**
         * 通话结束。
         * 通话中，对方挂断，己方挂断，或者通话过程网络异常造成的通话中断，都会回调 onCallDisconnected。
         *
         * @param callSession 通话实体。
         * @param reason      通话中断原因。
         */
        @Override
        public void onCallDisconnected(RongCallSession callSession, RongCallCommon.CallDisconnectedReason reason) {
            Log.d(TAG, "--> onCallDisconnected reason = " + reason);
            currentStatus = CallStatus.Idle;
            changeStatus();
            clearViews();
        }

        /**
         * 被叫端正在振铃
         *
         * @param uid 振铃端用户id
         */
        @Override
        public void onRemoteUserRinging(String uid) {
            Log.d(TAG, "--> onRemoteUserRinging uid = " + uid);

        }

        /**
         * 被叫端加入通话。
         * 主叫端拨出电话，被叫端收到请求后，加入通话，回调 onRemoteUserJoined。
         *
         * @param userId      加入用户的 id。<br />
         * @param mediaType   加入用户的媒体类型，audio or video。<br />
         * @param userType    加入用户的类型，1:正常用户,2:观察者。<br />
         * @param remoteVideo 加入用户者的 camera 信息。如果 userType为2，remoteVideo对象为空；<br />
         *                    如果对端调用{@link RongCallClient#startCall(int, boolean, Conversation.ConversationType, String, List, List, RongCallCommon.CallMediaType, String, StartCameraCallback)} 或
         *                    {@link RongCallClient#acceptCall(String, int, boolean, StartCameraCallback)}开始的音视频通话，则可以使用如下设置改变对端视频流的镜像显示：<br />
         *                    <pre class="prettyprint">
         *                                            public void onRemoteUserJoined(String userId, RongCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {
         *                                                 if (null != remoteVideo) {
         *                                                     ((RongRTCVideoView) remoteVideo).setMirror( boolean);//观看对方视频流是否镜像处理
         *                                                 }
         *                                            }
         *                                            </pre>
         */
        @Override
        public void onRemoteUserJoined(String userId, RongCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {
            Log.d(TAG, "--> onRemoteUserRinging uid = " + userId);
            addRemoteView(remoteVideo);
        }

        /**
         * 通话中的远端参与者离开。
         * 回调 onRemoteUserLeft 通知状态更新。
         *
         * @param userId 远端参与者的 id。(离开的用户id)
         * @param reason 远端参与者离开原因。
         */
        @Override
        public void onRemoteUserLeft(String userId, RongCallCommon.CallDisconnectedReason reason) {
            Log.d(TAG, "--> onRemoteUserLeft userId = " + userId);
            currentStatus = CallStatus.Idle;
            changeStatus();
            clearViews();
        }

        /**
         * 通话过程中发生异常
         *
         * @param code 异常原因
         */
        @Override
        public void onError(RongCallCommon.CallErrorCode code) {
            Log.d(TAG, "--> onError code = " + code);
            currentStatus = CallStatus.Idle;
            changeStatus();
            clearViews();
        }

        @Override
        public void onRemoteUserInvited(String uid, RongCallCommon.CallMediaType type) {
            Log.d(TAG, "--> onRemoteUserInvited uid = " + uid);
        }

        @Override
        public void onMediaTypeChanged(String uid, RongCallCommon.CallMediaType type, SurfaceView video) {
            Log.d(TAG, "--> onMediaTypeChanged uid = " + uid + ", type = " + type);
        }

        @Override
        public void onRemoteCameraDisabled(String uid, boolean disabled) {
            Log.d(TAG, "--> onRemoteCameraDisabled uid = " + uid + ", disabled = " + disabled);
        }

        @Override
        public void onRemoteMicrophoneDisabled(String uid, boolean disabled) {
            Log.d(TAG, "--> onRemoteMicrophoneDisabled uid = " + uid + ", disabled = " + disabled);
        }

        @Override
        public void onRemoteUserPublishVideoStream(String uid, String sid, String tag, SurfaceView surfaceView) {
            Log.d(TAG, "--> onRemoteUserPublishVideoStream uid = " + uid + ", sid = " + sid + ", tag = " + tag);
        }

        @Override
        public void onRemoteUserUnpublishVideoStream(String uid, String sid, String tag) {
            Log.d(TAG, "--> onRemoteUserUnpublishVideoStream uid = " + uid + ", sid = " + sid + ", tag = " + tag);
        }

        @Override
        public void onFirstRemoteVideoFrame(String uid, int height, int width) {
            Log.d(TAG, "--> onFirstRemoteVideoFrame uid = " + uid + ", height = " + height + ", width = " + width);
        }

        @Override
        public void onNetworkSendLost(int lossRate, int delay) {
            Log.d(TAG, "--> onNetworkSendLost lossRate = " + lossRate + ", delay = " + delay);
        }

        @Override
        public void onNetworkReceiveLost(String uid, int lossRate) {
            Log.d(TAG, "--> onNetworkReceiveLost uid = " + uid + ", lossRate = " + lossRate);
        }

        @Override
        public void onAudioLevelSend(String level) {
            Log.d(TAG, "--> onAudioLevelSend level = " + level);
        }

        @Override
        public void onAudioLevelReceive(HashMap<String, String> levels) {
            Log.d(TAG, "--> onAudioLevelReceive levels = " + levels);
        }
    };

    public static void start(Context context) {
        Intent intent = new Intent(context, CallLibActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_lib);

        initView();
        changeStatus();
        registerCallListener();
        AppManager.getAppManager().addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterCallListener();
        AppManager.getAppManager().removeActivity(this);
    }

    private void registerCallListener() {
        //来电监听
        RongCallClient.setReceivedCallListener(receivedCallListener);
        //通话状态监听
        RongCallClient.getInstance().setVoIPCallListener(callListener);
    }

    private void unRegisterCallListener() {
        RongCallClient.setReceivedCallListener(null);
        RongCallClient.getInstance().setVoIPCallListener(null);
    }

    private void initView() {
        flLocal = findViewById(R.id.flLocal);
        flRemote = findViewById(R.id.flRemote);

        etTargetUserId = findViewById(R.id.etTargetUserId);
        etTargetUserId2 = findViewById(R.id.etTargetUserId2);
        etTargetGroupId = findViewById(R.id.etTargetGroupId);
        btnCall = findViewById(R.id.btnCall);
        tvStatus = findViewById(R.id.tvStatus);
        btnAccept = findViewById(R.id.btnAccept);
        btnHangUp = findViewById(R.id.btnHangUp);
        rgUsers = findViewById(R.id.rgUsers);
        groupIds = findViewById(R.id.groupIds);

        btnCall.setOnClickListener(this);
        btnAccept.setOnClickListener(this);
        btnHangUp.setOnClickListener(this);

        rgUsers.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rbSingle:
                        groupIds.setVisibility(View.GONE);
                        conversationType = Conversation.ConversationType.PRIVATE;
                        break;
                    case R.id.rbDouble:
                        groupIds.setVisibility(View.VISIBLE);
                        conversationType = Conversation.ConversationType.GROUP;
                        break;

                }
            }
        });
    }

    private void changeStatus() {
        if (CallStatus.Idle == currentStatus) {
            btnCall.setEnabled(true);
            tvStatus.setText("");
            btnAccept.setEnabled(false);
            btnHangUp.setEnabled(false);
        } else if (CallStatus.Calling == currentStatus) {
            btnCall.setEnabled(false);
            tvStatus.setText("呼叫中");
            btnHangUp.setEnabled(true);
            btnAccept.setEnabled(false);
        } else if (CallStatus.BeCall == currentStatus) {
            btnCall.setEnabled(false);
            tvStatus.setText("有人找~");
            btnHangUp.setEnabled(true);
            btnAccept.setEnabled(true);
        } else if (CallStatus.OnCall == currentStatus) {
            btnCall.setEnabled(false);
            tvStatus.setText("通话中");
            btnHangUp.setEnabled(true);
            btnAccept.setEnabled(false);
        }
    }
    
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnCall) {
            call();
        } else if (id == R.id.btnAccept) {
            acceptCall();
        } else if (id == R.id.btnHangUp) {
            hangUpCall();
        }
    }

    private void hangUpCall() {
        // im未连接或者不在通话中，RongCallClient 和 RongCallSession 为空
        if (RongCallClient.getInstance() != null && RongCallClient.getInstance().getCallSession() != null) {
            RongCallClient.getInstance().hangUpCall(RongCallClient.getInstance().getCallSession().getCallId());
        }
    }

    private void acceptCall() {
        // im未连接或者不在通话中，RongCallClient 和 RongCallSession 为空
        if (RongCallClient.getInstance() != null && RongCallClient.getInstance().getCallSession() != null) {
            RongCallClient.getInstance().acceptCall(RongCallClient.getInstance().getCallSession().getCallId());
        }
    }

    //单人通话、多人通话
    private void call() {
        if (conversationType == Conversation.ConversationType.GROUP){
            //群组ID
            String targetGroupId = etTargetGroupId.getText().toString().trim();
            //群组内的用户ID集合
            String targetUserId = etTargetUserId.getText().toString().trim();
            String targetUserId2 = etTargetUserId2.getText().toString().trim();
            if (TextUtils.isEmpty(targetGroupId) || TextUtils.isEmpty(targetUserId) || TextUtils.isEmpty(targetUserId2)) {
                Toast.makeText(this, "请输入被叫用户的 userId", Toast.LENGTH_LONG).show();
                return;
            }
            List<String> userIds = new ArrayList<>();
            userIds.add(targetUserId);
            userIds.add(targetUserId2);
            RongCallCommon.CallMediaType mediaType = RongCallCommon.CallMediaType.VIDEO;
            String extra = "";
            RongCallClient.getInstance().startCall(conversationType, targetGroupId, userIds, null, mediaType, extra);
        }else {
            String targetId = etTargetUserId.getText().toString().trim();
            if (TextUtils.isEmpty(targetId)) {
                Toast.makeText(this, "请输入被叫用户的 userid", Toast.LENGTH_LONG).show();
                return;
            }
            List<String> userIds = new ArrayList<>();
            userIds.add(targetId);
            RongCallCommon.CallMediaType mediaType = RongCallCommon.CallMediaType.VIDEO;
            String extra = "";
            RongCallClient.getInstance().startCall(conversationType, targetId, userIds, null, mediaType, extra);
        }
    }

    enum CallStatus {
        Idle,
        Calling,
        BeCall,
        OnCall
    }
}
