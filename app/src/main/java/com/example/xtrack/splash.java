package com.example.xtrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // run() method will be executed when 3 seconds have passed

                //Time to start MainActivity
                Intent intent = new Intent(splash.this, Usersetup.class);
                startActivity(intent );

                finish();
            }
        }, 2000);
    }

}
