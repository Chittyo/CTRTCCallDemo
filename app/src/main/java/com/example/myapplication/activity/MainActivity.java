package com.example.myapplication.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.App;
import com.example.myapplication.R;
import com.example.myapplication.common.MockAppServer;
import com.example.myapplication.common.UiUtils;

import java.util.ArrayList;

import io.rong.calllib.RongCallClient;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getName();
    private Button btnConnectIMServer, btnRTCConnectIMServer, btnConnectIMServerCallLib, btnTest;
    private RadioGroup rgUsers;
    private EditText etRoomId;
    private String token001 = "Enzbxdr7hdO6WSr+DZFARkaUNlc6QSw8FXTjaWyaqiE=@poxt.cn.rongnav.com;poxt.cn.rongcfg.com";
    private String token002 = "DmWWu3/S6666WSr+DZFARi8Xh3c+CQbNFXTjaWyaqiE=@poxt.cn.rongnav.com;poxt.cn.rongcfg.com";
    private String token003 = "nXbwPYS2HrC6WSr+DZFARjhhokdvXIDdFXTjaWyaqiE=@poxt.cn.rongnav.com;poxt.cn.rongcfg.com";
    private String userId = "001", token = token001;
    private String roomId = "1001";
    private CallType callType = CallType.CALLKIT;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnectIMServer = findViewById(R.id.btnCallKitConnectIMServer);
        btnRTCConnectIMServer = findViewById(R.id.btnRTCConnectIMServer);
        rgUsers = findViewById(R.id.rgUsers);
        etRoomId = findViewById(R.id.etRoomId);
        btnConnectIMServerCallLib = findViewById(R.id.btnCallLibConnectIMServer);
        btnTest = findViewById(R.id.btnTest);

        btnConnectIMServer.setOnClickListener(this);
        btnRTCConnectIMServer.setOnClickListener(this);
        btnConnectIMServerCallLib.setOnClickListener(this);
        btnTest.setOnClickListener(this);

        rgUsers.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rbUser001:
                        userId = "001";
                        token = token001;//测试暂写死token
                        break;
                    case R.id.rbUser002:
                        userId = "002";
                        token = token002;//测试暂写死token
                        break;
                    case R.id.rbUser003:
                        userId = "003";
                        token = token003;//测试暂写死token
                        break;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // 音视频功能所需权限检测
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissionList = {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
            };
            ArrayList<String> ungrantedPermissions = new ArrayList<>();
            for (String permission : permissionList) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ungrantedPermissions.add(permission);
                }
            }
            if (!ungrantedPermissions.isEmpty()) {
                String[] array = new String[ungrantedPermissions.size()];
                ActivityCompat.requestPermissions(this, ungrantedPermissions.toArray(array), 0);
                return false;
            }
        }
        return true;
    }

    // 根据用户所填 UserID，模拟开发者从 App Server 获取 Token。
    private void getTokenFromAppServer() {
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "UserID 不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        UiUtils.showWaitingDialog(this);
        MockAppServer.getToken(App.APP_KEY, App.APP_SECRET, userId, new MockAppServer.GetTokenCallback() {

            @Override
            public void onGetTokenSuccess(String token) {
                Log.d(TAG, "--> onGetTokenSuccess() token = " + token);
                if (callType == CallType.CALLKIT){
                    callKitConnectIMServer(token);
                }else if (callType == CallType.RTCLIB){
                    rtcConnectIMServer(token);
                }else {
                    callLibConnectIMServer(token);
                }
            }

            @Override
            public void onGetTokenFailed(String code) {
                UiUtils.hideWaitingDialog();
                Log.e(TAG,"--> onGetTokenFailed() 获取 Token 失败，code = " + code);
            }
        });
    }

    /**
     * 音视频通话(CallKit)
     * @param token
     */
    private void callKitConnectIMServer(String token) {
        // 关键步骤 2：使用从 App Server 获取的代表 UserID 身份的 Token 字符串，连接融云 IM 服务。
        RongIM.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onSuccess(String s) {
                UiUtils.hideWaitingDialog();
                roomId = etRoomId.getText().toString().trim();
                if (TextUtils.isEmpty(roomId)) {
                    Toast.makeText(MainActivity.this, "房间号不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                CallKitActivity.start(MainActivity.this, roomId, userId);
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode errorCode) {
                Log.e(TAG, "--> callKitConnectIMServer - onError - 连接融云 IM 服务失败，code = " + errorCode);
                UiUtils.hideWaitingDialog();
                if(errorCode.equals(RongIMClient.ConnectionErrorCode.RC_CONN_TOKEN_INCORRECT)) {
                    //从 APP 服务获取新 token，并重连
                    getTokenFromAppServer();
                }else if (errorCode.equals(RongIMClient.ConnectionErrorCode.RC_CONNECTION_EXIST)){
                    //连接已存在
                    roomId = etRoomId.getText().toString().trim();
                    if (TextUtils.isEmpty(roomId)) {
                        Toast.makeText(MainActivity.this, "房间号不能为空！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    CallKitActivity.start(MainActivity.this, roomId, userId);
                } else {
                    //无法连接 IM 服务器，请根据相应的错误码作出对应处理
                }
            }

            @Override
            public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus databaseOpenStatus) {
                Log.d(TAG, "--> callKitConnectIMServer - onDatabaseOpened databaseOpenStatus = "+databaseOpenStatus);
            }
        });
    }

    /**
     * 音视频会议
     * @param token
     */
    private void rtcConnectIMServer(String token) {
        // 使用从 App Server 获取的代表 UserID 身份的 Token 字符串，连接融云 IM 服务。
        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onSuccess(String s) {
                UiUtils.hideWaitingDialog();
                roomId = etRoomId.getText().toString().trim();
                if (TextUtils.isEmpty(roomId)) {
                    Toast.makeText(MainActivity.this, "房间号不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                MeetingPrepareActivity.start(MainActivity.this, roomId, userId);
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode errorCode) {
                Log.e(TAG, "--> rtcConnectIMServer - onError - 连接融云 IM 服务失败，code = " + errorCode);
                UiUtils.hideWaitingDialog();
                if(errorCode.equals(RongIMClient.ConnectionErrorCode.RC_CONN_TOKEN_INCORRECT)) {
                    //从 APP 服务获取新 token，并重连
                    getTokenFromAppServer();
                }else if (errorCode.equals(RongIMClient.ConnectionErrorCode.RC_CONNECTION_EXIST)){
                    //连接已存在
                    roomId = etRoomId.getText().toString().trim();
                    if (TextUtils.isEmpty(roomId)) {
                        Toast.makeText(MainActivity.this, "房间号不能为空！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    MeetingPrepareActivity.start(MainActivity.this, roomId, userId);
                } else {
                    //无法连接 IM 服务器，请根据相应的错误码作出对应处理
                }
            }

            @Override
            public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus databaseOpenStatus) {
                //消息数据库打开，可以进入到主页面
                Log.d(TAG, "--> rtcConnectIMServer - onDatabaseOpened databaseOpenStatus = "+databaseOpenStatus);// DATABASE_OPEN_SUCCESS
            }
        });
    }

    private void callLibConnectIMServer(String token) {
        RongIMClient.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus code) {
                //消息数据库打开，可以进入到主页面
                Log.d(TAG, "--> callLibConnectIMServer - onDatabaseOpened DatabaseOpenStatus = "+code);// DATABASE_OPEN_SUCCESS

            }

            @Override
            public void onSuccess(String s) {
                //连接成功
                UiUtils.hideWaitingDialog();
                roomId = etRoomId.getText().toString().trim();
                if (TextUtils.isEmpty(roomId)) {
                    Toast.makeText(MainActivity.this, "房间号不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                CallLibActivity.start(MainActivity.this);
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode errorCode) {
                Log.e(TAG, "--> callLibConnectIMServer - onError - 连接融云 IM 服务失败，code = " + errorCode);
                UiUtils.hideWaitingDialog();
                if(errorCode.equals(RongIMClient.ConnectionErrorCode.RC_CONN_TOKEN_INCORRECT)) {
                    //从 APP 服务获取新 token，并重连
                    getTokenFromAppServer();
                }else if (errorCode.equals(RongIMClient.ConnectionErrorCode.RC_CONNECTION_EXIST)){
                    //连接已存在
                    roomId = etRoomId.getText().toString().trim();
                    if (TextUtils.isEmpty(roomId)) {
                        Toast.makeText(MainActivity.this, "房间号不能为空！", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    CallLibActivity.start(MainActivity.this);
                } else {
                    //无法连接 IM 服务器，请根据相应的错误码作出对应处理
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCallKitConnectIMServer://音视频通话(CallKit)
                callType = CallType.CALLKIT;
                if (checkPermission()) {
//                    getTokenFromAppServer();
                    callKitConnectIMServer(token);
                }
                break;
            case R.id.btnRTCConnectIMServer://音视频会议
                callType = CallType.RTCLIB;
                if (checkPermission()) {
//                    getTokenFromAppServer();
                    rtcConnectIMServer(token);
                }
                break;
            case R.id.btnCallLibConnectIMServer://音视频通话(CallLib)
                callType = CallType.CALLLIB;
                if (checkPermission()) {
//                    getTokenFromAppServer();
                    callLibConnectIMServer(token);
                }
                break;
            case R.id.btnTest:
                //测试
                TestActivity.start(MainActivity.this);
                break;
        }
    }

    enum CallType {
        CALLKIT,
        RTCLIB,
        CALLLIB
    }
}
