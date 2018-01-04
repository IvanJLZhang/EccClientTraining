package com.example.ivanjlzhang.eccclient.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by ivanjlzhang on 18-1-4.
 */

public abstract class NetworkUtil {
    private static final String TAG = "NetworkUtil";

    public static boolean isNetworkAvaiable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager == null){
            Log.e(TAG, "can not get ConnectivityManager");
        }else{
            NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();
            if(infos != null){
                for (NetworkInfo info:infos
                     ) {
                    if(info.isAvailable())
                        return true;
                }
            }
        }
        return false;
    }

    public static boolean checkNetState(Context context){
        boolean netstate = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null){
            NetworkInfo[] infos = connectivityManager.getAllNetworkInfo();
            if(infos != null){
                for (NetworkInfo info:infos
                     ) {
                    if(info.getState() == NetworkInfo.State.CONNECTED){
                        netstate = true;
                        break;
                    }
                }
            }
        }
        return netstate;
    }

    public static boolean isNetworkRoaming(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Log.w(TAG, "couldn't get connectivity manager");
        } else {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null
                    && info.getType() == ConnectivityManager.TYPE_MOBILE) {
                TelephonyManager tm = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                if (tm != null && tm.isNetworkRoaming()) {
                    return true;
                } else {
                }
            } else {
            }
        }
        return false;
    }

    public static boolean isMobileDataEnable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isMobileDataEnable = false;
        try {
            isMobileDataEnable = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
        return isMobileDataEnable;
    }

    public static boolean isWifiDataEnable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiDataEnable = false;
        try{
            isWifiDataEnable = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
        return isWifiDataEnable;
    }

    public static String getAssignedIPAddress() {
        String localip = "";
        try {
            Enumeration networkInterface = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (networkInterface.hasMoreElements()){
                NetworkInterface networkInterface1 = (NetworkInterface) networkInterface.nextElement();
                Enumeration<InetAddress> ias = networkInterface1.getInetAddresses();
                while (ias.hasMoreElements()){
                    ia = ias.nextElement();
                    if(ia instanceof Inet6Address){
                        continue;
                    }
                    String ip = ia.getHostAddress();
                    if(!"127.0.0.1".equals(ip)){
                        localip = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }
        return localip;
    }
}
