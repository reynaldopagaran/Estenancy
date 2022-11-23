package com.example.estenancy.Classes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estenancy.Appointments;
import com.example.estenancy.R;
import com.example.estenancy.createPost;
import com.example.estenancy.currentBooking;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.checkerframework.checker.units.qual.A;

import java.util.List;


public class my_listing extends RecyclerView.Adapter<my_listing.ViewHolder> {

    Context context;
    List<my_listing_get_post> my_listing_array;
    private ItemClickListener itemClickListener;
    private FirebaseFirestore db;
    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "mypref";
    private FirebaseStorage storage;
    private StorageReference storageReference;


    public my_listing(Context context, List<my_listing_get_post> my_listing_array, ItemClickListener itemClickListener) {
        this.context = context;
        this.my_listing_array = my_listing_array;
        this.itemClickListener = itemClickListener;

        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_listing_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        holder.myTitle.setText(my_listing_array.get(position).getMy_title());
        holder.timeStamp.setText(my_listing_array.get(position).getTimeStamp());
        holder.myThumbnail.setImageBitmap(my_listing_array.get(position).getMy_thumbnail());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickListener.onItemClick(my_listing_array.get(position));
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, v, Gravity.RIGHT);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.menu, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getItemId() == R.id.edit) {

                            try{

                                createPost createPost = new createPost();
                                Bundle bundle = new Bundle();
                                bundle.putString("id", my_listing_array.get(position).getId());
                                bundle.putString("label", "Edit Post");
                                createPost.setArguments(bundle);

                                FragmentTransaction fragmentTransaction = ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);;
                                fragmentTransaction.replace(R.id.mainLayout, createPost).addToBackStack("tags");
                                fragmentTransaction.commit();

                            }catch (Exception e){

                            }

                        } else if (item.getItemId() == R.id.delete) {

                            new AlertDialog.Builder(context)
                                    .setTitle("Delete")
                                    .setMessage("Are you sure you want to delete this post?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                            db.collection("posts").document(my_listing_array.get(position).getId())
                                                    .delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {

                                                            db.collection("categories")
                                                                            .document(my_listing_array.get(position).getId())
                                                                                    .delete();

                                                            db.collection("appointmentOnPost")
                                                                    .document(my_listing_array.get(position).getId())
                                                                    .delete();

                                                            storageReference.child("posts/"+my_listing_array.get(position).getId())
                                                                            .delete();

                                                            Toast.makeText(context, "Deleted successfully.",
                                                                    Toast.LENGTH_LONG).show();
                                                            notifyDataSetChanged();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(context, "Delete failed.",
                                                                    Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Toast.makeText(context, "Cancelled.",
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    })
                                    .show();
                        } else if(item.getItemId() == R.id.view_appointments){
                            Appointments appointments = new Appointments();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("myId", my_listing_array.get(position).getId());
                            editor.commit();

                            FragmentTransaction fragmentTransaction = ((AppCompatActivity)context).getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);;
                            fragmentTransaction.replace(R.id.mainLayout, appointments).addToBackStack("tags");
                            fragmentTransaction.commit();
                        }else{
                            return false;
                        }
                        return true;
                    }
                });

                return true;
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
