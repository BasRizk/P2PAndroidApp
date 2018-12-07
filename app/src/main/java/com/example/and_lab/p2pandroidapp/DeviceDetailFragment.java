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
    ProgressDialog progressDialog = null;
    private TCPClient tcpClient = null;
    private TCPServer tcpServer = null;
    private boolean connectBtnClicked = false;

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

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((DeviceActionListener) getActivity()).disconnect();
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
        /*
        // JUST UPDATING THE VIEW HERE
        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);

        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        */

        if (connectBtnClicked) {
            // Probably this btn does not matter however to keep the code safe
            if (info.groupFormed && info.isGroupOwner) {
                // Initiate Server Socket, then wait for socket to accept
                // get socket accepted IP/ADDRESS and create a client socket on its server
                TCPServer tcpServer = new TCPServer(getActivity());
                tcpServer.start();
                InetAddress clientInetAddress = tcpServer.getClientSocket().getInetAddress();
                TCPClient tcpClient = new TCPClient(getActivity(), clientInetAddress);
                tcpClient.start();
                ((DeviceActionListener) getActivity()).transferChatConnection(tcpServer, tcpClient);

            } else if (info.groupFormed) {
                // Initiate client socket, then server socket and
                // wait for client coming to server before beginning chat
                TCPClient tcpClient = new TCPClient(getActivity(), info.groupOwnerAddress);
                tcpClient.start();
                TCPServer tcpServer = new TCPServer(getActivity());
                tcpServer.start();
                ((DeviceActionListener) getActivity()).transferChatConnection(tcpServer, tcpClient);

            }

            // hide the connect button
            //mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
            this.getView().setVisibility(View.GONE);
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
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

}
