package com.example.and_lab.p2pandroidapp;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

/**
 * An interface-callback for the activity to listen to fragment interaction
 * events.
 */
public interface DeviceActionListener {
    void showDetails(WifiP2pDevice device);
    void cancelDisconnect();
    void connect(WifiP2pConfig config);
    void disconnect();
    void createMessageFromServer(String text, Boolean isSender);
}
