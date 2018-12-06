package com.example.and_lab.p2pandroidapp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class WorkerRunnable implements  Runnable {
    private Socket clientSocket = null;

    public  WorkerRunnable(Socket clientSocket){
        this.clientSocket = clientSocket;
    }
    public void  run(){
        try {
            InputStream input = clientSocket.getInputStream();
            OutputStream output = clientSocket.getOutputStream();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
