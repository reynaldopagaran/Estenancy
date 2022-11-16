package com.example.estenancy.Chat;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estenancy.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;


public class chatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    List<chatClass> chat;
    Context context;
    public static final int SENDER = 0;
    public static final int RECIPIENT = 1;

    public chatAdapter(List<chatClass> chat, Context context) {
        this.chat = chat;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (chat.get(position).getSender() == SENDER) {
            return SENDER;
        } else {
            return RECIPIENT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case SENDER:
                View viewSender = inflater.inflate(R.layout.message_design_sender, parent, false);
                viewHolder = new ViewHolderSender(viewSender);
                break;
            case RECIPIENT:
                View viewRecipient = inflater.inflate(R.layout.message_design_receive, parent, false);
                viewHolder = new ViewHolderRecipient(viewRecipient);
                break;
            default:
                View viewSenderDefault = inflater.inflate(R.layout.message_design_sender, parent, false);
                viewHolder = new ViewHolderSender(viewSenderDefault);
                break;
        }
        return viewHolder;

        //View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_design_sender,parent,false);
        //return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case SENDER:
                ViewHolderSender viewHolderSender = (ViewHolderSender) holder;
                configureSenderView(viewHolderSender, position);
                break;
            case RECIPIENT:
                ViewHolderRecipient viewHolderRecipient = (ViewHolderRecipient) holder;
                configureRecipientView(viewHolderRecipient, position);
                break;
        }
    }

    private void configureSenderView(ViewHolderSender viewHolderSender, int position) {
        viewHolderSender.textMessage.setText(chat.get(position).getMessage());
        viewHolderSender.time.setText(chat.get(position).getTime());
    }

    private void configureRecipientView(ViewHolderRecipient viewHolderRecipient, int position) {

        viewHolderRecipient.textMessageR.setText(chat.get(position).getMessage());
        viewHolderRecipient.timeR.setText(chat.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return chat.size();
    }


    public class ViewHolderSender extends RecyclerView.ViewHolder  {

        TextView textMessage;
        TextView time;

        public ViewHolderSender(View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessageS);
            time = itemView.findViewById(R.id.dateTimeS);
        }
    }


    /*ViewHolder for Recipient*/
    public class ViewHolderRecipient extends RecyclerView.ViewHolder {

        TextView textMessageR;
        TextView timeR;
        public ViewHolderRecipient(View itemView) {
            super(itemView);
            textMessageR = itemView.findViewById(R.id.textMessageR);
            timeR = itemView.findViewById(R.id.dateTimeR);
        }

    }
}
