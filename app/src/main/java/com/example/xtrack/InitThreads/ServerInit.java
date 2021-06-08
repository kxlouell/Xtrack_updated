package com.example.xtrack.InitThreads;


import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.xtrack.AsyncTasks.SendRecieve;
import com.example.xtrack.trackingphase;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerInit extends Thread {
    Socket socket;
    ServerSocket serverSocket;
    public SendRecieve sendRecieve;
    trackingphase trackingphase;
    Handler handler, threadHandler;
    WifiP2pDeviceList devices;
    WifiP2pDevice device;
    List<SendRecieve> devicesSR;
    public static ArrayList<InetAddress> clients;
    public static Map<String,Socket> clientSockets;

    public ServerInit(trackingphase trackingPhase, Handler handler, Handler threadHandler){
        devicesSR = new ArrayList<SendRecieve>();
        devices = new WifiP2pDeviceList();
        this.trackingphase = trackingPhase;
        this.handler = handler;
        this.threadHandler = threadHandler;
        clients = new ArrayList<InetAddress>();
        clientSockets = new HashMap<String,Socket>();
    }


    @Override
    public void run() {
        clients.clear();
        clientSockets.clear();

        try {
            serverSocket = new ServerSocket(8888);
            while(true) {
                socket = serverSocket.accept();
                if(socket.isConnected()){
                    sendRecieve = new SendRecieve(socket, trackingphase, threadHandler);
                    sendRecieve.setName("SRThread as Server: "+socket.getInetAddress());
                    sendRecieve.start();
                    devicesSR.add(sendRecieve);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Socket getSocket() {
        return socket;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public List<SendRecieve> getDevicesSR() {
        return devicesSR;
    }
}

