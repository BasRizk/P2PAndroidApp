package com.example.and_lab.p2pandroidapp;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    private static final int SOCKET_TIMEOUT = 5000;

    private InetAddress mInetAddress;
    private int mPortNum;
    private Socket mClientSocket;
    private DataOutputStream mDataOutputStream;
    private InputStreamReader mInputStreamReader;
    private String receivedMessage;
    private Thread acceptingMessagesThread;
    private int connectionRetries;

    private Context context;


    public TCPClient(Context context, InetAddress mInetAddress){
        this.mInetAddress = mInetAddress;
        this.context = context;
        this.mPortNum = WifiDirectActivity.PORT_NUM;
        this.connectionRetries = 5;
    }

    protected void start(){
        try {
            //mClientSocket = new Socket();
            //mClientSocket.bind(null);
            //mClientSocket.connect((new InetSocketAddress(mInetAddress.getHostAddress(), mPortNum)), SOCKET_TIMEOUT);

            mClientSocket = new Socket(mInetAddress, mPortNum);

            mDataOutputStream = new DataOutputStream(mClientSocket.getOutputStream());
            mInputStreamReader = new InputStreamReader(mClientSocket.getInputStream());
            Log.d(WifiDirectActivity.TAG,
                    "Initiated ClientSocket to IP/ " + mInetAddress + ", PortNum/ " + mPortNum);
            final BufferedReader comingMessage = new BufferedReader(mInputStreamReader);
            acceptingMessagesThread = new Thread(){
                public void run(){
                    try {
                        receivedMessage = comingMessage.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            acceptingMessagesThread.start();

            Log.d(WifiDirectActivity.TAG,
                    "Starting thread accepting messages by ClientSocket with " +
                    "IP/" + mInetAddress + ",PortNum/" + mPortNum);

        } catch (IOException e) {
            Log.e(WifiDirectActivity.TAG, e.getMessage());

            try {

                if(connectionRetries > 0) {
                    Log.d(WifiDirectActivity.TAG,
                            "Retrying to connect in 1 second.");
                    Thread.sleep(1000);
                    connectionRetries--;
                    this.start();
                }
                else {
                    Log.d(WifiDirectActivity.TAG,
                            "Failed to connect,please try again later.");
                }
            } catch (InterruptedException e1) {
                Log.e(WifiDirectActivity.TAG, e1.getMessage());
            }
        }
    }

    protected void sendMessage(String message){
        try {
            mDataOutputStream.writeBytes(message + "\n");
            Log.d(WifiDirectActivity.TAG,"Message: " + message + " was sent successfully");
        } catch (IOException e) {
            Log.e(WifiDirectActivity.TAG, e.getMessage());
        }
    }

    protected void tearDown() {
        try {
            mClientSocket.close();
        } catch (IOException e) {
            Log.e(WifiDirectActivity.TAG, e.getMessage());
            Log.e(WifiDirectActivity.TAG,"Error Closing the TCP Server");
        }
    }

    public InetAddress getmInetAddress() {
        return mInetAddress;
    }

    public void setmInetAddress(InetAddress mInetAddress) {
        this.mInetAddress = mInetAddress;
    }

    public Socket getmClientSocket() {
        return mClientSocket;
    }

    public void setmClientSocket(Socket mClientSocket) {
        this.mClientSocket = mClientSocket;
    }

    public DataOutputStream getmDataOutputStream() {
        return mDataOutputStream;
    }

    public void setmDataOutputStream(DataOutputStream mDataOutputStream) {
        this.mDataOutputStream = mDataOutputStream;
    }

    public InputStreamReader getmInputStreamReader() {
        return mInputStreamReader;
    }

    public void setmInputStreamReader(InputStreamReader mInputStreamReader) {
        this.mInputStreamReader = mInputStreamReader;
    }

    public String getReceivedMessage() {
        return receivedMessage;
    }

    public void setReceivedMessage(String receivedMessage) {
        this.receivedMessage = receivedMessage;
    }

    public boolean isConnected() {
        return mClientSocket.isConnected();
    }
}
