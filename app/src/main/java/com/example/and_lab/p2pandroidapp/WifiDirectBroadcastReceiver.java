package com.example.and_lab.p2pandroidapp;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;


public class WifiDirectBroadcastReceiver extends android.content.BroadcastReceiver {


    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectActivity activity;

    DeviceListFragment deviceListFragment;
    DeviceDetailFragment connectionListener;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiDirectActivity context) {
        this.mManager = manager;
        this.mChannel = channel;
        this.activity = context;
        //this.deviceListFragment = new DeviceListFragment();
        //this.connectionListener = new DeviceDetailFragment();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not,
            // TODO alert the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi P2P is enabled
                activity.setIsWifiP2pEnabled(true);
            } else {
                // Wi-Fi P2P is not enabled
                activity.setIsWifiP2pEnabled(false);
                activity.resetData();
            }
            Log.d(WifiDirectActivity.TAG, "P2P state changed - " + state);

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // The peer list has changed! We should probably do something about
            // that.

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on DeviceListFragment.onPeersAvailable()

            if(mManager != null) {
                mManager.requestPeers(mChannel, deviceListFragment);
                // or ?
                //mManager.requestPeers(mChannel, (DeviceListFragment) activity.getFragmentManager()
                  //      .findFragmentById(R.id.device_list_fragment));
            }
            Log.d(WifiDirectActivity.TAG, "P2P peers changed");

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Connection state changed! We should probably do something about
            // that.

            if (mManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // We are connected with the other device, request connection
                // info to find group owner IP

                DeviceDetailFragment fragment = (DeviceDetailFragment) activity
                        .getFragmentManager().findFragmentById(R.id.frag_detail);
                mManager.requestConnectionInfo(mChannel, connectionListener);
            } else {
                activity.resetData();
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }
    }

    //@Override
    public void connect(int peerNumber) {

        WifiP2pDevice device = this.deviceListFragment.getPeers().get(peerNumber);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        //config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver notifies us. Ignore for now.
                // TODO What happens once connected successfully
            }

            @Override
            public void onFailure(int reason) {
                // TODO What happens if the connection fails
                Toast.makeText(activity, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                String groupPassword = group.getPassphrase();
            }
        });

    }

    public void createGroup() {
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Device is ready to accept incoming connections from peers.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(activity, "P2P group creation failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                // TODO if needed to retrieve group info including peers on the network
            }
        });
    }
}
