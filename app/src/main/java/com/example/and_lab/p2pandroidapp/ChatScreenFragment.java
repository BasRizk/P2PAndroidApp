package com.example.and_lab.p2pandroidapp;

import android.content.Context;
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

import java.util.ArrayList;
import java.util.List;

public class ChatScreenFragment extends Fragment {

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private View mContentView;
    private ArrayList<Message> messageList = new ArrayList<>();
    private String clientName;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button sendButton = (Button) mContentView.findViewById(R.id.button_chatbox_send);
        sendButton.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendClicked();
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

    public void backClicked() {

    }

    public void refreshView() {
        mContentView.setVisibility(View.VISIBLE);
        TextView chatTitle = mContentView.findViewById(R.id.chat_title);
        if(clientName != null)
            chatTitle.setText(clientName);
    }

}
