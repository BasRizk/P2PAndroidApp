package com.example.and_lab.p2pandroidapp;

import android.app.ProgressDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;


/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */

public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

    protected static final int CHOOSE_FILE_RESULT_CODE = 20;

    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    private ChatConnectionAsyncTask chatConnectionAsyncTask;
    private boolean connectBtnClicked = false;

    ProgressDialog progressDialog = null;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_detail, container);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                String deviceAddress = device.deviceAddress;
                if(deviceAddress == null || deviceAddress.split("")[0] == " ") {
                    deviceAddress = "N/A";
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + deviceAddress, true, true,
                        new DialogInterface.OnCancelListener() {

                            @Override
                            public void onCancel(DialogInterface dialog) {
                                ((DeviceActionListener) getActivity()).cancelDisconnect();
                            }
                        }
                );
                connectBtnClicked = true;
                ((DeviceActionListener) getActivity()).connect(config);
            }
        });

        return mContentView;
    }


    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        this.info = info;

        this.getView().setVisibility(View.VISIBLE);

        // JUST UPDATING THE VIEW HERE
        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);

        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());


        if(connectBtnClicked) {
            if (info.groupFormed) {
                chatConnectionAsyncTask = new ChatConnectionAsyncTask(getActivity(), info.groupOwnerAddress, info.isGroupOwner);
                chatConnectionAsyncTask.execute();
            }
            connectBtnClicked = false;
        }


   }

    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        Log.d(WifiDirectActivity.TAG,
                "Device with name \"" + device.deviceName + "\" to be displayed.");
        TextView view = mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());
    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        Log.d(WifiDirectActivity.TAG,
                "Device Detail View to be reset.");
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        this.getView().setVisibility(View.GONE);
    }

    public void acknowledgeConnectionCreation(ChatConnectionAsyncTask chatConnectionAsyncTask) {
        if(chatConnectionAsyncTask.isConnected()) {
            Log.d(WifiDirectActivity.TAG,
                    "Connection initiated successfully, resetting detailFragment.");
            resetViews();
            //this.getView().setVisibility(View.GONE);
        } else {
            // TODO ack user to repeat in a while
        }
    }
}
