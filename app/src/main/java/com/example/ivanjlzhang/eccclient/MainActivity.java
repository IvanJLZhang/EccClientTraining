package com.example.ivanjlzhang.eccclient;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.ivanjlzhang.eccclient.Common.CommonFunctions;
import com.example.ivanjlzhang.eccclient.network.NetBroadcastReceiver;
import com.example.ivanjlzhang.eccclient.network.NetworkCheckService;
import com.example.ivanjlzhang.eccclient.network.NetworkUtil;

public class MainActivity extends AppCompatActivity implements ConnectionConfigBFragment.iMessageTransition
                                                    , NetBroadcastReceiver.iNetEvent
{
    private static final String TAG = "ECC_MAIN";
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    ImageView iv_signal_state;

    ConnectionConfigBFragment fragmentb;
    ConnectedFragment connectedFragment;

    EccClient eccClient;

    TextView tv_log_msg;
    ScrollView sv_scroll;
    private boolean isFistRun = true;
    Handler eccClientMsgHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handleEccClientMessage(msg);
        }
    };

    Handler netStateCheckHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case NetworkCheckService.MSG_NET_STATE:
                    boolean isNetworkConnected = (boolean)msg.obj;
                    if(isNetworkConnected && eccClient != null){
                        eccClient.resume();
                    }
                    updateNetworkSignal(isNetworkConnected);
                    break;
                case NetworkCheckService.MSG_CHECK_NET_STATE:
                    logMsg(msg.obj.toString());
                    break;
                case NetworkCheckService.MSG_IS_CHECKING:
                    setAppReadyState(!(boolean)msg.obj);
                    break;
            }
        }
    };
    NetworkCheckService networkCheckService;

    NetBroadcastReceiver netBroadcastReceiver = new NetBroadcastReceiver();
    IntentFilter intentFilter;

    private void handleEccClientMessage(Message msg){
        switch (msg.what){
            case EccClient.MSG_SERVER_STATUS:
                boolean isServerOn = (boolean)msg.obj;
                updateServerStatus(isServerOn);
                break;
            case EccClient.MSG_LISTEN_STATUS:
                boolean isListening = (boolean)msg.obj;
                updateListenStatus(isListening);
                break;
            case EccClient.MSG_SHOW_MESSAGE:
                logMsg(msg.obj.toString());
                break;
            case EccClient.MSG_CLEAR:
                cleanMsg();
                break;
            case EccClient.MSG_DATA_RECEIVE:
                byte[] data = (byte[])msg.obj;
                handleRequest(data);
                break;
        }
    }

    /**
     * 处理Server发来的命令请求
     * @param data
     */
    private void handleRequest(byte[] data){
        if(data != null){
            eccClient.sendData(data);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(getSupportActionBar() != null){
            // 隐藏标题栏
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
                initialization();
//            }
//        }).start();
    }

    private void initialization(){
        getActivityView();
        logMsg("initialize for the app...");
//        netBroadcastReceiver = new NetBroadcastReceiver();
        netBroadcastReceiver.event = this;
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");

        fragmentb = new ConnectionConfigBFragment();
        showContentFragment(fragmentb);
        connectedFragment = new ConnectedFragment();

        eccClient = new EccClient();
        eccClient.setHandler(this.eccClientMsgHandler);

        networkCheckService = new NetworkCheckService();
        networkCheckService.setHandler(netStateCheckHandler);

//        setAppReadyState(false);
//        networkCheckService.startToCheckNetState(this);
    }

    @Override
    public void onNetChanged(String netState) {
        if(networkCheckService != null){
            cleanMsg();
            logMsg("network has been changed.");
            if(eccClient != null)
                eccClient.pause();
            networkCheckService.startToCheckNetState(this);
        }
    }
    protected void getActivityView(){
        iv_signal_state = findViewById(R.id.signal_view);
        tv_log_msg = findViewById(R.id.tv_log_msg);
        tv_log_msg.setMovementMethod(ScrollingMovementMethod.getInstance());
        sv_scroll = findViewById(R.id.sv_scroll);
        TextView tv_title = findViewById(R.id.tv_title);
        String title = getString(R.string.app_name) + " v" + CommonFunctions.getAppVersionName(this);
        tv_title.setText(title);
    }

    private void showContentFragment(Fragment fragment, boolean animator){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        if(animator){
            transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, R.animator.slide_in_left, R.animator.slide_out_right);
            transaction.addToBackStack("connected");
        }
        transaction.replace(R.id.fg_content, fragment);
        transaction.commit();
    }

    private void showContentFragment(Fragment fragment){
        this.showContentFragment(fragment, false);
    }
    boolean lastServerStatus = false;
    /**
     * 更新信号显示状态
     * @param isServerOn
     */
    protected void updateServerStatus(boolean isServerOn){
        if(isServerOn != lastServerStatus){
            try {
                if(isServerOn){
                    showContentFragment(connectedFragment, true);
                }else{
                    showContentFragment(fragmentb);
                }
            }catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        lastServerStatus = isServerOn;
    }

    private void updateNetworkSignal(boolean isNetworkConnected){
        setAppReadyState(isNetworkConnected);
        try {
            TextView tv_local_ip = fragmentb.getView().findViewById(R.id.local_device_ip_addr);
            if(isNetworkConnected){
                iv_signal_state.setImageDrawable(getDrawable( R.drawable.signal_full));
                String old_ip = tv_local_ip.getText().toString();
                String local_ip = NetworkUtil.getAssignedIPAddress(this);
                if(old_ip != local_ip){
                    tv_local_ip.setText(local_ip);
                }
            }else{
                iv_signal_state.setImageDrawable(getDrawable(R.drawable.signal_empty));
                tv_local_ip.setText("127.0.0.1");
                CountDownTimer countDownTimer = new CountDownTimer(const_inc.APP_EXIT_WAIT_TIME, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if(networkCheckService.isHasInternetAccess()){
                            this.cancel();
                            return;
                        }
                        cleanMsg();
                        logMsg("this app will be terminated in " + millisUntilFinished / 1000 + "s.");
                    }

                    @Override
                    public void onFinish() {
                        logMsg("exit.");
                        System.exit(0);
                    }
                };
                countDownTimer.start();
            }
        }catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    private void updateListenStatus(boolean isListening){
        try {
            Button button = fragmentb.getView().findViewById(R.id.btn_go);
            TextView textView = fragmentb.getView().findViewById(R.id.et_port);
            button.setEnabled(!isListening);
            textView.setEnabled(!isListening);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void setAppReadyState(boolean isAppReady){
        try {
            Button button = fragmentb.getView().findViewById(R.id.btn_go);
            TextView textView = fragmentb.getView().findViewById(R.id.et_port);
            button.setEnabled(isAppReady);
            textView.setEnabled(isAppReady);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    /**
     * Go 按钮响应事件，开启侦听
     * @param msg
     */
    @Override
    public void GoButtonClicked(String msg) {
        int port = Integer.parseInt(msg);
        eccClient.start(port);
    }

    private void logMsg(String msg){
        Log.d(TAG, msg);
        String logLine = msg + "\n";
        tv_log_msg.append(logLine);

        {
            int offset = tv_log_msg.getMeasuredHeight() -
                    sv_scroll.getMeasuredHeight();
            if(offset < 0){
                offset = 0;
            }
            sv_scroll.scrollTo(0, offset);
        }
    }

    private void cleanMsg(){
        tv_log_msg.setText("");

        {
            int offset = tv_log_msg.getMeasuredHeight() -
                    sv_scroll.getMeasuredHeight();
            if(offset < 0){
                offset = 0;
            }
            sv_scroll.scrollTo(0, offset);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(eccClient != null){
            eccClient.stop();
            eccClient = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(eccClient != null){
            eccClient.pause();
        }
        this.unregisterReceiver(this.netBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(this.netBroadcastReceiver, intentFilter);
        if(eccClient != null && !isFistRun) {
            eccClient.resume();
        }
        isFistRun = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(eccClient != null){
            eccClient.stop();
        }
    }
}
