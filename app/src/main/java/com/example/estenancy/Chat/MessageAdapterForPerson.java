package com.example.estenancy.Chat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.estenancy.Adapters.PostImagesAdapter;
import com.example.estenancy.Classes.post_model_getPosts;
import com.example.estenancy.Classes.post_model_recyclerView;
import com.example.estenancy.R;
import com.example.estenancy.ViewProfile;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapterForPerson extends RecyclerView.Adapter<MessageAdapterForPerson.MyViewHolder> {

    List<MessageList> messageLists;
    Context context;

   private MessageAdapterForPerson.ItemClickListener itemClickListener;

    public MessageAdapterForPerson(List<MessageList> messageLists, Context context , MessageAdapterForPerson.ItemClickListener itemClickListener) {
        this.messageLists = messageLists;
        this.context = context;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_adapter_layout,parent,false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

        Glide.with(context)
                .load(messageLists.get(position).getProfilePic())
                .into(holder.circleImageView);

        holder.name.setText(messageLists.get(position).getName());
        holder.lastMsg.setText(messageLists.get(position).getLastMessage());
        holder.unseen.setText(String.valueOf(messageLists.get(position).getUnseenMsg()));

        if(messageLists.get(position).getUnseenMsg() == 0){
            holder.unseen.setVisibility(View.GONE);
        }else {
            holder.unseen.setVisibility(View.VISIBLE);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onItemClick(messageLists.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageLists.size();
    }

    public interface ItemClickListener{
        void onItemClick(MessageList messageList);

    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        CircleImageView circleImageView;
        TextView name;
        TextView lastMsg;
        TextView unseen;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.profilePic);
            name = itemView.findViewById(R.id.nameMessage);
            lastMsg = itemView.findViewById(R.id.lastMsg);
            unseen = itemView.findViewById(R.id.unseen);
        }
    }
}
