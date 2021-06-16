package com.example.xtrack;


import android.os.Bundle;
import android.util.Log;



import com.example.xtrack.ui.devices.DevicesFragment;
import com.example.xtrack.ui.home.HomeFragment;
import com.example.xtrack.ui.settings.SettingsFragment;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.example.xtrack.ui.devices.DevicesFragment;
import com.example.xtrack.ui.home.HomeFragment;
import com.example.xtrack.ui.settings.SettingsFragment;
import com.ismaeldivita.chipnavigation.ChipNavigationBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

//from John App
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.example.xtrack.BroadCastReciever.WiFiDirectBroadcastReciever;
import com.example.xtrack.AsyncTasks.SendRecieve;
import com.example.xtrack.InitThreads.ClientInit;
import com.example.xtrack.InitThreads.ServerInit;


public class MainActivity extends AppCompatActivity implements HomeFragment.homeFragmentListener{
    private static final String TAG = MainActivity.class.getSimpleName();

    ChipNavigationBar bottomNav;

    //From John App
    private int FINE_LOCATION_PERMISION_CODE = 1;
    private int STATUS = 0;

    public ListView listView;
    public TextView read_msg_box;
    public EditText writeMsg;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    public static final int MESSAGE_READ = 1;
    public static final int mTOAST = 2;

    ServerInit serverInit;
    ClientInit clientInit;
    SendRecieve sendRecieve;


    private ConnectivityManager mCManager;
    private ConnectivityManager.NetworkCallback mCallback;

    HomeFragment homeFragment;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
        exqListener();
        if (savedInstanceState == null) {
            bottomNav.setItemSelected(R.id.navigation_home,true);
            fragmentManager = getSupportFragmentManager();
            HomeFragment homeFragment = new HomeFragment(MainActivity.this,wifiManager);
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container,homeFragment)
                    .commit();

        }

    }


    @Override
    public void onInputSent(CharSequence cSeq) {
        homeFragment.connectionStatusText(cSeq);
    }

    Handler threadHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            Bundle bb = msg.getData();
            String str = bb.getString("udd");
            read_msg_box.setText(str);
            switch (msg.what){
                case mTOAST:
                    if(STATUS==0) {
                        Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    }
                    break;

            }
        }
    };

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    read_msg_box.setText(tempMsg);
                    Toast.makeText(MainActivity.this, "Message Recieved!!"+tempMsg, Toast.LENGTH_LONG).show();
                    break;
            }
            return true;
        }
    });


    private void exqListener() {

        bottomNav.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int id) {
                Fragment fragment = null;
                switch (id){
                    case R.id.navigation_home:
                        fragment = new HomeFragment(MainActivity.this, wifiManager);
                        break;
                    case R.id.navigation_devices:
                        fragment = new DevicesFragment();
                        break;
                    case R.id.navigation_settings:
                        fragment = new SettingsFragment();
                        break;
                }
                if (fragment!=null){
                    fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.frame_container, fragment).commit();

                }else{
                    Log.e(TAG,"error");
                }
            }
        });

    }

    private void initialWork() {
        bottomNav = findViewById(R.id.bottom_navigation);
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        //listView = (ListView) findViewById(R.id.peerListView);
        //read_msg_box = (TextView) findViewById(R.id.readMsg);
        //writeMsg = (EditText) findViewById(R.id.writeMsg);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);


        mCManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        homeFragment = new HomeFragment(MainActivity.this, wifiManager);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }



    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        disconnect();
        super.onDestroy();

    }

    public void disconnect() {
        if (mManager != null && mChannel != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions();
                return;
            }
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null) {
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Toast.makeText(getApplicationContext(),"Group Disconnect Success!", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(int reason) {
                                Toast.makeText(getApplicationContext(),"Group Disconnected Failed!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
        }
    }
    public void requestPermissions() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Ex-Track need to access your location")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }else{
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISION_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == FINE_LOCATION_PERMISION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


}


