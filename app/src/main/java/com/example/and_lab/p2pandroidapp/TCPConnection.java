package com.example.and_lab.p2pandroidapp;

import android.content.Context;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

// TODO not used class currently
public class TCPConnection {

    private static final String TAG = "TCP";
    private Context context;
    private ServerSocket mServerSocket;
    private Socket clientSocket;
    private int mLocalPort;

    public TCPConnection(Context context) throws IOException {
        this.context = context;
        initializeServerSocket();
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
            new Thread(new WorkerRunnable(clientSocket, context)).start();
        }
    }

    protected int getLocalPort() {
        return mLocalPort;
    }

    protected Socket getClientSocket() {
        return clientSocket;
    }

    private class WorkerRunnable implements  Runnable {

        private Context context;
        private Socket clientSocket = null;
        private String recieved_message;

        public  WorkerRunnable(Socket clientSocket, Context context){
            this.context = context;
            this.clientSocket = clientSocket;
        }

        public void  run(){
            try {
                InputStreamReader input = new InputStreamReader(clientSocket.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(input);
                while(true){
                    recieved_message = bufferedReader.readLine();
                    ((DeviceActionListener) context).createMessageFromServer(recieved_message, false);
                }
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}
