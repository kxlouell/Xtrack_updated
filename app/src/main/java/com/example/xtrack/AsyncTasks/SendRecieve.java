package com.example.xtrack.AsyncTasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.xtrack.Adapters.ConnectedDevice;
import com.example.xtrack.R;
import com.example.xtrack.Tools.MacIPFinder;
import com.example.xtrack.trackingphase;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class SendRecieve extends HandlerThread {
    public boolean IS_CLIENT_INIT;
    public boolean IS_AVATAR_INIT;
    public static final String TAG = "SendRecieve";
    public static final int SEND_IP = 0;
    public static final int SEND_AVATAR = 1;
    public static final int SEND_LOCATION = 2;
    public static final int EXECUTE_TASK = 3;
    public static final int REQUEST_CLIENT_INIT = 4;
    private static Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private trackingphase trackingPhase;
    public  Handler handler;
    private Handler threadHandler;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    Context context;

    public SendRecieve(Socket skt, trackingphase trackingPhase) {
        super("SendRecieveThread");
        this.trackingPhase = trackingPhase;
        this.threadHandler = trackingPhase.threadHandler;
        context = trackingPhase;
        socket = skt;


    }

    @Override
    public void interrupt() {
        super.interrupt();
        Log.d(TAG, " Interrupted!");
        quit();
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        IS_AVATAR_INIT = false;
        handler =  new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                Log.d(TAG," MessageWhatSR is "+msg.what);
                switch (msg.what){
                    case SEND_IP:
                        try {
                            Log.d(TAG,"Handler Send_IP");
                            sendIP();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case SEND_AVATAR:
                        Log.d(TAG, "Send Avatar Queue");
                        SharedPreferences sprefs = context.getSharedPreferences(context.getString(R.string.AVATAR), context.MODE_PRIVATE);
                        String devName = sprefs.getString("NAME", null);
                        Bundle avatar_bundle = new Bundle();
                        int avatar = avatar_bundle.getInt("ICON");;
                        try {
                            initAvatar(avatar,devName);
                            IS_AVATAR_INIT = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case SEND_LOCATION:
                        Log.d(TAG, " Send Location Queue");
                        Bundle bb = (Bundle) msg.obj;
                        double lat = bb.getDouble("lat");
                        double lon = bb.getDouble("lon");
                        System.out.println(Thread.currentThread()+" "+msg);
                        System.out.println(Thread.currentThread()+" "+msg.getData());
                        System.out.println(Thread.currentThread()+" "+"Handler = lat: "+bb.getDouble("lat")+" | lon: "+bb.getDouble("lon"));
                        sendLatlong(lat,lon);
                        break;
                    case EXECUTE_TASK:
                        try {
                            executeTask();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                    case REQUEST_CLIENT_INIT:
                        Log.d(TAG, " Request client init");
                        if(IS_CLIENT_INIT!=true) {
                            try {
                                requestInit();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else {
                            Log.d(TAG, "Client Is Already Init" + IS_CLIENT_INIT);
                        }
                        break;
                }
            }
        };
        try {
            inputStream = socket.getInputStream();
            dataInputStream = new DataInputStream(inputStream);
            outputStream = socket.getOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            Log.d(TAG, "In and Out init: "+currentThread().getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void  executeTask() throws IOException, InterruptedException {
        Log.d(TAG, "Looper is: "+currentThread().getName());
        Log.d(TAG, "Executing Task");
        if(trackingPhase.STATUS.equals("Host")){
            requestInit();
        }else if(trackingPhase.STATUS.equals("Client")){
            sendIP();
        }
        while (socket != null){
            Log.d(TAG, " socket is not null");
                try {
                    boolean done = false;
                    while (!done) {
                        if (inputStream.available() > 0) {
                            byte messageType = dataInputStream.readByte();
                            Log.d(TAG," MessageTyp is "+messageType);
                            switch (messageType) {
                                case 0:
                                    if(trackingPhase.STATUS.equals("Host")) {
                                        if(IS_CLIENT_INIT) {
                                            dataInputStream.readUTF();
                                            dataInputStream.readUTF();
                                            dataInputStream.readInt();
                                        }else {
                                            collectIP();
                                        }
                                    }else if(trackingPhase.STATUS.equals("Client")){
                                        if(dataInputStream.readBoolean()){
                                            IS_CLIENT_INIT = true;
                                        }else{
                                            sendIP();
                                        }
                                    }
                                    break;
                                case 1:
                                    Log.d(TAG, "Avatar Recieved!");
                                    System.out.println(Thread.currentThread()+" "+"Is Byte 0");
                                    Bundle bb = new Bundle();
                                    bb.putInt("Avatar", dataInputStream.readInt());
                                    bb.putString("Name", dataInputStream.readUTF());
                                    bb.putString("Inet",socket.getInetAddress().toString());
                                    threadHandler.obtainMessage(trackingPhase.INITAVATAR, bb).sendToTarget();
                                    break;
                                case 2:
                                    Log.d(TAG, "Location Received! ");
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
                                    break;

                            }
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }


    public void sendLatlong(double lat, double lon){
        try {
            dataOutputStream.writeByte(2);
            dataOutputStream.writeDouble(lat);
            dataOutputStream.writeDouble(lon);
            dataOutputStream.writeByte(-1);
            dataOutputStream.flush();
            Log.d(TAG, Thread.currentThread()+" "+"Location Data Sent!");
        } catch (IOException e) {
            e.printStackTrace();
            //trackingPhase.disconnect();
        }
    }

    public void requestInit() throws IOException {
        dataOutputStream.writeByte(0);
        dataOutputStream.writeBoolean(IS_CLIENT_INIT);
        dataOutputStream.writeByte(-1);
        dataOutputStream.flush();
    }

    public void initAvatar(int avatar, String devName) throws IOException {
        dataOutputStream.writeByte(1);
        dataOutputStream.writeInt(avatar);
        dataOutputStream.writeUTF(devName);
        dataOutputStream.writeByte(-1);
        dataOutputStream.flush();
        System.out.println(Thread.currentThread()+" initAvatar Sent!");
    }

    public void sendIP() throws IOException, InterruptedException {
            SharedPreferences sprefs = context.getSharedPreferences(context.getString(R.string.AVATAR), context.MODE_PRIVATE);
            String devName = sprefs.getString("NAME", null);
            byte[] IP = MacIPFinder.getLocalIPAddress();
            String dottedIP = MacIPFinder.getDottedDecimalIP(IP);
            int ICON = sprefs.getInt("ICON", 0);
            Log.d(TAG," Dotted IP is "+dottedIP);
            dataOutputStream.writeByte(0);
            dataOutputStream.writeUTF(dottedIP);
            dataOutputStream.writeUTF(devName);
            dataOutputStream.writeInt(ICON);
            dataOutputStream.writeByte(-1);
            dataOutputStream.flush();
            Log.d(TAG, "Send IP " +dottedIP);
            if(trackingPhase.sendLocation.isAlive()){
                System.out.println("Send Location Alread Started on Send IP");
            }else {
                trackingPhase.handlerSockets.obtainMessage(trackingPhase.START_SEND_LOC).sendToTarget();
            }
    }


    public void collectIP() throws IOException {
        Log.d(TAG, "Recieving IP");
        String IP = dataInputStream.readUTF();
        String devName = dataInputStream.readUTF();
        int ICON = dataInputStream.readInt();
        String s = "New Client: "+devName+" | "+IP;
        Log.d(TAG, s);
        System.out.println(s);
        IS_CLIENT_INIT=true;
        if(trackingPhase.sendLocation.isAlive()){
            System.out.println("Send Location Alread Started on collectIP");
        }else {
            trackingPhase.handlerSockets.obtainMessage(trackingPhase.START_SEND_LOC).sendToTarget();
        }
        trackingPhase.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(trackingPhase.connectedDeviceArrayList.size()==0) {
                    trackingPhase.connectedDeviceArrayList.add(new ConnectedDevice(ICON, devName, "Connected"));
                    trackingPhase.connectedDevList.setAdapter(trackingPhase.connectedDeviceAdapter);
                }else {
                    trackingPhase.connectedDeviceArrayList.add(new ConnectedDevice(ICON, devName, "Connected"));
                }
            }
        });
    }

    public void disconnecting(){

    }

    public Socket getSocket() {
        return socket;
    }

    public Handler getHandler() {
        return handler;
    }
}