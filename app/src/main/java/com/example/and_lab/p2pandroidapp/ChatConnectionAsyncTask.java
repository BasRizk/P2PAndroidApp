package com.example.and_lab.p2pandroidapp;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import java.net.InetAddress;

public class ChatConnectionAsyncTask extends AsyncTask<Void, Void, Void> {

    private Context context;
    private InetAddress groupOwnerAddress;
    private boolean isGroupOwner;
    public TCPServer tcpServer;
    public TCPClient tcpClient;

    private boolean isConnected = false;

    public ChatConnectionAsyncTask(Context context, InetAddress groupOwnerAddress, boolean isGroupOwner) {
        this.context = context;
        this.groupOwnerAddress = groupOwnerAddress;
        this.isGroupOwner = isGroupOwner;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Toast.makeText(context, "ChatConnectionAsyncTask began working", Toast.LENGTH_LONG).show();

        Log.d(WifiDirectActivity.TAG, "ChatConnectionAsyncTask is working.");
        if(isGroupOwner) {
            Log.d(WifiDirectActivity.TAG, "Initiating Chatting Connections as (a groupOwner)");
            // Initiate Server Socket, then wait for socket to accept
            // get socket accepted IP/ADDRESS and create a client socket on its server
            tcpServer = new TCPServer(context);
            tcpServer.start();
            if(tcpServer.isConnected()) {
                InetAddress clientInetAddress = tcpServer.getClientSocket().getInetAddress();
                TCPClient tcpClient = new TCPClient(context, clientInetAddress);
                tcpClient.start();
                if(tcpClient.isConnected()) {
                    isConnected = true;
                }
            }
        } else {
            Log.d(WifiDirectActivity.TAG, "Initiating Chatting Connections as (NOT a groupOwner)");
            // Initiate client socket, then server socket and
            // wait for client coming to server before beginning chat
            tcpClient = new TCPClient(context, groupOwnerAddress);
            tcpClient.start();
            if(tcpClient.isConnected()) {
                TCPServer tcpServer = new TCPServer(context);
                tcpServer.start();
                if(tcpServer.isConnected()) {
                    isConnected = true;
                }
            }
        }

        Toast.makeText(context, "chatConnectionAsyncTask finished working.", Toast.LENGTH_LONG).show();



        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d(WifiDirectActivity.TAG, "Finished initiating ChattingConnectionAsyncTask," +
                " and Connection = " + isConnected);
        ((DeviceActionListener)context).acknowledgeConnectionCreation(this);
    }

    public boolean isConnected() {
        return isConnected;
    }

    public TCPServer getTCPServer() {
        return tcpServer;
    }

    public TCPClient getTCPClient() {
        return tcpClient;
    }
}

