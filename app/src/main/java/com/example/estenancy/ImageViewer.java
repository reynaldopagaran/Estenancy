package com.example.estenancy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jsibbold.zoomage.ZoomageView;

import java.util.ArrayList;


public class ImageViewer extends Fragment {

    ArrayList<String> strings = new ArrayList<String>();
    ZoomageView zoomageView;
    LinearLayout linearLayout;

    public ImageViewer() {
        // Required empty public constructor
    }

    public static ImageViewer newInstance(String param1, String param2) {
        ImageViewer fragment = new ImageViewer();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_image_viewer, container, false);
       // zoomageView = v.findViewById(R.id.img);
        strings = ImageViewer.this.getArguments().getStringArrayList("uri");
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);

        for(int i = 0; i < strings.size(); i++){
            ZoomageView zoomageView = new ZoomageView(getContext());
            Glide.with(this)
                    .load(strings.get(i))
                    .into(zoomageView);
            layout.addView(zoomageView);
        }

        return v;
    }


}