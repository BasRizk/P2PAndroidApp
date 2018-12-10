package com.example.and_lab.p2pandroidapp;


import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import android.support.v7.app.AppCompatActivity;

/**
 * An activity that uses WiFi Direct APIs to discover and connect with available
 * devices. WiFi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * WiFi state related events.
 */
public class WifiDirectActivity extends AppCompatActivity implements ChannelListener, DeviceActionListener {
    public final static String TAG = "WifiDirect";
    public final static int PORT_NUM = 8988;

    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter mIntentFilter = new IntentFilter();
    private WifiP2pManager mManager;
    private Channel mChannel;
    private BroadcastReceiver mReceiver = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate :: BEGIN");
        setContentView(R.layout.activity_wifi_direct);

        // Indicates a change in the Wi-Fi P2P status.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Indicates a change in the list of available peers.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Indicates the state of Wi-Fi P2P connectivity has changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Indicates this device's details have changed.
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        //mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        Log.d(TAG, "onCreate :: DONE");
    }

    /** register the WifiDirectBroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {

        Log.d(WifiDirectActivity.TAG, "DeviceList and DeviceDetail to be reset.");
        DeviceListFragment deviceListFragment = (DeviceListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frag_list);

        DeviceDetailFragment deviceDetailFragment = (DeviceDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frag_detail);

        if (deviceListFragment != null) {
            deviceListFragment.clearPeers();
        }
        if (deviceDetailFragment != null) {
            deviceDetailFragment.resetViews();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // bearing in mind that the discovery will be using a pop up fragment like dialog (maybe)
        switch (item.getItemId()) {
            case R.id.atn_direct_enable:
                if (mManager != null && mChannel != null) {
                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.
                    startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.e(TAG, "channel or manager is null");
                }
                return true;
            case R.id.atn_direct_discover:
                if (!isWifiP2pEnabled) {
                    Toast.makeText(WifiDirectActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return true;
                }

                final DeviceListFragment deviceListFragment = (DeviceListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.frag_list);
                deviceListFragment.onInitiateDiscovery();
                mManager.discoverPeers(mChannel, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(WifiDirectActivity.this, "Discovery Initiated",
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WifiDirectActivity.this, "Discovery Failed : " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment deviceDetailFragment = (DeviceDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frag_detail);
        deviceDetailFragment.showDetails(device);
    }

    @Override
    public void connect(WifiP2pConfig config) {
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(WifiDirectActivity.TAG, "Connection succeeded.");
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }
            @Override
            public void onFailure(int reason) {
                Log.d(WifiDirectActivity.TAG, "Connection failed. reason: " + reason);

                Toast.makeText(WifiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        /*
        mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                String groupPassword = group.getPassphrase();
            }
        });
        */
    }

    @Override
    public void disconnect() {
        // if chat view is on
        final DeviceDetailFragment deviceDetailFragment = (DeviceDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frag_detail);
        deviceDetailFragment.resetViews();

        final ChatScreenFragment chatScreenFragment = (ChatScreenFragment) getSupportFragmentManager()
                .findFragmentById(R.id.chat_screen_frag);
        chatScreenFragment.closeFragment();

        Toast.makeText(this, "Connection lost", Toast.LENGTH_LONG).show();

        mManager.removeGroup(mChannel, new ActionListener() {
            @Override
            public void onFailure(int reasonCode) {

                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }
            @Override
            public void onSuccess() {
                Log.d(WifiDirectActivity.TAG, "Disconnection succeeded.");
                if(deviceDetailFragment.getView() != null) {
                    deviceDetailFragment.getView().setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (mManager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            mManager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost permanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void cancelDisconnect() {
        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (mManager != null) {
            final DeviceListFragment deviceListFragment = (DeviceListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.frag_list);
            if (deviceListFragment.getDevice() == null
                    || deviceListFragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (deviceListFragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || deviceListFragment.getDevice().status == WifiP2pDevice.INVITED) {
                mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(WifiDirectActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WifiDirectActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public void createMessageFromServer(String text, Boolean isSender) {
        Log.d(WifiDirectActivity.TAG,"CreateMessageFromServer entered");
        final ChatScreenFragment chatScreenFragment = (ChatScreenFragment) getSupportFragmentManager()
                .findFragmentById(R.id.chat_screen_frag);
        chatScreenFragment.createMessage(text, false);
    }

    @Override
    public void acknowledgeConnectionCreation(ChatConnectionAsyncTask chatConnectionAsyncTask) {
        DeviceListFragment deviceListFragment = (DeviceListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frag_list);
        deviceListFragment.acknowledgeConnectionCreation(chatConnectionAsyncTask);

        DeviceDetailFragment deviceDetailFragment = (DeviceDetailFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frag_detail);
        deviceDetailFragment.acknowledgeConnectionCreation(chatConnectionAsyncTask);

        ChatScreenFragment chatScreenFragment = (ChatScreenFragment) getSupportFragmentManager()
                .findFragmentById(R.id.chat_screen_frag);
        chatScreenFragment.acknowledgeConnectionCreation(chatConnectionAsyncTask);

        if(!chatConnectionAsyncTask.isConnected()) {
            Toast.makeText(this, "Connection failed!", Toast.LENGTH_SHORT);
            Log.d(WifiDirectActivity.TAG, "WifiDirectActivity :: ACK connection failed.");
            disconnect();
        }
    }

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }
}
