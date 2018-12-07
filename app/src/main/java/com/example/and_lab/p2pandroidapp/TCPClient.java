package com.example.and_lab.p2pandroidapp;

import android.content.Context;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPClient {

    private static final int SOCKET_TIMEOUT = 5000;

    private InetAddress IpAddress;
    private Socket clientSocket;
    private DataOutputStream dataOutputStream;
    private InputStreamReader inputStreamReader;
    private String receivedMessage;
    private Context context;
    private int portNum;
    private Thread acceptingMessagesThread;
    private int connectionRetries;

    public TCPClient(Context context, InetAddress IpAddress){
        this.IpAddress = IpAddress;
        this.context = context;
        this.portNum = WifiDirectActivity.PORT_NUM;
        this.connectionRetries = 5;
    }

    protected void start(){
        try {
            IpAddress.getHostAddress();

            clientSocket = new Socket();
            clientSocket.bind(null);
            clientSocket.connect((new InetSocketAddress(IpAddress.getHostAddress(), portNum)), SOCKET_TIMEOUT);

            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
            Log.d(WifiDirectActivity.TAG,
                    "Initiated ClientSocket to IP/ " + IpAddress + ", PortNum/ " + portNum);
            final BufferedReader comingMessage = new BufferedReader(inputStreamReader);
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
                    "IP/" + IpAddress + ",PortNum/" + portNum);

        } catch (IOException e) {
            e.printStackTrace();

            try {

                if(connectionRetries > 0) {
                    Toast.makeText(context,"Retrying to connect in 1 second.",Toast.LENGTH_SHORT).show();
                    Thread.sleep(1000);
                    connectionRetries--;
                    this.start();
                }
                else
                    Toast.makeText(context, "Failed to connect,please try again later.", Toast.LENGTH_LONG).show();

            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    protected void sendMessage(String message){
        try {
            dataOutputStream.writeBytes(message + "\n");
            Log.d(WifiDirectActivity.TAG,"Message: " + message + " was sent successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void tearDown() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            Log.e(WifiDirectActivity.TAG,"Error Closing the TCP Server");
            e.printStackTrace();
        }
    }

    public InetAddress getIpAddress() {
        return IpAddress;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.IpAddress = ipAddress;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public void setDataOutputStream(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    public InputStreamReader getInputStreamReader() {
        return inputStreamReader;
    }

    public void setInputStreamReader(InputStreamReader inputStreamReader) {
        this.inputStreamReader = inputStreamReader;
    }

    public String getReceivedMessage() {
        return receivedMessage;
    }

    public void setReceivedMessage(String receivedMessage) {
        this.receivedMessage = receivedMessage;
    }

    public boolean isConnected() {
        return clientSocket.isConnected();
    }
}
