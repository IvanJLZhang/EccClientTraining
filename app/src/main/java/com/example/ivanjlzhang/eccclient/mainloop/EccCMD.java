package com.example.ivanjlzhang.eccclient.mainloop;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

import static com.example.ivanjlzhang.eccclient.mainloop.Antenna.ANTENNA_CONFIGURATION_REQUEST;
import static com.example.ivanjlzhang.eccclient.mainloop.Antenna.ANTENNA_CONFIGURATION_STATUS_REQUEST;
import static com.example.ivanjlzhang.eccclient.mainloop.Antenna.ANTENNA_INFORMATION_REQUEST;

/**
 * Created by ivanjlzhang on 18-1-15.
 */

public class EccCMD {
    private static final String TAG = "EccCMD";
    private static final String SET_RX_DIVERSITY_HOOK_STRING = "SET_RX_DIVERSITY";
    private static final String GET_TX_INFO_HOOK_STRING = "GET_TX_RX_INFO";
    private static final String RX_CHAIN0_ONLY_PARAM = "RX_CHAIN0_ONLY";
    private static final String RX_CHAIN1_ONLY_PARAM = "RX_CHAIN1_ONLY";
    private static final String RX_CHAIN_BOTH_PARAM = "RX_CHAIN_BOTH";

    private static final int CMD_CALL = 0;
    private static final int CMD_RESULT = 1;
    private static final int CMD_ERROR = 2;

    private Handler resultHandler = null;
    private HandlerThread handlerThread;
    private Handler callHandler;

    public boolean isInitialized() {
        return initialized;
    }

    private boolean initialized = false;

    Class<?> PhoneFactory = null;
    Object mPhone = null;
    Method invokeOemRilRequestStrings = null;

    public Antenna.Configuration localConf;
    public Antenna.Information localInfo;

    public void setResultHandler(Handler handler){
        this.resultHandler = handler;
    }

    public EccCMD(){
        initializeCmdExecMethod();
        Log.d(TAG, "initializeCmdExecMethod: " + initialized);
        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        callHandler = new Handler(handlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                byte[] data = (byte[]) msg.obj;
                switch (data[0]){
                    case ANTENNA_CONFIGURATION_REQUEST:
                        Antenna.Configuration configuration = Antenna.parseConfiguration(data);
                        if(configuration.dual){
                            execAntennaConfigurationRequestCmd(RX_CHAIN_BOTH_PARAM);
                        }else if(configuration.primary){
                            execAntennaConfigurationRequestCmd(RX_CHAIN0_ONLY_PARAM);
                        }else if(configuration.secondary){
                            execAntennaConfigurationRequestCmd(RX_CHAIN1_ONLY_PARAM);
                        }
                        break;
                    case ANTENNA_CONFIGURATION_STATUS_REQUEST:
                        Antenna.Configuration configuration1 = Antenna.parseConfiguration(data);
                        localConf = configuration1;
                        execAntennaConfigurationStatusRequest("");
                        break;
                    case ANTENNA_INFORMATION_REQUEST:
                        Antenna.Information information = Antenna.parseInformation(data);
                        localInfo = information;
                        execAntennaInformationRequest("");
                        break;
                }
                return false;
            }
        });
    }

    public void callCmd(byte[] data){
        callHandler.obtainMessage(CMD_CALL, data).sendToTarget();
    }

    private void execAntennaConfigurationRequestCmd(String cmd){
        String[] cmdArr = new String[]{
                SET_RX_DIVERSITY_HOOK_STRING,
                cmd
        };
        if(initialized){
            try {
                invokeOemRilRequestStrings.invoke(mPhone, cmdArr,
                        resultHandler.obtainMessage(ANTENNA_CONFIGURATION_REQUEST));
                Log.d(TAG, "execAntennaConfigurationRequestCmd");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void execAntennaConfigurationStatusRequest(String cmd){
        String[] cmdArr = new String[]{
                GET_TX_INFO_HOOK_STRING,
                cmd
        };
        if(initialized){
            try {
                invokeOemRilRequestStrings.invoke(mPhone, cmdArr,
                        resultHandler.obtainMessage(ANTENNA_CONFIGURATION_STATUS_REQUEST));
                Log.d(TAG, "execAntennaConfigurationStatusRequest");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

    }
    private void execAntennaInformationRequest(String cmd){
        String[] cmdArr = new String[]{
                GET_TX_INFO_HOOK_STRING,
                cmd
        };
        if(initialized){
            try {
                invokeOemRilRequestStrings.invoke(mPhone, cmdArr,
                        resultHandler.obtainMessage(ANTENNA_INFORMATION_REQUEST));
                Log.d(TAG, "execAntennaInformationRequest");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeCmdExecMethod(){
        try {
            PhoneFactory = Class.forName("com.android.internal.telephony.PhoneFactory");
            Method[] methods = PhoneFactory.getDeclaredMethods();
            Method getDefaultPhone = null;
            for (Method method : methods){
                if(method.getName().equals("getDefaultPhone")){
                    getDefaultPhone = method;
                    break;
                }
            }
            if(getDefaultPhone != null){
                getDefaultPhone.setAccessible(true);
                mPhone = getDefaultPhone.invoke(null, (Object[])null);
            }
            if(mPhone != null){
                for (Method method : mPhone.getClass().getMethods()){
                    if(method.getName().equals("invokeOemRilRequestStrings")){
                        invokeOemRilRequestStrings = method;
                        break;
                    }
                }
                invokeOemRilRequestStrings.setAccessible(true);
                initialized = true;
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
    }


    public class Result {
        public String cmd = "";
        public int rc = -1;
        public List<String> out;
        public Result() {
            out = new ArrayList<>();
        }
    }

    public class Error {
        public String cmd = "";
        public String msg = "";
        public Error(String cmd) {
            this.cmd = cmd;
        }
    }
}
