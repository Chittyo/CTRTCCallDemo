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

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getName();
    private Button btnGetToken, btnConnectIMServer, btnRTCConnectIMServer;
    private RadioGroup rgUsers;
    private EditText etRoomId;
    private String token001 = "Enzbxdr7hdO6WSr+DZFARkaUNlc6QSw8FXTjaWyaqiE=@poxt.cn.rongnav.com;poxt.cn.rongcfg.com";
    private String token002 = "DmWWu3/S6666WSr+DZFARi8Xh3c+CQbNFXTjaWyaqiE=@poxt.cn.rongnav.com;poxt.cn.rongcfg.com";
    private String token003 = "nXbwPYS2HrC6WSr+DZFARjhhokdvXIDdFXTjaWyaqiE=@poxt.cn.rongnav.com;poxt.cn.rongcfg.com";
    private String userId = "001", token = token001;
    private String roomId = "1001";

    /*

    Xiaomi Redmi Note 7 Pro 手机：userId = 001
    HUAWEI HUAWEI NXT-DL00 手机：userId = 002
    Xiaomi M2006C3LC 手机：userId = 003

    {"code":200,"userId":"001","token":"Enzbxdr7hdO6WSr+DZFARkaUNlc6QSw8FXTjaWyaqiE=@poxt.cn.rongnav.com;poxt.cn.rongcfg.com"}

    {"code":200,"userId":"002","token":"DmWWu3/S6666WSr+DZFARi8Xh3c+CQbNFXTjaWyaqiE=@poxt.cn.rongnav.com;poxt.cn.rongcfg.com"}

     {"code":200,"userId":"003","token":"nXbwPYS2HrC6WSr+DZFARjhhokdvXIDdFXTjaWyaqiE=@poxt.cn.rongnav.com;poxt.cn.rongcfg.com"}
     */
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnGetToken = findViewById(R.id.btnGetToken);
        btnConnectIMServer = findViewById(R.id.btnConnectIMServer);
        btnRTCConnectIMServer = findViewById(R.id.btnRTCConnectIMServer);
        rgUsers = findViewById(R.id.rgUsers);
        etRoomId = findViewById(R.id.etRoomId);

        btnGetToken.setOnClickListener(this);
        btnConnectIMServer.setOnClickListener(this);
        btnRTCConnectIMServer.setOnClickListener(this);

        rgUsers.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rbUser001:
                        userId = "001";
                        token = token001;
                        break;
                    case R.id.rbUser002:
                        userId = "002";
                        token = token002;
                        break;
                    case R.id.rbUser003:
                        userId = "003";
                        token = token003;
                        break;
                }
            }
        });
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

    // 根据用户所填 UserID，模拟从开发者 App Server 获取 Token。
    private void getTokenFromAppServer() {
        Log.e(TAG,"getTokenFromAppServer");

        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(this, "UserID 不能为空！", Toast.LENGTH_SHORT).show();
            return;
        }
        UiUtils.showWaitingDialog(this);
        MockAppServer.getToken(App.APP_KEY, App.APP_SECRET, userId, new MockAppServer.GetTokenCallback() {

            @Override
            public void onGetTokenSuccess(String token) {
                Log.e(TAG, "onGetTokenSuccess() token = " + token);
                connectIMServer(token);
            }

            @Override
            public void onGetTokenFailed(String code) {
                UiUtils.hideWaitingDialog();
                Log.e(TAG,"onGetTokenFailed() 获取 Token 失败，code = " + code);
            }
        });
    }

    /**
     * 音视频通话
     * @param token
     */
    private void connectIMServer(String token) {
        Log.e(TAG,"connectIMServer");
        // 关键步骤 2：使用从 App Server 获取的代表 UserID 身份的 Token 字符串，连接融云 IM 服务。
        RongIM.connect(token, new RongIMClient.ConnectCallback() {
            @Override
            public void onSuccess(String s) {
                Log.e(TAG, "--> connectIMServer - onSuccess");
//                UiUtils.hideWaitingDialog();

                roomId = etRoomId.getText().toString().trim();
                if (TextUtils.isEmpty(roomId)) {
                    Toast.makeText(MainActivity.this, "房间号不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                CallActivity.start(MainActivity.this, roomId, userId);

            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode code) {
                Log.e(TAG, "--> connectIMServer - onError - 连接融云 IM 服务失败，code = " + code);
//                UiUtils.hideWaitingDialog();
            }                                       

            @Override
            public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus databaseOpenStatus) {
                Log.e(TAG, "--> connectIMServer - onDatabaseOpened databaseOpenStatus = "+databaseOpenStatus);
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
                Log.e(TAG, "--> rtcConnectIMServer - onSuccess");

                roomId = etRoomId.getText().toString().trim();
                if (TextUtils.isEmpty(roomId)) {
                    Toast.makeText(MainActivity.this, "房间号不能为空！", Toast.LENGTH_SHORT).show();
                    return;
                }
                MeetingPrepareActivity.start(MainActivity.this, roomId, userId);
            }

            @Override
            public void onError(RongIMClient.ConnectionErrorCode code) {
                Log.e(TAG, "--> rtcConnectIMServer - onError - 连接融云 IM 服务失败，code = " + code);
            }

            @Override
            public void onDatabaseOpened(RongIMClient.DatabaseOpenStatus databaseOpenStatus) {
                Log.e(TAG, "--> rtcConnectIMServer - onDatabaseOpened databaseOpenStatus = "+databaseOpenStatus);// DATABASE_OPEN_SUCCESS
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnGetToken:
                Log.e(TAG,"btnGetToken 点击了");
                if (checkPermission()) {
                    getTokenFromAppServer();
                }
                break;
            case R.id.btnConnectIMServer://音视频通话
                Log.e(TAG,"btnConnectIMServer 点击了");
                connectIMServer(token);
                break;
            case R.id.btnRTCConnectIMServer://音视频会议
                Log.e(TAG,"btnRTCConnectIMServer 点击了");
                rtcConnectIMServer(token);
                break;
        }
    }
}
