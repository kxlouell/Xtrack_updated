package com.example.xtrack.AsyncTasks;

import android.os.AsyncTask;
import android.os.Bundle;

import com.example.xtrack.trackingphase;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;

import java.util.Map;


public class plotTaskClient extends AsyncTask<Void,Void,Void> {
    private SendRecieve sendRecieve;
    private Bundle locationBundle;
    private Map<String, Symbol> symbolIP;
    private SymbolManager symbolManager;
    private double lat;
    private double lon;
    private String inet;
    private Symbol symbolPlot;
    private trackingphase trackingPhase;

    public plotTaskClient(trackingphase trackingPhase, SendRecieve sendRecieve, Bundle locationData, Map<String, Symbol> symbolIP, SymbolManager symbolManager){
        this.sendRecieve = sendRecieve;
        this.locationBundle = locationData;
        this.symbolIP = symbolIP;
        this.symbolManager = symbolManager;
        this.trackingPhase = trackingPhase;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        Thread.currentThread().setName("ploTaskClient");
        lat = locationBundle.getDouble("lat");
        lon = locationBundle.getDouble("lon");
        inet = locationBundle.getString("Inet");
        symbolPlot = symbolIP.get(inet);
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
