package com.example.ivanjlzhang.eccclient.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.example.ivanjlzhang.eccclient.MainActivity;

/**
 * Created by ivanjlzhang on 18-1-4.
 */

public class NetBroadcastReceiver extends BroadcastReceiver {

    public iNetEvent event = (iNetEvent) MainActivity.netEvent;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
            boolean netState = NetworkUtil.checkNetState(context);
            if(event != null){
                event.onNetChanged(netState);
            }
        }
    }

    public interface iNetEvent{
        void onNetChanged(boolean netState);
    }
}
