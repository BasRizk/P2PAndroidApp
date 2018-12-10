package com.example.and_lab.p2pandroidapp;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.InetSocketAddress;

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
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(context, "ChatConnectionAsyncTask began working", Toast.LENGTH_LONG).show();
        Log.d(WifiDirectActivity.TAG, "ChatConnectionAsyncTask is working.");
    }

    @Override
    protected Void doInBackground(Void... voids) {

        // TODO remove toasts and logs from here or wrap them in runOnUIThread()
        // TODO dig into TCPServer and tcpClient Logs as well.

        if(isGroupOwner) {
            // Initiate Server Socket, then wait for socket to accept
            // get socket accepted IP/ADDRESS and create a client socket on its server
            tcpServer = new TCPServer(context);
            tcpServer.start();
            if(tcpServer.isConnected()) {
                InetAddress clientInetAddress =
                        ((InetSocketAddress) tcpServer.getClientSocket().getRemoteSocketAddress()).getAddress();
                tcpClient = new TCPClient(context, clientInetAddress);
                tcpClient.start();
                if(tcpClient.isConnected()) {
                    isConnected = true;
                }
            }
        } else {
            // Initiate client socket, then server socket and
            // wait for client coming to server before beginning chat
            tcpClient = new TCPClient(context, groupOwnerAddress);
            tcpClient.start();
            if(tcpClient.isConnected()) {
                tcpServer = new TCPServer(context);
                tcpServer.start();
                if(tcpServer.isConnected()) {
                    isConnected = true;
                }
            }
        }


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        Toast.makeText(context, "chatConnectionAsyncTask finished working.", Toast.LENGTH_LONG).show();
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

    public static void debugAndToastOnUIThread(final Context context, final String strToToast) {
        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(WifiDirectActivity.TAG, strToToast);
                Toast.makeText(context, strToToast, Toast.LENGTH_SHORT);
            }
        });
    }
}

