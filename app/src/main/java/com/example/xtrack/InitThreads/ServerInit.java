package com.example.xtrack.InitThreads;


import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.xtrack.AsyncTasks.SendRecieve;
import com.example.xtrack.trackingphase;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerInit extends Thread {
    private static final String TAG = "ServerInit";
    Socket socket = null;
    ServerSocket serverSocket;
    public SendRecieve sendRecieve;
    Handler threadHandler;
    WifiP2pDeviceList devices;
    List<SendRecieve> devicesSR;
    public static ArrayList<InetAddress> clients;

    public ServerInit(trackingphase trackingPhase, Handler handler, Handler threadHandler){
        devicesSR = new ArrayList<SendRecieve>();
        devices = new WifiP2pDeviceList();
        this.threadHandler = threadHandler;
        clients = new ArrayList<InetAddress>();
    }


    @Override
    public void run() {
        clients.clear();
        devicesSR.clear();
        try {
            serverSocket = new ServerSocket(); // <-- create an unbound socket first
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(8888));
            while(true) {
                socket = serverSocket.accept();
                //sendRecieve = new SendRecieve(socket, trackingphase, threadHandler);
                sendRecieve.setName("SRThread as Server: "+socket.getInetAddress());
                sendRecieve.start();
                devicesSR.add(sendRecieve);
                clients.add(socket.getInetAddress());
                System.out.println(Thread.currentThread()+" DevicesSR size: "+devicesSR.size());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void interrupt() {
        super.interrupt();
        try {
            serverSocket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public static ArrayList<InetAddress> getClients() {
        return clients;
    }

    public List<SendRecieve> getDevicesSR() {
        return devicesSR;
    }
}

