package com.example.xtrack.P2pDiscovery;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.xtrack.Adapters.peerListAdapter;
import com.example.xtrack.BroadCastReciever.WiFiDirectBroadcastReciever;
import com.example.xtrack.InitThreads.ClientInit;
import com.example.xtrack.InitThreads.ServerInit;
import com.example.xtrack.R;
import com.example.xtrack.Tools.ChangeDeviceName;
import com.example.xtrack.trackingphase;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.loop;
import static android.os.Looper.prepare;

public class P2pDiscovery extends HandlerThread {

    private static final String TAG = "P2pDiscovery";

    public static Handler handler;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReciever mReciever;
    IntentFilter mIntentFilter;

    Context context;

    ConnectivityManager mCManager;
    trackingphase tp;
    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    WifiP2pDevice[] deviceArray;
    String[] deviceNameArray;
    int[] image;
    String name;

    SharedPreferences sprefs;
    public String STATUS;


    public P2pDiscovery(trackingphase tp) {
        super("P2pDiscovery", Thread.MAX_PRIORITY);
        this.tp = tp;
        context = tp;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        System.out.println(getLooper());
        handler = new Handler(getLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case 1:
                        discoverPeer();
                        break;
                }
            }
        };
        Log.d(TAG,"Hanlder Instantiated");
    }


    @Override
    public void run() {
        if(getLooper()!=null) {
            System.out.println("Running P2pDiscovery");
            initialWork();
            context.registerReceiver(mReciever, mIntentFilter);
        }
    }


    @Override
    public void interrupt() {
        super.interrupt();
        context.unregisterReceiver(mReciever);
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
        mReciever = new WiFiDirectBroadcastReciever(P2pDiscovery.this,tp,mManager, mChannel, mCManager);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    public void discoverPeer(){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
        }
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Discover Started", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                String failReason = "Unknown";
                if (reason == 0) failReason = "Internal Error";
                if (reason == 1) failReason = "P2P Unsupported";
                if (reason == 2) failReason = "WiFi Direct Busy";
                Toast.makeText(context, "Discovery Failed", Toast.LENGTH_SHORT).show();
                Toast.makeText(context, "Error: " + failReason, Toast.LENGTH_SHORT).show();
            }
        });

    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peerList.getDeviceList().equals(peers)) {
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                image = new int[peerList.getDeviceList().size()];
                int index = 0;

                for (WifiP2pDevice device : peerList.getDeviceList()) {
                    deviceNameArray[index] = device.deviceName;
                    deviceArray[index] = device;
                    image[index] = R.drawable.ic_unknown_user;
                    index++;
                }
                peerListAdapter peerAdapter = new peerListAdapter(context,image, deviceNameArray);
                tp.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tp.peersListView.setAdapter(peerAdapter);
                    }
                });



                if (peers.size() == 0) {
                    Toast.makeText(context, "No Device Found", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }
    };

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(final WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;
            if (info.groupFormed && info.isGroupOwner) {
                tp.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tp.connectionStatus.setText("Host");
                        //tp.serverInit = new ServerInit( trackingphase.this, handlerSockets, threadHandler);
                        //tp.serverInit.setName("HostSocketsThread");
                        //serverInit.start();
                    }
                });

            } else if (info.groupFormed) {
                tp.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tp.connectionStatus.setText("Client");
                        //tp.clientInit = new ClientInit(groupOwnerAddress,trackingphase.this,handlerSockets, threadHandler);
                        //tp.clientInit.setName("ClientSocketThread");
                        //clientInit.start();
                    }
                });
            }
        }
    };

    public void changeDeviceName(){
        sprefs = context.getSharedPreferences(context.getString(R.string.AVATAR), trackingphase.MODE_PRIVATE);
        STATUS = sprefs.getString("USERTYPE", null);
        String devName = sprefs.getString("NAME", null) + " | " + STATUS;
        ChangeDeviceName changeDeviceName = new ChangeDeviceName(mManager, mChannel);
        changeDeviceName.setDeviceName(devName);
        if (STATUS == "Host") {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(context, "Group Created! Client can now Connect!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int reason) {

                }
            });
        }
    }

    public WifiP2pDevice[] getDeviceArray() {
        return deviceArray;
    }

    public static Handler getHandler() {
        return handler;
    }
}
