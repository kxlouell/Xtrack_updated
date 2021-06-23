package com.example.xtrack.P2pDiscovery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
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
import static com.example.xtrack.trackingphase.START_CLIENT;
import static com.example.xtrack.trackingphase.START_SERVER;
import static com.example.xtrack.trackingphase.clientInit;
import static com.example.xtrack.trackingphase.serverInit;

public class P2pDiscovery extends HandlerThread implements Runnable {

    private static final String TAG = "P2pDiscovery";
    public static final int DISCOVER = 0;
    public static final int INITWORK = 1;
    public static final int SETDEVICENAME = 2;
    public static final int CONNECT = 3;


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

    public  InetAddress groupOwnerAddress;

    public P2pDiscovery(trackingphase tp) {
        super("P2pDiscovery", Thread.MAX_PRIORITY);
        this.tp = tp;
        context = tp;
    }


    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        System.out.println("Looper is :" + getLooper());
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                System.out.println("Message what is: "+msg.what);
                switch (msg.what) {
                    case DISCOVER:
                        discoverPeer();
                        break;
                    case INITWORK:
                        initialWork();
                        break;
                    case SETDEVICENAME:
                        changeDeviceName();
                        break;
                }
            }
        };
        Log.d(TAG, "Handler Instantiated");
        super.onLooperPrepared();
    }


    @Override
    public void interrupt() {
        context.unregisterReceiver(mReciever);
        disconnect();
        Log.d(TAG, " Interrupted");
        super.interrupt();
        quit();



    }


    public void initialWork() {
        Log.d(TAG, "Running initialWork"+Thread.currentThread().getName());
        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(context, getLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.d(TAG, "Channel Disconnected");
            }
        });
        mCManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mReciever = new WiFiDirectBroadcastReciever(P2pDiscovery.this, tp, mManager, mChannel, mCManager);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        context.registerReceiver(mReciever, mIntentFilter);


    }

    public void discoverPeer() {
        Log.d(TAG, "Running discoverPeer"+Thread.currentThread().getName());
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
                peerListAdapter peerAdapter = new peerListAdapter(context, image, deviceNameArray);
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
            groupOwnerAddress = info.groupOwnerAddress;
            if (info.groupFormed && info.isGroupOwner) {
                tp.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tp.connectionStatus.setText("Host");
                    }
                });
                tp.handlerSockets.obtainMessage(START_SERVER).sendToTarget();
                mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, "Local Services Cleared!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        switch (reason) {
                            case WifiP2pManager.P2P_UNSUPPORTED:
                                Toast.makeText(context, "Local Services Clear Failed: P2p Unsuported", Toast.LENGTH_SHORT).show();
                                break;
                            case WifiP2pManager.BUSY:
                                Toast.makeText(context, "Local Services Clear Failed: BUSY", Toast.LENGTH_SHORT).show();
                                break;
                            case WifiP2pManager.ERROR:
                                Toast.makeText(context, "Local Services Clear Failed: Enternal Error", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                discoverPeer();
            } else if (info.groupFormed) {
                tp.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tp.connectionStatus.setText("Client");
                    }
                });
                tp.handlerSockets.obtainMessage(START_CLIENT).sendToTarget();
                mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(context, "Local Services Cleared!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        switch (reason) {
                            case WifiP2pManager.P2P_UNSUPPORTED:
                                Toast.makeText(context, "Local Services Clear Failed: P2p Unsuported", Toast.LENGTH_SHORT).show();
                                break;
                            case WifiP2pManager.BUSY:
                                Toast.makeText(context, "Local Services Clear Failed: BUSY", Toast.LENGTH_SHORT).show();
                                break;
                            case WifiP2pManager.ERROR:
                                Toast.makeText(context, "Local Services Clear Failed: Enternal Error", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
                discoverPeer();
            }
        }
    };

    public void changeDeviceName() {
        sprefs = context.getSharedPreferences(context.getString(R.string.AVATAR), trackingphase.MODE_PRIVATE);
        STATUS = sprefs.getString("USERTYPE", null);
        String devName = sprefs.getString("NAME", null) + " | " + STATUS;
        ChangeDeviceName changeDeviceName = new ChangeDeviceName(mManager, mChannel);
        changeDeviceName.setDeviceName(devName);
        if (STATUS.equals("Host")) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                Log.d(TAG, "Permission Needed on Group Create!");
                return;
            }
            mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(context, "Group Created! Client can now Connect!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, " Group Created!");
                }

                @Override
                public void onFailure(int reason) {

                }
            });
        }
    }

    public void connectPeer(WifiP2pConfig config, final WifiP2pDevice device) {
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
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(context, "Connecting to" + device.deviceName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(context, "Cant connect to " + device.deviceName, Toast.LENGTH_SHORT).show();
                Toast.makeText(context, "Error Code: " + reason, Toast.LENGTH_SHORT).show();
            }
        });
}

    public void disconnect() {
        if (mManager != null && mChannel != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null) {
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Toast.makeText(context,"Group Disconnect Success!", Toast.LENGTH_LONG).show();
                                Log.d(TAG, "Disconnected");
                                if(STATUS=="Host"){
                                    serverInit.interrupt();
                                }else if(STATUS=="Client"){
                                    clientInit.interrupt();
                                }
                            }

                            @Override
                            public void onFailure(int reason) {
                                Toast.makeText(context,"Group Disconnected Failed!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
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

    public InetAddress getGroupOwnerAddress() {
        return groupOwnerAddress;
    }
}
