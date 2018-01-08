package com.example.ivanjlzhang.eccclient.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
/**
 * Created by ivanjlzhang on 18-1-4.
 */

public class NetBroadcastReceiver extends BroadcastReceiver {

    public iNetEvent event;
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)
//                ||
//                intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION) ||
//                intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)
                ){
            if(event != null){
                event.onNetChanged(intent.getAction());
            }
        }
    }

    public interface iNetEvent{
        void onNetChanged(String netState);
    }
}
