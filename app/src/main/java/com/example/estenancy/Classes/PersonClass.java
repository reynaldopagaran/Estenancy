package com.example.estenancy.Classes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.estenancy.Chat.chat;
import com.example.estenancy.R;
import com.example.estenancy.ViewProfile;
import com.example.estenancy.completedBooking;
import com.example.estenancy.currentBooking;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonClass extends RecyclerView.Adapter<PersonClass.ViewHolder> {

    private Context mContext;
    private ArrayList<Person> names;
    LayoutInflater layoutInflater;
    private FirebaseFirestore db;
    private PersonClass.ItemClickListener itemClickListener;
    String id;
    Spinner date;
    Button getAppoint;

    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "mypref";

    public PersonClass(Context mContext, ArrayList<Person> names, Spinner date, Button getAppoint, PersonClass.ItemClickListener itemClickListener) {
        this.mContext = mContext;
        this.names = names;
        this.date = date;
        this.getAppoint = getAppoint;
        this.itemClickListener = itemClickListener;

        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        sharedPreferences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
        id = sharedPreferences.getString("myId", null);

        Glide.with(mContext)
                .load(names.get(position).getImage())
                .into(holder.image);

        holder.name.setText(names.get(position).getName());
        holder.email.setText(names.get(position).getEmail());


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                PopupMenu popupMenu = new PopupMenu(mContext, v, Gravity.RIGHT);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.appointment_menu, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if (item.getItemId() == R.id.remove_appointment) {

                            DocumentReference docRef =    db.collection("appointmentOnPost")
                                    .document(id)
                                    .collection("booked")
                                    .document(date.getSelectedItem().toString());

                            Map<String, Object> updates = new HashMap<>();
                            updates.put(names.get(position).getEmail().replace("@gmail.com", ""), FieldValue.delete());

                            docRef.update(updates);

                            Toast.makeText(mContext, "Appointment removed.", Toast.LENGTH_SHORT).show();
                            getAppoint.performClick();

                        } else if (item.getItemId() == R.id.finished_appointment) {

                            //add to finished
                            Map<String, Object> data = new HashMap<>();
                            data.put(names.get(position).getEmail().replace("@gmail.com", ""), names.get(position).getName());
                            db.collection("appointmentOnPost")
                                    .document(id)
                                    .collection("finished")
                                    .document(date.getSelectedItem().toString())
                                    .set(data, SetOptions.merge());

                            //remove from booked
                            DocumentReference docRef = db.collection("appointmentOnPost")
                                    .document(id)
                                    .collection("booked")
                                    .document(date.getSelectedItem().toString());

                            Map<String, Object> updates = new HashMap<>();
                            updates.put(names.get(position).getEmail().replace("@gmail.com", ""), FieldValue.delete());
                            docRef.update(updates);



                            Toast.makeText(mContext, "Moved to finished",
                                    Toast.LENGTH_SHORT).show();
                            getAppoint.performClick();

                        } else if (item.getItemId() == R.id.view_profilex) {

                            ViewProfile viewProfile = new ViewProfile();
                            Bundle bundle = new Bundle();
                            bundle.putString("email", names.get(position).getEmail());
                            viewProfile.setArguments(bundle);
                            FragmentTransaction transaction = ((AppCompatActivity)mContext).getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);;
                            transaction.replace(R.id.mainLayout, viewProfile).addToBackStack("tag");
                            transaction.commit();

                        } else if (item.getItemId() == R.id.messagex) {
                            chat chat = new chat();
                            Bundle bundle = new Bundle();
                            bundle.putString("email", names.get(position).getEmail().replace("@gmail.com", ""));
                            chat.setArguments(bundle);
                            FragmentTransaction transaction = ((AppCompatActivity)mContext).getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                            transaction.replace(R.id.mainLayout, chat).addToBackStack("tag");
                            transaction.commit();
                        }

                        return true;
                    }
                });


                return true;
            }
        });

    }

    public interface ItemClickListener {
        void onItemClick(Person Person);

    }


    @Override
    public int getItemCount() {
        return names.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView image;
        TextView name;
        TextView email;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.image_list);
            name = itemView.findViewById(R.id.name_list);
            email = itemView.findViewById(R.id.email_list);

        }
    }
}
