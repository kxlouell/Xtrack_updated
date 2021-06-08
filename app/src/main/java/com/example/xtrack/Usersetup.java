package com.example.xtrack;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class Usersetup extends AppCompatActivity {

    int ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usersetup);
        ImageButton buttonava = findViewById(R.id.avatar);
        Button buttonus = findViewById(R.id.buttonusersu);
        Dialog avatarsel = new Dialog(this);

        //button
        buttonus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPref = Usersetup.this.getSharedPreferences(
                        getString(R.string.AVATAR), Usersetup.this.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("ICON",ID);
                editor.apply();
                openmainact();
            }
        });
        buttonava.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Usersetup.this, avatarselection.class);
                startActivityForResult(intent, 0x11);
            }
        });

    }

    private void openmainact() {
        Intent intent = new Intent(getApplication(),MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0x11 && resultCode == 0x11) {
            ID = data.getExtras().getInt("image");
            ImageButton imageView = (ImageButton) findViewById(R.id.avatar);
            imageView.setImageResource(ID);
        }


    }
}