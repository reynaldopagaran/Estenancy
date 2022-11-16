package com.example.estenancy.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

import com.bumptech.glide.Glide;
import com.example.estenancy.R;
import com.example.estenancy.createPost;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImagesAdapter extends PagerAdapter {

    Context context;
    public ArrayList<Uri> imageUris;
    private List<String> uriId;
    private List<String> removedId;
    LayoutInflater layoutInflater;
    public ImageView imageView;
    public TextView cat;
    private List<String> categories;
    ViewPager viewPager;
    private StorageReference storageReference;
    private FirebaseStorage storage;
    String id;
    String title;


    public ImagesAdapter(Context context, String title, ArrayList<Uri> imageUris, List<String> categories, ViewPager viewPager, String id, List<String> uriId, List<String> removedId) {
        this.context = context;
        this.imageUris = imageUris;
        this.categories = categories;
        this.viewPager = viewPager;
        this.id = id;
        this.uriId = uriId;
        this.removedId = removedId;
        this.title = title;
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

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        cat = v.findViewById(R.id.cat);
        cat.setText(categories.get(position));
        // imageView.setImageURI(imageUris.get(position));
        Glide.with(context)
                .load(imageUris.get(position))
                .into(imageView);
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

                        if(title.equals("Edit Post")){
                            removedId.add(uriId.get(position));
                            uriId.remove(position);
                        }
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
