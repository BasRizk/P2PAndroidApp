package com.example.and_lab.p2pandroidapp;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;

// TODO not used class currently
public class TCPConnection implements Runnable {

    private static final String TAG = "TCP";
    private Context context;
    private ServerSocket mServerSocket;
    private int mLocalPort;

    public TCPConnection(Context context) {
        this.context = context;
        initializeServerSocket();
    }

    @Override
    public void run() {

    }

    private void initializeServerSocket() {
        try {
            // Initialize a server socket on the next available port.
            mServerSocket = new ServerSocket(0);
            // Store the chosen port.
            mLocalPort = mServerSocket.getLocalPort();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void tearDown() {
        try {
            mServerSocket.close();
        } catch (IOException e) {
            Log.e(TAG,"Error Closing the TCP Server");
            e.printStackTrace();
        }
    }

    protected int getLocalPort() {
        return mLocalPort;
    }
}
