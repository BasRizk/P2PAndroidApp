package com.example.and_lab.p2pandroidapp;

import android.net.nsd.NsdManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    NsdHelper mNsdHelper;
    NsdManager mNsdManager;
    TCPConnection mConnection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConnection = new TCPConnection(this);
        mNsdHelper = new NsdHelper(this);
        mNsdHelper.initializeNsd();
        mNsdManager = mNsdHelper.getNsdManager();


    }


    @Override
    protected void onPause() {
        if (mNsdHelper != null) {
            mNsdHelper.tearDown();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNsdHelper != null) {
            mNsdHelper.registerService(mConnection.getLocalPort());
            mNsdHelper.discoverServices();
        }
    }

    @Override
    protected void onDestroy() {
        mNsdHelper.tearDown();
        mConnection.tearDown();
        super.onDestroy();
    }


}
