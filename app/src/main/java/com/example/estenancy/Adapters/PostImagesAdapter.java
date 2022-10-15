package com.example.estenancy.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.estenancy.R;
import com.jsibbold.zoomage.ZoomageView;
import java.util.ArrayList;

public class PostImagesAdapter extends RecyclerView.Adapter<PostImagesAdapter.ViewHolder> {

    Context context;
    ArrayList<String> imageUris;
    ArrayList<String> names;
    LayoutInflater layoutInflater;

    public PostImagesAdapter(Context context, ArrayList<String> imagesUris, ArrayList<String> names) {
        this.context = context;
        this.imageUris = imagesUris;
        this.names = names;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.zoomage_view,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Glide.with(context)
                .load(imageUris.get(position))
                .into(holder.zoomageView);

        holder.cat.setText(names.get(position));
    }

    @Override
    public int getItemCount() {
        return imageUris.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ZoomageView zoomageView;
        TextView cat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            zoomageView = itemView.findViewById(R.id.myZoomageView);
            cat = itemView.findViewById(R.id.cat_post);

        }
    }
}
