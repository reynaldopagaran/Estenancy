package com.example.estenancy;

import android.media.Image;
import android.net.Uri;
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
import com.example.estenancy.Adapters.ImagesAdapter;
import com.example.estenancy.Adapters.PostImagesAdapter;
import com.jsibbold.zoomage.ZoomageView;

import java.util.ArrayList;


public class ImageViewer extends Fragment {

    ViewPager2 viewPager2;
    private ArrayList<String> imagesUri;

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
        viewPager2 = v.findViewById(R.id.postViewPager);
        imagesUri = new ArrayList<>();
        imagesUri =  ImageViewer.this.getArguments().getStringArrayList("uri");
        PostImagesAdapter postImagesAdapter = new PostImagesAdapter(getContext(), imagesUri);
        viewPager2.setAdapter(postImagesAdapter);

        return v;
    }


}