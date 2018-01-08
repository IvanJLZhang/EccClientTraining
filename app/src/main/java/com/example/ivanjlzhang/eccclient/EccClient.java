package com.example.ivanjlzhang.eccclient;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOError;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by ivanjlzhang on 18-1-2.
 */

public class EccClient {
    private static final String TAG = "EccClient";

    private Handler handler = null;
    public static final int MSG_SERVER_STATUS = 0;
    public static final int MSG_DATA_RECEIVE = 1;

    public static final int MSG_SHOW_MESSAGE = 2;
    public static final int MSG_CLEAR = 3;
    public static final int MSG_LISTEN_STATUS = 4;
    private static final int BUFF_SIZE = 8;

    private boolean isServerValid = false;
    private int localPort = -1;
    private String serverIp = "";
    private int serverPort = 0;

    private boolean isPause = false;
    private DatagramSocket mUDPSocket;

    private ReadThread receiveThread = new ReadThread();
    public EccClient setHandler(Handler handler){
        this.handler = handler;
        return this;
    }

    public EccClient start(int localPort){
        if(isServerValid){
//            if(localPort == this.localPort){
//                logMsg("Duplicated localPort: " + this.localPort);
//                return this;
//            }
            stop();
        }
        this.localPort = localPort;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mUDPSocket = new DatagramSocket(EccClient.this.localPort);
                    receiveThread = new ReadThread();
                    receiveThread.start();
                    isServerValid = true;
                    onListenStatusUpdate(isServerValid);
                    logMsg("start listening and opening receive data channel: " + EccClient.this.localPort);

                }catch (IOException ex){
                    logMsg("Error: " + ex);
                }
            }
        }).start();
        return this;
    }

    public void pause(){
        if(isServerValid && !isPause){
            logMsg("pause the ecc client service.");
            isPause = true;
            stop();
        }
    }

    public void resume(){
        if(localPort > 0 && isPause){
            logMsg("resume the ecc client service.");
            start(this.localPort);
            isPause = false;
        }
    }


    public void stop(){
        logMsg("stop listening and reveiving data.");
        receiveThread.interrupt();
        close();
        isServerValid = false;
        onListenStatusUpdate(isServerValid);
        onServerStatusUpdate(isServerValid);
    }

    public void sendData(final byte[] data){
        logMsg("response data to " + serverIp + ":" + serverPort);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress address = InetAddress.getByName(serverIp);
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, serverPort);
                    mUDPSocket.send(packet);
                    logMsg("response data: " + toHexString(data));
                } catch (UnknownHostException e) {
                    logMsg("Send error:" + e);
                    stop();
                } catch (IOException e) {
                    logMsg("Send error:" + e);
                    stop();
                }
            }
        }).start();
    }

    private void close(){
        if(mUDPSocket != null){
            mUDPSocket.close();
            mUDPSocket = null;
        }
    }

    private void onServerStatusUpdate(boolean connected){
        if(handler != null){
            handler.obtainMessage(MSG_SERVER_STATUS, connected).sendToTarget();
        }
    }
    private void onListenStatusUpdate(boolean connected){
        if(handler != null){
            handler.obtainMessage(MSG_LISTEN_STATUS, connected).sendToTarget();
        }
    }
    private void onDataReceive(final byte[] data) {
        if (handler != null) {
            handler.obtainMessage(MSG_DATA_RECEIVE, data).sendToTarget();
        }
    }
    private void logMsg(final String msg){
        Log.d(TAG, msg);
        if(handler != null){
            handler.obtainMessage(MSG_SHOW_MESSAGE, msg).sendToTarget();
        }
    }
    private void clearMsg(){
        Log.d(TAG, "Clear message");
        if (handler != null) {
            handler.obtainMessage(MSG_CLEAR).sendToTarget();
        }
    }
    class ReadThread extends Thread{
        @Override
        public void run() {
            super.run();
            while (isServerValid){
                byte[] buff = new byte[BUFF_SIZE];
                DatagramPacket packet = new DatagramPacket(buff, buff.length);
                try {
                    mUDPSocket.receive(packet);
                    onServerStatusUpdate(true);
                    serverIp = packet.getAddress().getHostAddress();
                    serverPort = packet.getPort();
                    clearMsg();
                    logMsg("receive data from: "+serverIp+":"+serverPort);
                    logMsg("\t" + toHexString(packet.getData()));
                    onDataReceive(packet.getData());
                } catch (IOException e) {
                    logMsg("receive error:" + e);
                    EccClient.this.stop();
                }
            }
        }
    }

    @NonNull
    private static String toHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        try{
        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i] & 0xFF).toUpperCase();
            hex = hex.length()>1 ? hex : ("0"+hex);
            sb.append(hex + " ");
        }}catch (Exception ex){
            ex.printStackTrace();
        }
        return sb.toString();
    }
}
