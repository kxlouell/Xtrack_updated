package com.example.xtrack.AsyncTasks;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.example.xtrack.trackingphase;

public class SendRecieve extends HandlerThread {
    private static Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private trackingphase trackingPhase;
    public  Handler handler, threadHandler;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public SendRecieve(Socket skt, trackingphase trackingPhase, Handler threadHandler) {
        super("SendRecieveThread");
        this.trackingPhase = trackingPhase;
        this.threadHandler = threadHandler;
        socket = skt;

    }


    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        handler =  new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case 0:
                        int avatar = (int) msg.obj;
                        sendAvatar xTask = new sendAvatar(trackingPhase,SendRecieve.this, avatar);
                        xTask.execute();
                        break;
                    case 1:
                        Bundle bb = (Bundle) msg.obj;
                        System.out.println(msg);
                        System.out.println(msg.getData());
                        System.out.println("Handler = lat: "+bb.getDouble("lat")+" | lon: "+bb.getDouble("lon"));
                        sendTask sTask = new sendTask(trackingPhase,SendRecieve.this, bb);
                        sTask.execute();
                        break;

                }
            }
        };

    }


    @Override
    public void run() {
        onLooperPrepared();
        System.out.println("Done Looper Prepared");
        try {
            inputStream = socket.getInputStream();
            dataInputStream = new DataInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (socket != null) {
            if (socket.isConnected()) {
                System.out.println("Socket is Connected!");
                try {
                    boolean done = false;
                    System.out.println("DataInputStream Success!");
                    while (!done) {
                        if (inputStream.available() > 0) {
                            System.out.println("Is Available");
                            byte messageType = dataInputStream.readByte();
                            System.out.println(messageType);
                            switch (messageType) {
                                case 0:
                                    Bundle bb = new Bundle();
                                    bb.putInt("Avatar", dataInputStream.readInt());
                                    //bb.putI
                                    //handler.obtainMessage(trackingPhase.INITAVATAR, avatar).sendToTarget();
                                    threadHandler.obtainMessage(trackingPhase.mTOAST, 1, -1, "Message Recieved" + dataInputStream.readUTF()).sendToTarget();
                                    break;
                                case 1:
                                    System.out.println("Is Case 1");
                                    double lat = dataInputStream.readDouble();
                                    double lon = dataInputStream.readDouble();
                                    System.out.println("lat is " + lat);
                                    System.out.println("lon is " + lon);
                                    Bundle locationBundle = new Bundle();
                                    locationBundle.putDouble("lat", lat);
                                    locationBundle.putDouble("lon", lon);
                                    threadHandler.obtainMessage(trackingPhase.PLOTLOCATION,locationBundle).sendToTarget();
                                    break;
                                default:
                                    done = true;
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void sendLatlong(double lat, double lon){
        threadHandler.obtainMessage(trackingPhase.mTOAST, 1, -1, "New location to be send on SendRecieve!").sendToTarget();
        try {
            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeByte(1);
            dataOutputStream.writeDouble(lat);
            dataOutputStream.writeDouble(lon);
            dataOutputStream.flush();
            dataOutputStream.writeByte(-1);
            dataOutputStream.flush();
            System.out.println("Data Sent!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initAvatar(int avatar){
        try {
            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeByte(0);
            dataOutputStream.writeInt(avatar);
            dataOutputStream.flush();
            System.out.println("initAvatar Sent!");
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    public Handler getHandler() {
        return handler;
    }
}