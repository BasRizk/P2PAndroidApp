package com.example.and_lab.p2pandroidapp;

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

    private InputStreamReader inputStreamReader;
    private String recieved_message;

    public TCPClient(InetAddress IP_address){
        this.IP_address = IP_address;
    }

    public void start(){
        try {
            clientSocket = new Socket(IP_address,0);
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader coming_message = new BufferedReader(inputStreamReader);
            recieved_message = coming_message.readLine();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendMessage(String message){
        try {
            dataOutputStream.writeBytes(message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
