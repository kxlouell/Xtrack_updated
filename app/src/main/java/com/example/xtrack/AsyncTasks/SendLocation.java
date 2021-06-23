package com.example.xtrack.AsyncTasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.xtrack.R;
import com.example.xtrack.trackingphase;

public class SendLocation extends HandlerThread {

    public static final String TAG = "Send Location Thread";
    double lastLat=0, lastLon=0;
    public static final int SEND_LOCATION = 0;
    public int FIRST_RUN = 0;

    public Handler handler;
    private trackingphase tp;
    Context context;
    SharedPreferences sprefs;

    public SendLocation(trackingphase tp) {
        super("Send Location", 8);
        this.tp = tp;
        this.context = tp;
        sprefs = context.getSharedPreferences(context.getString(R.string.AVATAR), context.MODE_PRIVATE);
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        handler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case SEND_LOCATION:
                        Log.d(TAG," Send Location Queue Recieved");
                        Bundle bb = (Bundle) msg.obj;
                        double lat = bb.getDouble("lat");
                        double lon = bb.getDouble("lon");
                        sendLoc(lat,lon);
                        break;
                }
            }
        };
    }

    @Override
    public void interrupt() {
        quit();
        super.interrupt();

    }

    public void sendLoc(double resultLat, double resultLon){
        Log.d(TAG,"Executing SendLoc");
        double lastlat = lastLat;
        double lastlon = lastLon;
        double newlat = 0;
        double newlon = 0;
        if(lastlat>resultLat){
            newlat = lastLat - resultLat;
        }else{
            newlat = resultLat-lastLat;
        }
        if(lastlon>resultLon){
            newlon = lastLon - resultLon;
        }else{
            newlon = resultLon-lastLon;
        }
        Log.d(TAG,"Range "+newlat+" "+newlon);
        if (Math.abs(newlat) >=  0.000001||Math.abs(newlon) >= 0.000001) {
            if (tp.STATUS.equals("Host")) {
                Log.d(TAG," SendingLoc as Server!\n\n\n" + FIRST_RUN);
                Bundle bb = new Bundle();
                bb.putDouble("lat", resultLat);
                bb.putDouble("lon", resultLon);
                Log.d(TAG," First Run is" + FIRST_RUN);
                int count = 0;
                Log.d(TAG, "DeviceSRVector size: "+tp.serverInit.deviceSRVector);
                synchronized (tp.serverInit.devicesSR){
                    Log.d(TAG,"DevicesSR size: " + tp.serverInit.devicesSR.size());
                }

                for (SendRecieve sr : tp.serverInit.devicesSR) {
                    if (FIRST_RUN == 0 && sr!=null) {
                        int icon = sprefs.getInt("ICON", 0);
                        Log.d(TAG, " Icon is"+icon);
                        if (icon > 0) {
                            Log.d(TAG," Send Avatar as server");
                            sr.handler.obtainMessage(1, sprefs.getInt("ICON", 0)).sendToTarget();
                            Log.d(TAG," TrackingPhase " + FIRST_RUN);
                            FIRST_RUN = 1;
                        }
                    } else if(FIRST_RUN==1){
                        sr.handler.obtainMessage(2, bb).sendToTarget();
                        Log.d(TAG," Sending Location!");
                    }else if(sr==null){
                        Toast.makeText(context, "SR is Null", Toast.LENGTH_SHORT).show();
                    }
                    count++;
                }
                Log.d(TAG,"Count is" + count);
            } else if (tp.STATUS.equals("Client")) {
                Log.d(TAG,"SendingLoc as Client!\n\n\n");
                Bundle bb = new Bundle();
                bb.putDouble("lat", resultLat);
                bb.putDouble("lon", resultLon);
                Log.d(TAG,"First Run is" + FIRST_RUN);
                if (FIRST_RUN == 0) {
                    Log.d(TAG,"Sending Avatar as client");
                    int icon = sprefs.getInt("ICON", 0);
                    Log.d(TAG, "icon is"+icon);
                    if(tp.clientInit.sendRecieve.IS_AVATAR_INIT==true){
                        Log.d(TAG, " AVATAR is INIT");
                    }else if(tp.clientInit.sendRecieve.IS_AVATAR_INIT!=true){
                        Log.d(TAG, "HANDLER SF" + tp.clientInit.sendRecieve.handler.toString());
                        tp.clientInit.sendRecieve.handler.obtainMessage(1, icon).sendToTarget();
                        Log.d(TAG, "TrackingPhase " + FIRST_RUN);
                        }
                    FIRST_RUN = 1;
                } else if(FIRST_RUN == 1){
                    tp.clientInit.sendRecieve.handler.obtainMessage(2, bb).sendToTarget();
                    Log.d(TAG," Sending Location!");
                }
            }

        }
        lastLat = resultLat;
        lastLon = resultLon;
    }
}
