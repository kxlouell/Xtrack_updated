package com.example.xtrack.ui.home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.xtrack.MainActivity;
import com.example.xtrack.R;
import com.example.xtrack.Usersetup;
import com.example.xtrack.trackingphase;
import com.github.angads25.toggle.interfaces.OnToggledListener;
import com.github.angads25.toggle.model.ToggleableView;
import com.github.angads25.toggle.widget.LabeledSwitch;
import com.kusu.loadingbutton.LoadingButton;

import org.jetbrains.annotations.NotNull;

import pl.droidsonroids.gif.GifImageView;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class HomeFragment extends Fragment  {

    private homeFragmentListener listener;
    private GifImageView btn;
    LoadingButton btnOnOff;
    ImageButton userPic;
    WifiManager wifiManager;
    trackingphase trackingPhase;
    MainActivity mActivity;
    LabeledSwitch labeledSwitch;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public TextView connectionStatus, username;

    public HomeFragment(MainActivity mActivity, WifiManager wifiManager){
        this.mActivity = mActivity;
        this.wifiManager = wifiManager;
    }

    public interface homeFragmentListener{
        void onInputSent(CharSequence cSeq);
    }



    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initialWork(view);
        exqListener();
        return view;
    }

    public void connectionStatusText(CharSequence newText){
        connectionStatus.setText(newText);

    }

    @Override
    public void onAttach(@NonNull @NotNull Context context) {
        super.onAttach(context);
        if (context instanceof homeFragmentListener){
            listener = (homeFragmentListener) context;
        }else{
            throw new RuntimeException(context.toString()
                    + "must implement homeFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void exqListener() {
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnOnOff.showLoading();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                    startActivityForResult(panelIntent, 545);
                } else {
                    if (wifiManager.isWifiEnabled() == true) {
                        wifiManager.setWifiEnabled(false);
                    } else if (wifiManager.isWifiEnabled() == false) {
                        wifiManager.setWifiEnabled(true);
                    }
                }
                if (wifiManager.isWifiEnabled()) {
                    btnOnOff.setText("ON WIFI");
                    btnOnOff.hideLoading();
                } else {
                    btnOnOff.setText("OFF WIFI");
                    btnOnOff.hideLoading();
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opentrackingphase();
            }
        });

        labeledSwitch.setOnToggledListener(new OnToggledListener() {
            @Override
            public void onSwitched(ToggleableView toggleableView, boolean isOn) {
            }
        });

    }

    private void initialWork(View view) {
        btn =  view.findViewById(R.id.button1);
        connectionStatus = (TextView) view.findViewById(R.id.connectionStatus);
        btnOnOff = view.findViewById(R.id.loadingButton);
        labeledSwitch = view.findViewById(R.id.Status_Switch);
        userPic = view.findViewById(R.id.user_home);
        username = view.findViewById(R.id.user_name);
        sharedPref = mActivity.getSharedPreferences(
                getString(R.string.AVATAR), mActivity.MODE_PRIVATE);
        editor = sharedPref.edit();

        userPic.setImageResource(sharedPref.getInt("ICON",0));
        username.setText(sharedPref.getString("NAME",null));
        if (wifiManager.isWifiEnabled()) {
            btnOnOff.setText("OFF WIFI");
        } else {
            btnOnOff.setText("ON WIFI");
        }
    }

    private void opentrackingphase() {
        if(labeledSwitch.isOn()){
            editor.putString("USERTYPE", labeledSwitch.getLabelOn());
            editor.apply();
        }else {
            editor.putString("USERTYPE", labeledSwitch.getLabelOff());
            editor.apply();
        }
        trackingPhase = new trackingphase();
        Intent intent = new Intent(getContext(), trackingPhase.getClass());
        startActivity(intent);
    }
}