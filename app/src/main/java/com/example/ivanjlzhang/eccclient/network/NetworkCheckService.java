package com.example.ivanjlzhang.eccclient.network;

import android.content.Context;
import android.os.Handler;

import com.example.ivanjlzhang.eccclient.const_inc;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ivanjlzhang on 18-1-4.
 */

public class NetworkCheckService {
    public static final int MSG_NET_STATE = 0;
    public static final int MSG_CHECK_NET_STATE = 1;
    public static final int MSG_IS_CHECKING = 2;
    Handler handler;
    Context mContext;
    int count = 0;
    private final int MAX_RETRY_COUNT = 4;
    Timer timer;
    boolean isStarted = false;

    public boolean isHasInternetAccess() {
        return hasInternetAccess;
    }

    private boolean hasInternetAccess = false;
    public void setHandler(Handler handler){
        this.handler = handler;
    }
    public void startToCheckNetState(Context context){
        if(!isStarted) {
            isStarted = true;
            mContext = context;
            timer = new Timer();
            count = 0;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    RunTaskContent();
                }
            }, 1000, const_inc.TIMER_PERIOD);
        }
    }
    private void RunTaskContent(){
        updateCheckingProgress(true);
        hasInternetAccess = NetworkUtil.checkNetState(mContext);
        count++;
        if(hasInternetAccess){
            timer.cancel();
            timer = null;
            isStarted = false;
            count = 0;
            updateCheckingProgress(false);
            ReportNetState(hasInternetAccess);
            ReportNetStateMsg("Device is registered to network.");
        }else {
            ReportNetStateMsg("Checking Network State: " + count);
            if (count >= MAX_RETRY_COUNT) {
                count = 0;
                updateCheckingProgress(false);
                ReportNetState(hasInternetAccess);
                ReportNetStateMsg("Device is not attached to the network …");
                timer.cancel();
                timer = null;
                isStarted = false;
            }
        }
    }
    private void ReportNetState(boolean isNetConnected){
        if(handler != null){
            handler.obtainMessage(MSG_NET_STATE, isNetConnected).sendToTarget();
        }
    }

    private void ReportNetStateMsg(String msg){
        if(handler != null){
            handler.obtainMessage(MSG_CHECK_NET_STATE, msg).sendToTarget();
        }
    }

    private void updateCheckingProgress(boolean isChecking){
        if(handler != null){
            handler.obtainMessage(MSG_IS_CHECKING, isChecking).sendToTarget();
        }
    }
}
