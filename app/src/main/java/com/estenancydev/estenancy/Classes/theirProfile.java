package com.estenancydev.estenancy.Classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estenancy.R;

import java.util.List;

public class theirProfile extends RecyclerView.Adapter<theirProfile.ViewHolder> {

    Context context;
    List<my_listing_get_post> my_listing_array;
    private theirProfile.ItemClickListener itemClickListener;

    public theirProfile(Context context, List<my_listing_get_post> my_listing_array, theirProfile.ItemClickListener itemClickListener) {
        this.context = context;
        this.my_listing_array = my_listing_array;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public theirProfile.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_listing_card, parent, false);
        return new theirProfile.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull theirProfile.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.myTitle.setText(my_listing_array.get(position).getMy_title());
        holder.timeStamp.setText(my_listing_array.get(position).getTimeStamp());
        holder.myThumbnail.setImageBitmap(my_listing_array.get(position).getMy_thumbnail());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onItemClick(my_listing_array.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return my_listing_array.size();
    }

    public interface ItemClickListener {
        void onItemClick(my_listing_get_post my_listing_get_post);
    }


    public class ViewHolder extends RecyclerView.ViewHolder {


        TextView myTitle, timeStamp;
        ImageView myThumbnail;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            myTitle = itemView.findViewById(R.id.my_title_card);
            myThumbnail = itemView.findViewById(R.id.my_thumbnail);
            timeStamp = itemView.findViewById(R.id.timeStamp);
        }
    }
}
