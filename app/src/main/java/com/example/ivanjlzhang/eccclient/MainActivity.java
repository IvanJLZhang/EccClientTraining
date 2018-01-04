package com.example.ivanjlzhang.eccclient;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
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

    private boolean isFistRun = true;
    public static NetBroadcastReceiver.iNetEvent netEvent;

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
                    updateNetworkSignal((boolean)msg.obj);
                    break;
                case NetworkCheckService.MSG_CHECK_NET_STATE:
                    logMsg(msg.obj.toString());
                    break;
            }
        }
    };
    NetworkCheckService networkCheckService;
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
        getActivityView();

        netEvent = this;

        fragmentb = new ConnectionConfigBFragment();
        showContentFragment(fragmentb);
        connectedFragment = new ConnectedFragment();

        eccClient = new EccClient();
        eccClient.setHandler(this.eccClientMsgHandler);

        networkCheckService = new NetworkCheckService();
        networkCheckService.setHandler(netStateCheckHandler);
        networkCheckService.startToCheckNetState(this);
    }

    protected void getActivityView(){
        iv_signal_state = findViewById(R.id.signal_view);
        tv_log_msg = findViewById(R.id.tv_log_msg);
        tv_log_msg.setMovementMethod(ScrollingMovementMethod.getInstance());
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
        try {
            TextView tv_local_ip = fragmentb.getView().findViewById(R.id.local_device_ip_addr);
            if(isNetworkConnected){
                iv_signal_state.setImageDrawable(getDrawable( R.drawable.signal_full));
                String local_ip = NetworkUtil.getAssignedIPAddress();
                tv_local_ip.setText(local_ip);

            }else{
                iv_signal_state.setImageDrawable(getDrawable(R.drawable.signal_empty));
                tv_local_ip.setText("127.0.0.1");
                CountDownTimer countDownTimer = new CountDownTimer(const_inc.APP_EXIT_WAIT_TIME, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
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
    }

    private void cleanMsg(){
        tv_log_msg.setText("");
        tv_log_msg.scrollTo(0, 0);
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
            eccClient.stop();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    public void onNetChanged(boolean netState) {
        if(networkCheckService != null){
            logMsg("network has been changed.");
            networkCheckService.startToCheckNetState(this);
        }
    }
}
