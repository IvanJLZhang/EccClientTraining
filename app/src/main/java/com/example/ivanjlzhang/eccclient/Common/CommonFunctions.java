package com.example.ivanjlzhang.eccclient.Common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by ivanjlzhang on 18-1-3.
 */

public abstract class CommonFunctions {
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

    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return versionName;
    }
}
