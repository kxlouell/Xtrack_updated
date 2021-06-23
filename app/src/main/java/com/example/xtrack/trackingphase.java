package com.example.xtrack;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.xtrack.Adapters.ConnectedDevice;
import com.example.xtrack.Adapters.connectedDeviceAdapter;
import com.example.xtrack.AsyncTasks.SendLocation;
import com.example.xtrack.AsyncTasks.SendRecieve;
import com.example.xtrack.AsyncTasks.plotTaskClient;
import com.example.xtrack.AsyncTasks.plotTaskServer;
import com.example.xtrack.InitThreads.ClientInit;
import com.example.xtrack.InitThreads.ServerInit;
import com.example.xtrack.P2pDiscovery.P2pDiscovery;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.lang.ref.WeakReference;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.jetbrains.annotations.NotNull;

import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;


/**
 * Use the Mapbox Core Library to receive updates when the device changes location.
 */
public class trackingphase extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    private int FINE_LOCATION_PERMISION_CODE = 1;
    public static final int START_SERVER = 0;
    public static final int START_CLIENT = 1;
    public static final int START_SEND_LOC = 2;
    public String STATUS, USER;;
    public int USER_ICON;
    public int FIRST_RUN = 0;
    private static final String NEW_LAYER_ID = "NEW_LAYER_ID";
    double trying = -86.78160;
    private ServerSocket dumySskt;
    private Socket dumySkt;
    private static final String TAG = "Tracking Phase";
    public boolean IS_AVATAR_INIT = false;

    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationChangeListeningActivityLocationCallback callback =
            new LocationChangeListeningActivityLocationCallback(this);
    LocationComponent locationComponent;

    ImageView btn_peersImageView, btn_notification, btn_previous, userICON;
    ImageButton btn;
    public static ListView peersListView;
    public ListView connectedDevList;
    Style style;

    public TextView connectionStatus, usernameTV,userStatusTV, connectedPeers;

    MainActivity mActivity;
    public static ServerInit serverInit;
    public static ClientInit clientInit;
    public SendLocation sendLocation;
    P2pDiscovery p2pDiscovery;


    private static final String SOURCE_ID = "SOURCE_ID";
    private static final String ICON_ID = "ICON_ID";
    private static final String LAYER_ID = "LAYER_ID";
    FloatingActionButton centerLoc;
    SymbolManager symbolManager;
    Map<String, Symbol> symbolIP = new HashMap<>();

    public ArrayList<ConnectedDevice> connectedDeviceArrayList;
    public connectedDeviceAdapter connectedDeviceAdapter;

    public static final int INITAVATAR = 1;
    public static final int mTOAST = 2;
    public static final int PLOTLOCATION = 3;

    SharedPreferences sprefs;
    Context context;

    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        sprefs = getSharedPreferences(getString(R.string.AVATAR), trackingphase.MODE_PRIVATE);
        STATUS = sprefs.getString("USERTYPE", null);
        USER = sprefs.getString("NAME", null);
        USER_ICON = sprefs.getInt("ICON", 0);
        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.access_token));
        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.tracking_phase);
        initialWork(savedInstanceState);
        exqListener();
        Log.d(TAG,"Oncreate sendlocation ");
        p2pDiscovery = new P2pDiscovery(trackingphase.this);
        p2pDiscovery.start();
        while (p2pDiscovery.handler == null){
            Log.d(TAG," Waiting");
        }
        p2pDiscovery.handler.obtainMessage(p2pDiscovery.INITWORK).sendToTarget();
        p2pDiscovery.handler.obtainMessage(p2pDiscovery.SETDEVICENAME).sendToTarget();
    }

    public Handler threadHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case INITAVATAR:
                    Log.d(TAG,"Handler Case AvatarInit");
                    Bundle bb = (Bundle) msg.obj;
                    Log.d(TAG,"threadHandler: " + bb);
                    String devName = bb.getString("Name");
                    style.addImage(bb.getString("Inet"), BitmapFactory.decodeResource(
                            trackingphase.this.getResources(), bb.getInt("Avatar")));
                    SymbolOptions symbolOptions = new SymbolOptions()
                            .withLatLng(new LatLng(0, 0))
                            .withIconImage(bb.getString("Inet"))
                            .withIconSize(0.1f)
                            .withSymbolSortKey(2f)
                            .withDraggable(false);
                    symbolIP.put(bb.getString("Inet"), symbolManager.create(symbolOptions));
                    Log.d(TAG,"threadHandler: " + symbolIP);
                    break;
                case mTOAST:
                    Toast.makeText(trackingphase.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case PLOTLOCATION:
                    Bundle locationBundle = (Bundle) msg.obj;
                    if (STATUS.equals("Host")) {
                        Log.d(TAG, " Plotting Loc as Host");
                        plotTaskServer ptaskServer = new plotTaskServer(trackingphase.this, serverInit.devicesSR, locationBundle, symbolIP, symbolManager);
                        ptaskServer.execute();
                    } else if (STATUS.equals("Client")) {
                        Log.d(TAG, "Plotting Loc as Client");
                        plotTaskClient plotTaskClient = new plotTaskClient(trackingphase.this, clientInit.sendRecieve, locationBundle, symbolIP, symbolManager);
                        plotTaskClient.execute();
                    }
                    break;


            }
        }
    };

    public Handler handlerSockets = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.d(TAG," New What "+msg.what);
            switch (msg.what) {
                case START_SERVER:
                    serverInit = new ServerInit(trackingphase.this);
                    serverInit.start();
                    while (serverInit.serverHandler == null){
                        Log.d(TAG," Waiting");
                    }
                    serverInit.serverHandler.obtainMessage(serverInit.EXECUTE_TASK).sendToTarget();
                    Log.d(TAG, "Server Running!");
                    break;
                case START_CLIENT:
                    clientInit = new ClientInit(p2pDiscovery.getGroupOwnerAddress(), trackingphase.this);
                    clientInit.start();
                    while (clientInit.clientHandler == null){
                        Log.d(TAG," Waiting for clientHandler");
                    }
                    clientInit.clientHandler.obtainMessage(clientInit.EXECUTE_TASK).sendToTarget();
                    Log.d(TAG, "CLient Running!");
                    while (clientInit.sendRecieve==null){
                        Log.d(TAG, " Waiting for clientSendRecive");
                    }
                    while (clientInit.sendRecieve.handler==null){
                        Log.d(TAG, " Waiting for client SendRecive Handler");
                    }
                    clientInit.sendRecieve.handler.obtainMessage(clientInit.sendRecieve.SEND_IP).sendToTarget();
                    break;
                case START_SEND_LOC:
                    Log.d(TAG,"Starting SendLocation");
                    if(sendLocation.isAlive()){
                        Log.d(TAG, " SendLocation already alive");
                    }else {
                        Log.d(TAG, " SendLocation is Starting");
                        sendLocation.start();
                    }
                    break;

            }

        }
    };

    public void initialWork(Bundle savedInstanceState) {
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        btn = (ImageButton) findViewById(R.id.backbtn);
        btn_peersImageView = (ImageView) findViewById(R.id.btn_peers);
        btn_notification = (ImageView) findViewById(R.id.btn_notification);
        btn_peersImageView.setVisibility(View.VISIBLE);
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        mActivity = new MainActivity();
        peersListView = findViewById(R.id.peerList);
        centerLoc = (FloatingActionButton) findViewById(R.id.centerLoc);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        btn_previous = (ImageView) findViewById(R.id.tp_previous);
        userICON = findViewById(R.id.user_tp);
        usernameTV = findViewById(R.id.tp_user_name);
        userStatusTV = findViewById(R.id.tp_STATUS);
        connectedDevList = findViewById(R.id.connectedDevList);

        //Setting up USERS
        userICON.setImageResource(USER_ICON);
        usernameTV.setText(USER);
        userStatusTV.setText(STATUS);
        connectedPeers = findViewById(R.id.connectedPeers);
        connectedDeviceArrayList = new ArrayList<>();
        connectedDeviceAdapter = new connectedDeviceAdapter(this,R.layout.connected_device_rows,connectedDeviceArrayList);
        sendLocation = new SendLocation(trackingphase.this);
    }

    public void exqListener() {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openmainactivity();
            }
        });

        btn_peersImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(AnimationUtils.loadAnimation(trackingphase.this, R.anim.click_anim));
                Log.d(TAG,"P2pDiscovery is :"+p2pDiscovery);
                Log.d(TAG,"Handler is :"+p2pDiscovery.handler);
                p2pDiscovery.handler.obtainMessage(p2pDiscovery.DISCOVER).sendToTarget();
            }
        });

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull @NotNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull @NotNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull @NotNull View drawerView) {
                drawerLayout.setVisibility(View.GONE);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        btn_notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(trackingphase.this, "Clicked Notification!", Toast.LENGTH_SHORT).show();
                drawerLayout.setVisibility(View.VISIBLE);
                v.startAnimation(AnimationUtils.loadAnimation(trackingphase.this, R.anim.click_anim));
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        btn_previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(AnimationUtils.loadAnimation(trackingphase.this, R.anim.click_anim));
                if(drawerLayout.isDrawerOpen(GravityCompat.END)){
                    drawerLayout.closeDrawer(GravityCompat.END);
                    drawerLayout.setVisibility(View.GONE);
                }
            }
        });

        peersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                WifiP2pDevice[] deviceArray = p2pDiscovery.getDeviceArray();
                final WifiP2pDevice device = deviceArray[position];
                Log.d(TAG,"Device Name is " + device.deviceName.contains("Client"));
                Log.d(TAG,"Status is "+STATUS);
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                config.groupOwnerIntent = 0;

                if (STATUS.equals("Client") && device.deviceName.contains("Client")) {
                    new AlertDialog.Builder(trackingphase.this)
                            .setTitle("That Device is Client")
                            .setMessage("Can't connect to a Client when you're also a Client")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                } else if (STATUS.equals("Host") && device.deviceName.contains("Host")) {
                    new AlertDialog.Builder(trackingphase.this)
                            .setTitle("Connecting Host as a Host")
                            .setMessage("Can't connect to a Host when you're also a Host")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                } else if (STATUS.equals("Host") && device.deviceName.contains("Client")) {
                    new AlertDialog.Builder(trackingphase.this)
                            .setTitle("Connecting Client as a Host")
                            .setMessage("Can't connect to a Client when you're a Host")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                } else if (STATUS.equals("Client") && device.deviceName.contains("Host")) {
                    if (ActivityCompat.checkSelfPermission(trackingphase.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions();
                    } else {
                        Log.d(TAG,"Access Fine Location Permitted!");
                        Bundle bb = new Bundle();
                        p2pDiscovery.handler.post(new Runnable() {
                            @Override
                            public void run() {
                            p2pDiscovery.connectPeer(config, device);
                            }
                        });

                    }

                }
            }

        });


        centerLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lat = locationComponent.getLastKnownLocation().getLatitude();
                double lon = locationComponent.getLastKnownLocation().getLongitude();
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(lat, lon))
                        .zoom(18).build();
                mapboxMap.setCameraPosition(cameraPosition);
                if(STATUS.equals("Client")){
                    Toast.makeText(trackingphase.this, "ClienInit is alive: " +clientInit.isAlive()+"| ClientHandler is: "+clientInit.clientHandler.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void openmainactivity() {
        Intent intent = new Intent (getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

    //button

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(new Style.Builder().fromUri(Style.TRAFFIC_NIGHT)
                .withImage("ID_ICON_A1",
                        BitmapFactory.decodeResource(
                                trackingphase.this.getResources(), R.drawable.mapbox_marker_icon_default))
                .withSource(new GeoJsonSource("marker-source-id"))
                .withLayer(new SymbolLayer(NEW_LAYER_ID,
                        "marker-source-id").withProperties(
                        iconImage("ID_ICON_A1"),
                        visibility(VISIBLE),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true)
                )),
                new Style.OnStyleLoaded() {
                    @Override public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        trackingphase.this.style = style;
                        symbolManager = new SymbolManager(mapView, mapboxMap, style);
                        symbolManager.setIconAllowOverlap(true);
                        symbolManager.setTextAllowOverlap(true);
                        symbolManager.setIconTranslate(new Float[]{-4f, 5f});

                        style.addImage("ID_ICON_A2", BitmapFactory.decodeResource(
                                trackingphase.this.getResources(), R.drawable.ic_unknown_user));
                    }
                 });
    }

    /**
     * Initialize the Maps SDK's LocationComponent
     */
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();

            // Set the LocationComponent activation options
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, loadedMapStyle)
                            .useDefaultLocationEngine(false)
                            .build();

            // Activate with the LocationComponentActivationOptions object
            locationComponent.activateLocationComponent(locationComponentActivationOptions);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);


            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    /**
     * Set up the LocationEngine and the parameters for querying the device's location
     */
    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
        }
    }



    private static class LocationChangeListeningActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<trackingphase> activityWeakReference;
        trackingphase trackingPhase;

        LocationChangeListeningActivityLocationCallback(trackingphase activity) {
            this.activityWeakReference = new WeakReference<>(activity);
            this.trackingPhase = activity;
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */

        @Override
        public void onSuccess(LocationEngineResult result) {
            trackingphase activity = activityWeakReference.get();

            if (activity != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

                // Create a Toast which displays the new location's coordinates
                // Pass the new location to the Maps SDK's LocationComponent
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                    Bundle bb = new Bundle();
                    bb.putDouble("lat", result.getLastLocation().getLatitude());
                    bb.putDouble("lon", result.getLastLocation().getLongitude());
                    if(trackingPhase.sendLocation.isAlive()){
                        Log.d(TAG,"Send Loc Que to Handler");
                        trackingPhase.sendLocation.handler.obtainMessage(trackingPhase.sendLocation.SEND_LOCATION, bb).sendToTarget();
                    }



                }
                }
            }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can't be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            trackingphase activity = activityWeakReference.get();
            if (activity != null) {
                Toast.makeText(activity, exception.getLocalizedMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        p2pDiscovery.quit();
        super.onStop();
        mapView.onStop();
        if(p2pDiscovery.isAlive()){
            if(STATUS.equals("Host")){
                serverInit.sendRecieve.quit();
                serverInit.quit();
            }else if(STATUS.equals("Client")){
                clientInit.quit();
                clientInit.sendRecieve.quit();
            }
            p2pDiscovery.quit();
            p2pDiscovery = null;
            Log.d(TAG, "P2pDiscovery is dead!"+p2pDiscovery);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendLocation.interrupt();

        if(p2pDiscovery.isAlive()){
            if(STATUS.equals("Host")){
                serverInit.sendRecieve.quit();
                serverInit.quit();
            }else if(STATUS.equals("Client")){
                clientInit.quit();
                clientInit.sendRecieve.quit();
            }
            p2pDiscovery.quit();
            p2pDiscovery = null;
            Log.d(TAG, "P2pDiscovery is dead!"+p2pDiscovery);
        }else {
            Log.d(TAG, "P2pDiscovery is already dead!" + p2pDiscovery);
        }
        sendLocation.quit();
        // Prevent leaks
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
        btn_peersImageView.setVisibility(View.INVISIBLE);
    }



    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void requestPermissions() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Ex-Track need to access your location")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(trackingphase.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISION_CODE);
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
}