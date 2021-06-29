package com.example.myapplication.meeting;

import com.example.myapplication.base.BasePresenter;
import com.example.myapplication.base.BaseViewImp;

import java.util.List;

import cn.rongcloud.rtc.api.RCRTCRemoteUser;
import cn.rongcloud.rtc.api.RCRTCRoom;
import cn.rongcloud.rtc.api.stream.RCRTCInputStream;
import cn.rongcloud.rtc.base.RTCErrorCode;

public interface MeetingContract {
    interface View extends BaseViewImp {
        void onJoinRoomSuccess(RCRTCRoom rcrtcRoom);

        void onJoinRoomFailed(RTCErrorCode rtcErrorCode);

        void onPublishSuccess();

        void onPublishFailed();

        void onSubscribeSuccess(List<RCRTCInputStream> inputStreamList);

        void onSubscribeFailed();

        void onUserJoined(RCRTCRemoteUser rcrtcRemoteUser);

        void onUserLeft(RCRTCRemoteUser rcrtcRemoteUser);
    }

    abstract class Presenter extends BasePresenter<View> {

        public abstract void joinRoom(String roomId);

        public abstract void publishDefaultAVStream();

        public abstract void subscribeAVStream();

        public abstract void leaveRoom();

    }
}