package com.example.estenancy.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.estenancy.R;

import java.util.ArrayList;
import java.util.Objects;

public class ImagesAdapter extends PagerAdapter {

    Context context;
    ArrayList<Uri> imageUris;
    LayoutInflater layoutInflater;

    public ImagesAdapter(Context context, ArrayList<Uri> imagesUris){
        this.context = context;
        this.imageUris = imagesUris;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return imageUris.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View v = layoutInflater.inflate(R.layout.images_single, container, false);
        ImageView imageView = (ImageView) v.findViewById(R.id.imageView);
        imageView.setImageURI(imageUris.get(position));
        Objects.requireNonNull(container).addView(v);
        return v;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout) object);
    }
}
