package com.example.xtrack.InitThreads;


import com.example.xtrack.AsyncTasks.SendRecieve;
import com.example.xtrack.MainActivity;
import com.example.xtrack.trackingphase;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.os.Handler;

public class ClientInit extends Thread {
    Socket socket;
    String hostAdd;
    public SendRecieve sendRecieve;
    trackingphase trackingPhase;
    Handler handler,threadHandler;


    public ClientInit(InetAddress hostAddress, trackingphase trackingPhase, Handler handler, Handler threadHandler) {
        this.threadHandler = threadHandler;
        hostAdd = hostAddress.getHostAddress();
        socket = new Socket();
        this.trackingPhase = trackingPhase;
        this.handler = handler;
    }


    @Override
    public void run() {
        try {
            socket.connect(new InetSocketAddress(hostAdd, 8888), 500);
            sendRecieve = new SendRecieve(socket,trackingPhase,threadHandler);
            sendRecieve.setName("SRThread as Client");
            sendRecieve.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
