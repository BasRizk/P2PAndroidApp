package com.example.and_lab.p2pandroidapp;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;

import static java.lang.Thread.sleep;

public class TCPServer {

    private Context context;
    private ServerSocket mServerSocket;
    private Socket clientSocket;
    private int mLocalPort;
    private boolean keepRunning;

    public TCPServer(Context context) {
        this.context = context;
        this.keepRunning = true;
        this.mLocalPort = WifiDirectActivity.PORT_NUM;
    }

    public void tearDown() {
        keepRunning = false;
        try {
            mServerSocket.close();
        } catch (IOException e) {
            Log.e(WifiDirectActivity.TAG, e.getMessage());
            Log.d(WifiDirectActivity.TAG, "problem closing server socket while tearing down");
        }
    }


    public void start() {
        try {
            // Initialize a server socket on the next available port.
            mServerSocket = new ServerSocket(this.mLocalPort);
            // Store the chosen port.
            mLocalPort = mServerSocket.getLocalPort();
            Log.d(WifiDirectActivity.TAG, "Initiated ServerSocket on port " + mLocalPort);

            clientSocket = mServerSocket.accept();
            new Thread(new WorkerRunnable(clientSocket, context)).start();
            Log.d(WifiDirectActivity.TAG,
                    "Connected with client with IP/ " + clientSocket.getInetAddress().toString());

        } catch (IOException e) {
            Log.e(WifiDirectActivity.TAG, e.getMessage());
            Log.d(WifiDirectActivity.TAG,"Server failed to initiate connection on port " + mLocalPort);
        }

    }

    protected Socket getClientSocket() {
        return clientSocket;
    }

    public boolean isConnected() {
        return !mServerSocket.isClosed();
    }

    private class WorkerRunnable implements  Runnable {

        private Context context;
        private Socket clientSocket = null;
        private String receivedMessage;

        public  WorkerRunnable(Socket clientSocket, Context context){
            this.context = context;
            this.clientSocket = clientSocket;
        }

        public void  run(){
            try {

                BufferedReader bufferedReader = null;

                while(keepRunning){
                    // TODO maybe check if ready and sleep for a bit to save effort
                    bufferedReader = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                    receivedMessage = bufferedReader.readLine();
                    if(receivedMessage != null) {
                        Log.d(WifiDirectActivity.TAG,
                                "TCPServer :: WorkerRunnable :: received message : " + receivedMessage);
                        actOnUi(context, receivedMessage);
                        //((DeviceActionListener) context).createMessageFromServer(receivedMessage, false);
                    } else {
                        try {
                            sleep(500);
                        } catch (InterruptedException e) {
                            Log.e(WifiDirectActivity.TAG, e.getMessage());
                        }
                    }

                }
                if(bufferedReader != null) {
                    bufferedReader.close();
                }
            }
            catch (IOException e){
                Log.e(WifiDirectActivity.TAG, e.getMessage());
                Log.d(WifiDirectActivity.TAG, "problem in server run thread");
            }
        }
    }

    public static void actOnUi(final Context context, final String receivedMessage) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(WifiDirectActivity.TAG, "Message Received: "  + receivedMessage);
                ((DeviceActionListener) context).createMessageFromServer(receivedMessage, false);

            }
        });
    }
}
