package com.example.xtrack.AsyncTasks;


import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.example.xtrack.trackingphase;

public class sendAvatar extends AsyncTask<Void,Void,Void> {


    private SendRecieve sendRecieve;
    private trackingphase trackingPhase;
    private int avatar;


    public sendAvatar(trackingphase trackingPhase, SendRecieve sendRecieve, int avatar){
        this.trackingPhase = trackingPhase;
        this.sendRecieve = sendRecieve;
        this.avatar = avatar;
    }

    @Override
    protected void onPreExecute() {
        Toast.makeText(trackingPhase, "PreExecute sendAvatar!="+avatar, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        sendRecieve.initAvatar(avatar);
        return null;
    }
}

