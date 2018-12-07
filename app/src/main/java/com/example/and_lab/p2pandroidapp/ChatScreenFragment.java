package com.example.and_lab.p2pandroidapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;

public class ChatScreenFragment extends Fragment implements ConnectionInfoListener {

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private View mContentView;
    private ArrayList<Message> messageList = new ArrayList<>();
    private String clientName;
    private TCPServer tcpServer;
    private TCPClient tcpClient;
    WifiP2pInfo info;
    ProgressDialog progressDialog;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button sendButton = (Button) mContentView.findViewById(R.id.button_chatbox_send);
        Button backButton = (Button) mContentView.findViewById(R.id.back_button);
        sendButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendClicked();
            }
        });
        backButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                closeFragment();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.chat_screen, container);

        mMessageRecycler = mContentView.findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(messageList);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.setStackFromEnd(true);
        mMessageRecycler.setLayoutManager(layoutManager);
        mMessageRecycler.setAdapter(mMessageAdapter);

        return mContentView;
    }

    public void sendClicked() {

        TextView editTextChatbox = (TextView) (mContentView.findViewById(R.id.edittext_chatbox));

        if(editTextChatbox.getText() != null && editTextChatbox.getText().toString() != null
                && !editTextChatbox.getText().toString().equals("")) {
            String messageText = editTextChatbox.getText().toString();
            tcpClient.sendMessage(messageText);
            createMessage(messageText, true);
            editTextChatbox.setText("");
        }
    }

    public void createMessage(String text, Boolean isSender) {
        messageList.add(new Message(text, isSender));
        mMessageAdapter.notifyItemInserted(messageList.size() - 1);
        mMessageRecycler.scrollToPosition(messageList.size() - 1);
    }

    public void resetView() {
        messageList.clear();
        clientName = "";
        TextView editTextChatbox = (TextView) (mContentView.findViewById(R.id.edittext_chatbox));
        editTextChatbox.setText("");
    }

    public void closeConnections() {
        tcpClient.tearDown();
        tcpServer.tearDown();
    }

    public void closeFragment() {
        closeConnections();
        resetView();
        ((DeviceActionListener) getActivity()).disconnect();
        mContentView.setVisibility(View.GONE);
    }

    public void refreshView() {
        mContentView.setVisibility(View.VISIBLE);
        TextView chatTitle = mContentView.findViewById(R.id.chat_title);
        if(clientName != null)
            chatTitle.setText(clientName);
    }

    public void setClientName(String name) {
        this.clientName = name;
        refreshView();
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        this.info = info;

        // Probably this btn does not matter however to keep the code safe
        if (info.groupFormed && info.isGroupOwner) {
            // Initiate Server Socket, then wait for socket to accept
            // get socket accepted IP/ADDRESS and create a client socket on its server
            TCPServer tcpServer = new TCPServer(getActivity());
            tcpServer.start();
            InetAddress clientInetAddress = tcpServer.getClientSocket().getInetAddress();
            TCPClient tcpClient = new TCPClient(getActivity(), clientInetAddress);
            tcpClient.start();

        } else if (info.groupFormed) {
            // Initiate client socket, then server socket and
            // wait for client coming to server before beginning chat
            TCPClient tcpClient = new TCPClient(getActivity(), info.groupOwnerAddress);
            tcpClient.start();
            TCPServer tcpServer = new TCPServer(getActivity());
            tcpServer.start();

        }

        this.getView().setVisibility(View.VISIBLE);

    }
}
