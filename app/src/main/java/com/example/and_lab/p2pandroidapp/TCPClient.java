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

    private InetAddress IP_address;
    private Socket clientSocket;
    private DataOutputStream dataOutputStream;
    private InputStreamReader inputStreamReader;
    private String recieved_message;
    private Context context;

    public TCPClient(Context context, InetAddress IP_address){
        this.IP_address = IP_address;
        this.context = context;
    }

    public void start(){
        try {
            clientSocket = new Socket(IP_address,0);
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
            final BufferedReader coming_message = new BufferedReader(inputStreamReader);
            new Thread(){
                public void run(){
                    try {
                        recieved_message = coming_message.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message){
        try {
            dataOutputStream.writeBytes(message + "\n");
            Log.d(WifiDirectActivity.TAG,"Message : " + message + " was sent successfully");
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

    public InetAddress getIP_address() {
        return IP_address;
    }

    public void setIP_address(InetAddress IP_address) {
        this.IP_address = IP_address;
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

    public String getRecieved_message() {
        return recieved_message;
    }

    public void setRecieved_message(String recieved_message) {
        this.recieved_message = recieved_message;
    }
}
