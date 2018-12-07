package com.example.and_lab.p2pandroidapp;

import android.content.Context;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// TODO not used class currently
public class TCPConnection implements Runnable {

    private static final String TAG = "TCP";
    private Context context;
    private ServerSocket mServerSocket;
    private Socket clientSocket;
    private int mLocalPort;

    public TCPConnection(Context context) throws IOException {
        this.context = context;
        initializeServerSocket();
    }

    @Override
    public void run() {

    }

    private void initializeServerSocket() throws IOException {
        try {
            // Initialize a server socket on the next available port.
            mServerSocket = new ServerSocket(0);
            // Store the chosen port.
            mLocalPort = mServerSocket.getLocalPort();

        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true){
            clientSocket = mServerSocket.accept();
            new Thread(new WorkerRunnable(clientSocket)).start();
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
