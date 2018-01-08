package com.example.ivanjlzhang.eccclient;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.ivanjlzhang.eccclient.Common.CommonFunctions;
import com.example.ivanjlzhang.eccclient.network.NetworkUtil;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionConfigBFragment extends Fragment implements View.OnClickListener {

    Button button;
    TextView localIpAddress;
    TextView port;
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_go:
                String port = this.port.getText().toString();
                listener.GoButtonClicked(port);
            break;
            default:
                break;
        }
    }

    public interface iMessageTransition{
        public void GoButtonClicked(String msg);
    }
    iMessageTransition listener;

    public ConnectionConfigBFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connection_config_b, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        button = getActivity().findViewById(R.id.btn_go);
        button.setOnClickListener(this);
        localIpAddress = getActivity().findViewById(R.id.local_device_ip_addr);
        port = getActivity().findViewById(R.id.et_port);
        if(port.getText().toString().isEmpty()){
            port.setText("1024");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        listener = (iMessageTransition)activity;
        super.onAttach(activity);
    }
}
