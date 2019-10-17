package com.example.sns;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.TabHost;

public class NotificationReceiver extends BroadcastReceiver {
    private String TAG = "NotificationReceiver";


    interface OnPushNotificationListener {
        void onPushReceived(Intent intent);
    }

    OnPushNotificationListener mListener;

    public NotificationReceiver(OnPushNotificationListener mListener) {
        this.mListener = mListener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {//브로드캐스트를 보내면 콜백되어서 받는 메소드
        Log.d(TAG, "userAccount-" + intent.getStringExtra("userAccount"));
        Log.d(TAG, "profile-" + intent.getStringExtra("profile"));
        Log.d(TAG, "category-" + intent.getStringExtra("category"));
        Log.d(TAG, "click_action-" + intent.getStringExtra("click_action"));
        Log.d(TAG, "message" + intent.getStringExtra("message"));
        if (intent.getStringExtra("image") != null) {
            Log.d(TAG, "image-" + intent.getStringExtra("image"));
        }
        Log.d(TAG, "postNum-" + intent.getIntExtra("postNum", 0));

        MainActivity.tabHost.setCurrentTab(3);//알림 화면으로 액티비티를 전환해준다.
        mListener.onPushReceived(intent);
    }

}
