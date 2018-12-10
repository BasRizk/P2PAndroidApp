package com.example.and_lab.p2pandroidapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
/**
 * A ListFragment that displays available peers on discovery and requests the
 * parent activity to handle user interaction events
 */

public class DeviceListFragment extends ListFragment implements PeerListListener {

    private ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();

    ProgressDialog progressDialog = null;
    View mContentView = null;
    private WifiP2pDevice device;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.setListAdapter(new WiFiPeerListAdapter(getActivity(), R.layout.row_devices, (ArrayList<WifiP2pDevice>) peers));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.device_list, container, false);
        return mContentView;
    }

    public WifiP2pDevice getDevice() {
        return device;
    }
    private static String getDeviceStatus(int deviceStatus) {
        Log.d(WifiDirectActivity.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    /**
     * Initiate a connection with the peer.
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // it may be needed to be directed accordingly to chat page (fragment)
        // following a connection directly
        WifiP2pDevice device = (WifiP2pDevice) getListAdapter().getItem(position);
        ((DeviceActionListener) getActivity()).showDetails(device);
    }

    public void acknowledgeConnectionCreation(ChatConnectionAsyncTask chatConnectionAsyncTask) {
        if(chatConnectionAsyncTask.isConnected()) {
            Log.d(WifiDirectActivity.TAG,
                    "Connection initiated successfully, resetting detailFragment.");
            this.getView().setVisibility(View.GONE);
        } else {
            // TODO ack user to repeat in a while
            Log.d(WifiDirectActivity.TAG,
                    "Connection initiated successfully, resetting detailFragment.");
            Toast.makeText(getActivity(),
                    "Connection failed, please try again in a while.", Toast.LENGTH_SHORT);
        }
    }

    /**
     * Array adapter for ListFragment that maintains WifiP2pDevice list.
     */
    private class WiFiPeerListAdapter extends ArrayAdapter<WifiP2pDevice> {
        private ArrayList<WifiP2pDevice> items;
        /**
         * @param context
         * @param textViewResourceId
         * @param objects
         */
        public WiFiPeerListAdapter(Context context, int textViewResourceId,
                                   ArrayList<WifiP2pDevice> objects) {
            super(context, textViewResourceId, objects);
            items = objects;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.row_devices, null);
            }
            WifiP2pDevice device = items.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(R.id.device_name);
                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    Log.d(WifiDirectActivity.TAG, "WifiPeerListAdapter :: " + position +
                            " deviceName = '"+ device.deviceName +"'");
                    top.setText(device.deviceName);
                }
                if (bottom != null) {
                    Log.d(WifiDirectActivity.TAG, "WifiPeerListAdapter :: " + position +
                            " deviceStatus'"+ device.status +"'");
                    bottom.setText(getDeviceStatus(device.status));
                }
            }
            return v;
        }
    }

    /**
     * Update UI for this device.
     *
     * @param device WifiP2pDevice object
     */
    public void updateThisDeviceView(WifiP2pDevice device) {
        this.device = device;
        TextView view;
        view = mContentView.findViewById(R.id.my_name);
        view.setText(device.deviceName);
        view = mContentView.findViewById(R.id.my_status);
        view.setText(getDeviceStatus(device.status));
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {

        ArrayList<WifiP2pDevice> refreshedPeers =
                new ArrayList<WifiP2pDevice>();

        for (WifiP2pDevice peer : peerList.getDeviceList()) {
            refreshedPeers.add(peer);
        }

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        peers.clear();

        for (WifiP2pDevice peer : refreshedPeers) {
            peers.add(peer);
            ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        }

        // If an AdapterView is backed by this data, notify it
        // of the change. For instance, if you have a ListView of
        // available peers, trigger an update.
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        Log.d(WifiDirectActivity.TAG, "WifiPeerListAdapter notified of refreshing peers.");
        Log.d(WifiDirectActivity.TAG, "Found " + peers.size() + " devices.");
        Toast.makeText(getActivity(),"Found " + peers.size() + " devices.",Toast.LENGTH_SHORT).show();

        if (peers.size() == 0) {
            Log.d(WifiDirectActivity.TAG, "No devices found");
            return;
        }
    }

    public void clearPeers() {
        Log.d(WifiDirectActivity.TAG, "Peers to be cleared.");
        peers.clear();
        ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
    }

    public void onInitiateDiscovery() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel", "finding peers", true,
                true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // TODO might actually be needed to automatically initiate discovery once app is loaded
                    }
                });
    }

}
