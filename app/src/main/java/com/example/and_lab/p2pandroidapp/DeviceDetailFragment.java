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
    private TCPConnection tcpServer = null;

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


        // TODO to be deleted at the end
        // startActivityForResult here is this method looking for the image using the intent
        // in case of our application //////
        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("image/*");
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                    }
                });

        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO 1: it may be used if we created the chatting operation as a totally separate activity
        // but then we would need somehow to keep the connection opened and use back at the same time
        // our old activity

        // TODO 2: remove this part otherwise
        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        Uri uri = data.getData();
        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
        statusText.setText("Sending: " + uri);
        Log.d(WifiDirectActivity.TAG, "Intent----------- " + uri);
        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                info.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
        getActivity().startService(serviceIntent);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        /* JUST UPDATING THE VIEW HERE*/
        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));
        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());
        String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();


        // TODO Follows the chatting connection mechanism instead
        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        //
        // After the group negotiation, we can determine the group owner
        // (server).
        if (info.groupFormed && info.isGroupOwner) {

            // Do whatever tasks are specific to the group owner.
            // One common case is creating a group owner thread and accepting
            // incoming connections.
            // TODO take care that here is the group owner work
            // TODO ChattingServerTask implementation (probably async task as well)
            new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
                    .execute();
            if(info.groupOwnerAddress.toString() == this.device.deviceAddress) {
                // Meaning
            }
        } else if (info.groupFormed) {
            // The other device acts as the peer (client). In this case,
            // you'll want to create a peer thread that connects
            // to the group owner.
            // TODO TcpClientConnection implementation where the client (not the group owner) does his work
            ((DeviceActionListener) getActivity()).initializeChatTCPConnection(new TCPClient(getActivity(), info.groupOwnerAddress));


            // The other device acts as the client. In this case, we enable the
            // get file button.
//            // TODO Erase this of course!
//            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
//            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
//                    .getString(R.string.client_text));
        }

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
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
