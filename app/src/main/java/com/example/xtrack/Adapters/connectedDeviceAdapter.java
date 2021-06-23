package com.example.xtrack.Adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.xtrack.R;

import java.util.ArrayList;

public class connectedDeviceAdapter extends ArrayAdapter<ConnectedDevice> {
private Context context;
private int resource;

public connectedDeviceAdapter(@NonNull Context context, int resource, @NonNull ArrayList<ConnectedDevice> objects){
super(context, resource, objects);
this.context = context;
this.resource = resource;
}

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        convertView = layoutInflater.inflate(resource, parent, false);

        ImageView userICON = convertView.findViewById(R.id.icon_connected);
        TextView userNAME = convertView.findViewById(R.id.user_connected);
        TextView status = convertView.findViewById(R.id.status_connected);

        userICON.setImageResource(getItem(position).getICON());
        userNAME.setText(getItem(position).getDeviceName());
        status.setText(getItem(position).getDeviceStatus());
        return convertView;
    }
}
