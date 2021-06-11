package com.example.xtrack.InitThreads;


import com.example.xtrack.AsyncTasks.SendRecieve;
import com.example.xtrack.MainActivity;
import com.example.xtrack.trackingphase;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.os.Handler;

public class ClientInit extends Thread {
    Socket socket;
    String hostAdd;
    public SendRecieve sendRecieve;
    trackingphase trackingPhase;
    Handler threadHandler;
    int count = 0;

    public ClientInit(InetAddress hostAddress, trackingphase trackingPhase, Handler handler, Handler threadHandler) {
        this.threadHandler = threadHandler;
        hostAdd = hostAddress.getHostAddress();
        this.trackingPhase = trackingPhase;
    }


    @Override
    public void interrupt() {
        super.interrupt();
        sendRecieve.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(hostAdd, 8888), 1000);
            sendRecieve = new SendRecieve(socket,trackingPhase,threadHandler);
            sendRecieve.setName("SRThread as Client");
            sendRecieve.start();
        } catch (IOException e) {
            e.printStackTrace();
            if(count<5) {
                count++;
                this.run();
            }else{
                trackingPhase.disconnect();
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
