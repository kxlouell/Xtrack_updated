package com.example.xtrack.InitThreads;


import android.annotation.SuppressLint;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.xtrack.AsyncTasks.SendRecieve;
import com.example.xtrack.trackingphase;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerInit extends HandlerThread {
    private static final String TAG = "ServerInit";
    public static final int EXECUTE_TASK = 0;
    Socket socket = null;
    ServerSocket serverSocket;
    public SendRecieve sendRecieve;
    trackingphase trackingPhase;
    Handler threadHandler;
    WifiP2pDeviceList devices;
    public CopyOnWriteArrayList<SendRecieve> devicesSR;
    ArrayList<Socket> clientSockets;
    public Vector<SendRecieve> deviceSRVector;
    public static ArrayList<InetAddress> clients;
    public static Handler serverHandler;

    public ServerInit(trackingphase trackingPhase){
        super("ServerInit", 9);
        this.trackingPhase = trackingPhase;
        this.threadHandler = trackingPhase.threadHandler;
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        devicesSR = new CopyOnWriteArrayList<>();
        devices = new WifiP2pDeviceList();
        clients = new ArrayList<InetAddress>();
        clientSockets = new ArrayList<Socket>();
        deviceSRVector = new Vector<SendRecieve>();
        serverHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case EXECUTE_TASK:
                        executeTask();
                        break;
                }

            }
        };


    }

    public void executeTask() {
        Log.d(TAG, "Executing Task");
        try {
            serverSocket = new ServerSocket(); // <-- create an unbound socket first
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(8888));
            while(true) {
                socket = serverSocket.accept();
                sendRecieve = new SendRecieve(socket, trackingPhase);
                sendRecieve.setName("SRThread as Server: "+socket.getInetAddress());
                sendRecieve.start();
                while (sendRecieve.handler == null){
                    Log.d(TAG, "Waiting sendRecieve handler");
                }
                sendRecieve.handler.obtainMessage(sendRecieve.EXECUTE_TASK).sendToTarget();
                synchronized (devicesSR) {
                    devicesSR.add(sendRecieve);
                }
                clients.add(socket.getInetAddress());
                clientSockets.add(socket);
                socket=null;
                System.out.println(Thread.currentThread()+" DevicesSR size: "+devicesSR.size());
                Log.d(TAG, "DeviceSR Vector size "+deviceSRVector.size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void interrupt() {
        try {
            serverSocket.close();
            for (Socket skt : clientSockets){
                if (skt!=null) {
                    skt.close();
                    skt = null;
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        Log.d(TAG, " Interrupted!");
        super.interrupt();
        quit();
    }

    public static ArrayList<InetAddress> getClients() {
        return clients;
    }

}

