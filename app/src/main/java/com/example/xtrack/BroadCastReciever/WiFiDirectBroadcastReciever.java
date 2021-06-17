package com.example.xtrack.BroadCastReciever;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.xtrack.MainActivity;
import com.example.xtrack.P2pDiscovery.P2pDiscovery;
import com.example.xtrack.trackingphase;

public class WiFiDirectBroadcastReciever extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private ConnectivityManager mCManager;
    private P2pDiscovery p2pDiscovery;
    public static final String TAG = "WifiDirectBR";
    private trackingphase tp;


    public WiFiDirectBroadcastReciever(P2pDiscovery p2pDiscovery,trackingphase tp, WifiP2pManager mManager, WifiP2pManager.Channel mChannel, ConnectivityManager mCManager) {
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.mCManager = mCManager;
        this.p2pDiscovery = p2pDiscovery;
        this.tp = tp;
        Log.d(TAG,"Running Broadcast Reciever");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "Wifi is ON", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Wifi is OFF", Toast.LENGTH_SHORT).show();
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //do something
            if (mManager != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    System.out.println("Permission Not Granted");
                }else {
                    System.out.println("Permission Granted");
                }
                System.out.println("requesting peer");
                mManager.requestPeers(mChannel, p2pDiscovery.peerListListener);
            }
        }else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)){
            //do something
            if(mManager == null){
                Toast.makeText(context, "mManager is null", Toast.LENGTH_SHORT).show();
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, p2pDiscovery.connectionInfoListener);
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE ));
                } else {
                    //deprecated in API 26
                    v.vibrate(2000);
                }
            }else {
                tp.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tp.connectionStatus.setText("Device Disconnected!");
                    }
                });
                Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
// Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    //deprecated in API 26
                    v.vibrate(2000);
                }
            }

        }else if(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)){
            //do something
            Toast.makeText(context, "this device change action", Toast.LENGTH_SHORT).show();
        }
    }
}
