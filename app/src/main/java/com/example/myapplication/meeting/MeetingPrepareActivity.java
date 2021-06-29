package com.example.myapplication.meeting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class MeetingPrepareActivity extends AppCompatActivity  implements View.OnClickListener {

    private static final String TAG = MeetingPrepareActivity.class.getName();
    private Button btnJoinRoom;
    public static final String USER_ID = "user_id";
    public static final String ROOM_ID = "room_id";
    private String roomId = "1001", userId = "001";

    public static void start(Context context, String roomId, String userId) {
        Intent intent = new Intent(context, MeetingPrepareActivity.class);
        intent.putExtra(ROOM_ID, roomId);
        intent.putExtra(USER_ID, userId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_prepare);
        btnJoinRoom = findViewById(R.id.btnJoinRoom);
        btnJoinRoom.setOnClickListener(this);

        Intent intent = getIntent();
        roomId = intent.getStringExtra(ROOM_ID);
        userId = intent.getStringExtra(USER_ID);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnJoinRoom:
                MeetingActivity.start(MeetingPrepareActivity.this, roomId, false);
                break;
        }
    }
}
