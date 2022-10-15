package com.example.estenancy.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.estenancy.R;
import com.example.estenancy.createPost;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImagesAdapter extends PagerAdapter {

    Context context;
    ArrayList<Uri> imageUris;
    LayoutInflater layoutInflater;
    public ImageView imageView;
    public TextView cat;
    private List<String> categories;
    ViewPager viewPager;

    public ImagesAdapter(Context context, ArrayList<Uri> imagesUris, List<String> categories, ViewPager viewPager){
        this.context = context;
        this.imageUris = imagesUris;
        this.categories = categories;
        this.viewPager = viewPager;
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
        imageView = (ImageView) v.findViewById(R.id.imageView);
        cat = v.findViewById(R.id.cat);
        cat.setText(categories.get(position));
        imageView.setImageURI(imageUris.get(position));
        Objects.requireNonNull(container).addView(v);

        imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Remove Image");
                builder.setMessage("Are you sure to remove selected image?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        imageUris.remove(position);
                        categories.remove(position);
                        Toast.makeText(context, "Removed.",
                                Toast.LENGTH_SHORT).show();

                        notifyDataSetChanged();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                return false;
            }
        });

        return v;
    }

    @Override
    public int getItemPosition(@NotNull Object object) {
        return POSITION_NONE;
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
