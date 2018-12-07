package com.example.and_lab.p2pandroidapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.view.View;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {


    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private WifiDirectActivity activity;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, WifiDirectActivity context) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = context;

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
                Log.d(WifiDirectActivity.TAG, "WifiDirect is enabled.");
                activity.setIsWifiP2pEnabled(true);
            } else {
                // Wi-Fi P2P is not enabled
                Log.d(WifiDirectActivity.TAG, "WifiDirect is disabled.");
                activity.setIsWifiP2pEnabled(false);
                activity.resetData();
            }
            Log.d(WifiDirectActivity.TAG, "P2P state changed - " + state);

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on DeviceListFragment.onPeersAvailable(list)

            if(manager != null) {
                DeviceListFragment deviceListFragment = (DeviceListFragment) activity.getSupportFragmentManager()
                        .findFragmentById(R.id.frag_list);
                manager.requestPeers(channel, (WifiP2pManager.PeerListListener) deviceListFragment);
            }
            Log.d(WifiDirectActivity.TAG, "P2P peers changed");

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Connection state changed! We should probably do something about
            // that.

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                Log.d(WifiDirectActivity.TAG, "WifiDirectBroadcastReceiver :: Devices are connected.");
                // we are connected with the other device, request connection
                // info to find group owner IP

                DeviceDetailFragment deviceDetailFragment = (DeviceDetailFragment) activity
                        .getSupportFragmentManager().findFragmentById(R.id.frag_detail);
                deviceDetailFragment.getView().setVisibility(View.GONE);
                manager.requestConnectionInfo(channel, deviceDetailFragment);

//                // calling FRAGMENT.onConnectionInfoAvailable(WifiP2PInfo info)
//                ChatScreenFragment chatScreenFragment = (ChatScreenFragment) activity
//                        .getSupportFragmentManager().findFragmentById(R.id.chat_screen_frag);
//                manager.requestConnectionInfo(channel, chatScreenFragment);


            } else {
                Log.d(WifiDirectActivity.TAG, "WifiDirectBroadcastReceiver :: Devices are disconnected.");
                // It's a disconnect
                // TODO what to do once devices disconnected (maybe) acknowledge user via text, and lock chat
                // TODO it is the UI and the Logic changes once wifi disconnection happens
                activity.resetData();
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {

            // TODO once device details have changed what to do! get name from here just like the list here does
            DeviceListFragment deviceListFragment = (DeviceListFragment) activity.getSupportFragmentManager()
                    .findFragmentById(R.id.frag_list);
            deviceListFragment.updateThisDeviceView((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
//            ChatScreenFragment chatScreenFragment = (ChatScreenFragment) activity.getSupportFragmentManager()
//                    .findFragmentById(R.id.chat_screen_frag);
//            chatScreenFragment.setClientName(((WifiP2pDevice) intent.getParcelableExtra(
//                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)).deviceName);

        }
    }

}
