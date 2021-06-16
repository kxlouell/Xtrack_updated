package com.example.xtrack.P2pDiscovery;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.xtrack.BroadCastReciever.WiFiDirectBroadcastReciever;

import static android.os.Looper.getMainLooper;

public class P2pDiscovery extends HandlerThread {

    private static final String TAG = "P2pDiscovery";

    Handler handler;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReciever mReciever;
    Context context;
    ConnectivityManager mCManager;
    ConnectivityManager.NetworkCallback mCallback;;

    public P2pDiscovery(String name, int priority) {
        super("P2pDiscovery", priority);

    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        handler = new Handler(this.getLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
            }
        };
    }

    @Override
    public void run() {

    }

    private void initialWork(){
        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, getLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "Channel Disconnected");
            }
        });
        mCManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mReciever = new WiFiDirectBroadcastReciever(mManager, mChannel, context, mCManager, mCallback);
    }
}
