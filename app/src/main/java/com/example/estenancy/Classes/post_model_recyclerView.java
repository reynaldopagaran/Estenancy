package com.example.estenancy.Classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.estenancy.R;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class post_model_recyclerView extends RecyclerView.Adapter<post_model_recyclerView.ViewHolder> {

    Context context;
    private List<post_model_getPosts> array_getPosts;
    private ItemClickListener itemClickListener;

    public post_model_recyclerView(Context context, List<post_model_getPosts> array_getPosts, ItemClickListener itemClickListener) {
        this.context = context;
        this.array_getPosts = array_getPosts;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        holder.title.setText(array_getPosts.get(position).getTitle());
        holder.stat.setText(array_getPosts.get(position).getStat());
        holder.name.setText(array_getPosts.get(position).getName());
        holder.timeStampx.setText(array_getPosts.get(position).getTimeStampx());
        holder.profile_post.setImageBitmap(array_getPosts.get(position).getProfilePic());
        holder.thumbnail.setImageBitmap(array_getPosts.get(position).getThumbnail());
        holder.descr.setText(array_getPosts.get(position).getDesc());

        if(array_getPosts.get(position).getStat().equals("Available")){
            holder.stat.setTextColor(Color.parseColor("#01DA15"));

        }else{
            holder.stat.setTextColor(Color.parseColor("#FD0000"));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onItemClick(array_getPosts.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return array_getPosts.size();
    }

    public interface ItemClickListener{
        void onItemClick(post_model_getPosts post_model_getPosts);

    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profile_post;
        TextView name, title, timeStampx,stat, descr;
        ImageView thumbnail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stat = itemView.findViewById(R.id.stat);
            profile_post = itemView.findViewById(R.id.card_profile);
            name = itemView.findViewById(R.id.name);
            timeStampx = itemView.findViewById(R.id.timeStampx);
            title = itemView.findViewById(R.id.title_card);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            descr = itemView.findViewById(R.id.desc_post);


            //remove comment on method call if you want animation on cards
            //animation(itemView);
        }
    }

    public void animation(View view){
        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        view.setAnimation(animation);
    }
}
