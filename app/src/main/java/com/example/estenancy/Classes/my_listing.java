package com.example.estenancy.Classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estenancy.R;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class my_listing extends RecyclerView.Adapter<my_listing.ViewHolder> {

    Context context;
    List<my_listing_get_post> my_listing_array;

    public my_listing(Context context, List<my_listing_get_post> my_listing_array) {
        this.context = context;
        this.my_listing_array = my_listing_array;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_listing_card,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.myTitle.setText(my_listing_array.get(position).getMy_title());
        holder.timeStamp.setText(my_listing_array.get(position).getTimeStamp());
        holder.myThumbnail.setImageBitmap(my_listing_array.get(position).getMy_thumbnail());
    }

    @Override
    public int getItemCount() {
        return my_listing_array.size();
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
