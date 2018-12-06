package com.example.and_lab.p2pandroidapp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import java.util.List;

public class ChatScreenFragment extends Fragment {

    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private View mContentView;
    private List<Message> messageList;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.chat_screen, container);

        mMessageRecycler = getActivity().findViewById(R.id.reyclerview_message_list);
        mMessageAdapter = new MessageListAdapter(getActivity(), messageList);
//        mMessageRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        return mContentView;
    }

//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_message_list);
//
//        mMessageRecycler = (RecyclerView) findViewById(R.id.reyclerview_message_list);
//        mMessageAdapter = new MessageListAdapter(this, messageList);
//        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
//    }
}