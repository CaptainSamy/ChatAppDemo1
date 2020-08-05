package com.example.chatappdemo.internet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.chatappdemo.activity.ChatActivity;
import com.example.chatappdemo.activity.GroupChatActivity;
import com.example.chatappdemo.activity.MainActivity;

public class InternetConnector_Receiver extends BroadcastReceiver {
    public InternetConnector_Receiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {

            boolean isVisible = MyApplication.isActivityVisible();// Check if

            Log.i("Activity is Visible ", "Is activity visible : " + isVisible);

            // If it is visible then trigger the task else do nothing
            if (isVisible == true) {
                ConnectivityManager connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager
                        .getActiveNetworkInfo();

                // Check internet connection and accrding to state change the
                // text of activity by calling method
                if (networkInfo != null && networkInfo.isConnected()) {
                    new MainActivity().changeTextStatus(true);
                    new ChatActivity().changeTextStatus(true);
                    new GroupChatActivity().changeTextStatus(true);
                } else {
                    new MainActivity().changeTextStatus(false);
                    new ChatActivity().changeTextStatus(false);
                    new GroupChatActivity().changeTextStatus(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
