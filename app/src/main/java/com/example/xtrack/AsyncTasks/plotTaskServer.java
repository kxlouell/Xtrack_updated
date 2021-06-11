package com.example.xtrack.AsyncTasks;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.xtrack.trackingphase;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class plotTaskServer extends AsyncTask<Void,Void,Void> {

    private List<SendRecieve> sendRecieve;
    private Bundle locationBundle;
    private Map<String, Symbol> symbolIP;
    private SymbolManager symbolManager;
    private double lat;
    private double lon;
    private String inet;
    private Symbol symbolPlot;
    private trackingphase trackingPhase;



    public plotTaskServer(trackingphase trackingPhase, List<SendRecieve> sendRecieves, Bundle locationData, Map<String, Symbol> symbolIP, SymbolManager symbolManager){
        this.sendRecieve = sendRecieves;
        this.locationBundle = locationData;
        this.symbolIP = symbolIP;
        this.symbolManager = symbolManager;
        this.trackingPhase = trackingPhase;


    }
    @Override
    protected Void doInBackground(Void... voids) {
        Thread.currentThread().setName("plotTaskServer");
        lat = locationBundle.getDouble("lat");
        lon = locationBundle.getDouble("lon");
        inet = locationBundle.getString("Inet");
        symbolPlot = symbolIP.get(inet);
        if(sendRecieve.size()>1){
            System.out.println(Thread.currentThread()+" SendRecive size"+sendRecieve.size());
            for(SendRecieve sr  : sendRecieve){
                if(sr.getSocket().getInetAddress().toString()!=inet){
                    sr.getHandler().obtainMessage(1, locationBundle).sendToTarget();
                    System.out.println(Thread.currentThread().getName()+"Loc Transfered From "+inet+" to "+sr.getSocket().getInetAddress());
                }
            }
        }

        return null;
    }
    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        System.out.println(Thread.currentThread().getName()+"New location to plot: "+lat+" | "+lon+" From: "+inet);
        if(symbolPlot!=null){
            symbolPlot.setLatLng(new LatLng(lat, lon));
            symbolManager.update(symbolPlot);
            System.out.println(Thread.currentThread().getName()+"Plotted! "+lat+" | "+lon+" From: "+inet);
        }else{
            System.out.println(Thread.currentThread()+"Symbol is null!");
        }
    }
}
