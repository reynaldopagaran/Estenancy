package com.example.estenancy.Classes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
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
    public void onBindViewHolder(@NonNull ViewHolder holder,int position) {

        holder.title.setText(array_getPosts.get(position).getTitle());
        holder.name.setText(array_getPosts.get(position).getName());
        holder.timeStampx.setText(array_getPosts.get(position).getTimeStampx());
        holder.profile_post.setImageBitmap(array_getPosts.get(position).getProfilePic());
        holder.thumbnail.setImageBitmap(array_getPosts.get(position).getThumbnail());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onItemClick(array_getPosts.get(position));
            }
        });


        holder.book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        TextView name, title, timeStampx;
        ImageView thumbnail;
        Button book, msg, map;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_post = itemView.findViewById(R.id.card_profile);
            name = itemView.findViewById(R.id.name);
            timeStampx = itemView.findViewById(R.id.timeStampx);
            title = itemView.findViewById(R.id.title_card);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            book = itemView.findViewById(R.id.book_card);
            msg = itemView.findViewById(R.id.msg_card);
            map = itemView.findViewById(R.id.map_card);


            //remove comment on method call if you want animation on cards
            //animation(itemView);
        }
    }

    public void animation(View view){
        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
        view.setAnimation(animation);
    }
}
