package com.example.xtrack.AsyncTasks;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.example.xtrack.trackingphase;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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

    @Override
    public void interrupt() {
        super.interrupt();

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
                        System.out.println(Thread.currentThread()+" "+msg);
                        System.out.println(Thread.currentThread()+" "+msg.getData());
                        System.out.println(Thread.currentThread()+" "+"Handler = lat: "+bb.getDouble("lat")+" | lon: "+bb.getDouble("lon"));
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
        System.out.println(Thread.currentThread()+" "+"Done Looper Prepared");
        try {
            inputStream = socket.getInputStream();
            dataInputStream = new DataInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            //trackingPhase.disconnect();
        }
        while (socket != null) {
            if (socket.isConnected()) {
                System.out.println(Thread.currentThread()+" "+"Socket is Connected!");
                try {
                    boolean done = false;
                    System.out.println(Thread.currentThread()+" "+"DataInputStream Success!");
                    while (!done) {
                        if (inputStream.available() > 0) {
                            System.out.println(Thread.currentThread()+" "+"Is Available");
                            byte messageType = dataInputStream.readByte();
                            System.out.println(Thread.currentThread()+" "+"MessageType"+messageType);
                            switch (messageType) {
                                case 0:
                                    System.out.println(Thread.currentThread()+" "+"Is Byte 0");
                                    Bundle bb = new Bundle();
                                    bb.putInt("Avatar", dataInputStream.readInt());
                                    bb.putString("Inet",socket.getInetAddress().toString());
                                    threadHandler.obtainMessage(trackingPhase.INITAVATAR, bb).sendToTarget();
                                    break;
                                case 1:
                                    System.out.println(Thread.currentThread()+" "+"Is Byte 1");
                                    double lat = dataInputStream.readDouble();
                                    double lon = dataInputStream.readDouble();
                                    System.out.println(Thread.currentThread()+" "+"lat is " + lat);
                                    System.out.println(Thread.currentThread()+" "+"lon is " + lon);
                                    Bundle locationBundle = new Bundle();
                                    locationBundle.putDouble("lat", lat);
                                    locationBundle.putDouble("lon", lon);
                                    locationBundle.putString("Inet",socket.getInetAddress().toString());
                                    threadHandler.obtainMessage(trackingPhase.PLOTLOCATION,locationBundle).sendToTarget();
                                    break;
                                default:
                                    done = true;
                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    //trackingPhase.disconnect();
                }
            }
        }
    }


    public void sendLatlong(double lat, double lon){
        try {
            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeByte(1);
            dataOutputStream.writeDouble(lat);
            dataOutputStream.writeDouble(lon);
            dataOutputStream.flush();
            dataOutputStream.writeByte(-1);
            dataOutputStream.flush();
            System.out.println(Thread.currentThread()+" "+"Data Sent!");
        } catch (IOException e) {
            e.printStackTrace();
            //trackingPhase.disconnect();
        }
    }

    public void initAvatar(int avatar){
        try {
            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeByte(0);
            dataOutputStream.writeInt(avatar);
            dataOutputStream.flush();
            System.out.println(Thread.currentThread()+" initAvatar Sent!");
        }catch (IOException e){
            e.printStackTrace();
            //trackingPhase.disconnect();
        }

    }

    public Socket getSocket() {
        return socket;
    }

    public Handler getHandler() {
        return handler;
    }
}