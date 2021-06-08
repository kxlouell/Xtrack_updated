    package com.example.xtrack;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class avatarselection  extends AppCompatActivity {
     String[] text = {"Jim","Mingurii","CryptoGong","Dowps","Yejiena"};
     int[] picture = new int[]{R.drawable.avatar_1, R.drawable.avatar_2, R.drawable.avatar_3, R.drawable.avatar_4,R.drawable.avatar_5};
    GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avatar_selection);
        gridView = findViewById(R.id.gridview);
        CustomAdapter customAdapter = new CustomAdapter(text,picture,this);
        gridView.setAdapter(customAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=getIntent();
                Bundle bundle=new Bundle();
                bundle.putInt("image",picture[position]);
                intent.putExtras(bundle);
                setResult(0x11,intent);
                finish();

            }
        });

    }

    public class CustomAdapter extends BaseAdapter {
        private String[] avatarname;
        private int[] avatars;
        private Context context;
        private LayoutInflater layoutInflater;


        public CustomAdapter(String[] avatarname, int[] avatars, Context context) {
            this.avatarname = avatarname;
            this.avatars = avatars;
            this.context = context;
            this.layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return avatars.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = layoutInflater.inflate(R.layout.row_items, viewGroup, false);

            }

            TextView textView = view.findViewById(R.id.textava);
            ImageView imageView = view.findViewById(R.id.ava1);

            textView.setText(avatarname[i]);
            imageView.setImageResource(avatars[i]);

            return view;
        }

    }
}
