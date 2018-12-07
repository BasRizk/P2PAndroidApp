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
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatScreenFragment extends Fragment {

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private View mContentView;
    private ArrayList<Message> messageList = new ArrayList<>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.chat_screen, container);

        mMessageRecycler = mContentView.findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(messageList);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mMessageRecycler.setLayoutManager(layoutManager);
        mMessageRecycler.setAdapter(mMessageAdapter);
        messageList.add(new Message("hi boy", true));
        mMessageAdapter.notifyItemInserted(messageList.size() - 1);
        messageList.add(new Message("hi boy", true));
        mMessageAdapter.notifyItemInserted(messageList.size() - 1);
        messageList.add(new Message("hi boy", true));
        mMessageAdapter.notifyItemInserted(messageList.size() - 1);
        messageList.add(new Message("hi boy", true));
        mMessageAdapter.notifyItemInserted(messageList.size() - 1);
        messageList.add(new Message("hi boy", true));
        mMessageAdapter.notifyItemInserted(messageList.size() - 1);

        messageList.add(new Message("hi boyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy", false));
        mMessageAdapter.notifyItemInserted(messageList.size() - 1);
        messageList.add(new Message("hi boy", false));
        mMessageAdapter.notifyItemInserted(messageList.size() - 1);

        return mContentView;
    }

}