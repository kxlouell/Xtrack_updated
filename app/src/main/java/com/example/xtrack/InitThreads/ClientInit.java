package com.example.xtrack.InitThreads;


import com.example.xtrack.AsyncTasks.SendRecieve;
import com.example.xtrack.MainActivity;
import com.example.xtrack.Tools.MacIPFinder;
import com.example.xtrack.trackingphase;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

public class ClientInit extends HandlerThread {

    public static final int EXECUTE_TASK = 0;
    public static final int RUN_SENDRECIEVE = 1;
    public static final int INIT_CLIENT = 3;
    private boolean IS_IP_INIT = false;
    public static final String TAG = "Client Init";
    Socket socket;
    String hostAdd;
    public SendRecieve sendRecieve;
    trackingphase trackingPhase;
    public Handler clientHandler;

    public ClientInit(InetAddress hostAddress, trackingphase trackingPhase) {
        super("Client Init", 9);
        hostAdd = hostAddress.getHostAddress();
        this.trackingPhase = trackingPhase;
    }

    @SuppressWarnings("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        clientHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case EXECUTE_TASK:
                        executeTask();
                        break;
                    case RUN_SENDRECIEVE:

                        break;
                    case INIT_CLIENT:
                        IS_IP_INIT=true;
                        break;
                }

            }
        };
    }

    @Override
    public void interrupt() {
        Log.d(TAG, " Interrupted!");
        sendRecieve.interrupt();
        quit();

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.interrupt();

    }

    public void executeTask() {
            Log.d(TAG, " Executing Task: " + currentThread().getName());
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(hostAdd, 8888), 0);
                Log.d(TAG, "Connected to" + socket.getInetAddress());
                sendRecieve = new SendRecieve(socket, trackingPhase);
                sendRecieve.setName("SRThread as Client");
                sendRecieve.start();
                while (sendRecieve.handler == null) {
                    Log.d(TAG, " Waiting for handler");
                }
                sendRecieve.handler.obtainMessage(sendRecieve.EXECUTE_TASK).sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }




    public Socket getSocket() {
        return socket;
    }
}
