package com.example.myapplication.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.util.ArrayList;

import io.rong.callkit.RongCallKit;
import io.rong.imlib.model.Conversation;

/**
 * 音视频通话
 */
public class CallKitActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = CallKitActivity.class.getName();
    private Button btnMultiPersonCall, btnSinglePersonCall;
    private RadioGroup rgUsers;
    private RadioButton rbUser001, rbUser002, rbUser003;
    public static final String USER_ID = "user_id";
    public static final String ROOM_ID = "room_id";
    private String roomId = "1001", userId = "001", targetId = "002";

    public static void start(Context context, String roomId, String userId) {
        Intent intent = new Intent(context, CallKitActivity.class);
        intent.putExtra(ROOM_ID, roomId);
        intent.putExtra(USER_ID, userId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_kit);

        btnSinglePersonCall = findViewById(R.id.btnSinglePersonCall);
        btnMultiPersonCall = findViewById(R.id.btnMultiPersonCall);
        rgUsers = findViewById(R.id.rgUsers);
        btnSinglePersonCall.setOnClickListener(this);
        btnMultiPersonCall.setOnClickListener(this);

        Intent intent = getIntent();
        roomId = intent.getStringExtra(ROOM_ID);
        userId = intent.getStringExtra(USER_ID);
        Log.e(TAG, "--> onCreate - roomId = "+roomId);
        Log.e(TAG, "--> onCreate - userId = "+userId);

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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSinglePersonCall:
                //发起单人通话
                RongCallKit.startSingleCall(CallKitActivity.this, targetId, RongCallKit.CallMediaType.CALL_MEDIA_TYPE_VIDEO);
                break;
            case R.id.btnMultiPersonCall:
                //发起多人通话，要现在融云控制台创建群组并加入用户
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
