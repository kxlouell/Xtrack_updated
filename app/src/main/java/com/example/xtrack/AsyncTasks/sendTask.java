package com.example.xtrack.AsyncTasks;


import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.xtrack.trackingphase;

public class sendTask extends AsyncTask<Void,Void,Void> {


    private SendRecieve sendRecieve;
    private trackingphase trackingPhase;
    private Bundle bb;


    public sendTask(trackingphase trackingPhase, SendRecieve sendRecieve, Bundle bb){
        this.trackingPhase = trackingPhase;
        this.sendRecieve = sendRecieve;
        this.bb = bb;
    }

    @Override
    protected void onPreExecute() {
        Toast.makeText(trackingPhase, "PreExecute sendTask!="+bb.getDouble("lat")+" | "+bb.getDouble("lon"), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        sendRecieve.sendLatlong(bb.getDouble("lat"),bb.getDouble("lon"));
        return null;
    }
}

