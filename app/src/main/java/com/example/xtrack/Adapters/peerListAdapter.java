package com.example.xtrack.Adapters;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.xtrack.R;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class peerListAdapter extends ArrayAdapter<String> {

    Context context;
    String[] deviceName;
    int[] image;

    public peerListAdapter(Context context,int[] image, String[] deviceName){
        super(context, R.layout.row_items, R.id.peerText, deviceName);
        this.context = context;
        this.deviceName = deviceName;
        this.image = image;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = layoutInflater.inflate(R.layout.peers_row, parent, false);
        ImageView imageView = row.findViewById(R.id.peerImage);
        TextView device = row.findViewById(R.id.peerText);
        imageView.setImageResource(image[position]);
        device.setText(deviceName[position]);

        return row;
    }
}
