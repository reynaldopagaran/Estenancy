package com.estenancydev.estenancy.Chat;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.estenancy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class chat extends Fragment {

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://estenancy-2eca5-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference db = database.getReference("chats");

    private FirebaseAuth mAuth;
    String email, my_email;
    int count = 0;
    EditText message;
    ImageView sends;
    RecyclerView chatRecycler;
    TextView chatName;
    CircleImageView chatPic;
    private FirebaseFirestore dbf;
    String name, finalName;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    List<chatClass> chat;
    chatAdapter chatAdapter;

    int notifCounter = 0;

    int unread_me = 0, unread_them = 0;

    public chat() {
        // Required empty public constructor
    }


    public static chat newInstance(String param1, String param2) {
        chat fragment = new chat();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat, container, false);

        email = chat.this.getArguments().getString("email");

        message = v.findViewById(R.id.msg);
        sends = v.findViewById(R.id.send);
        chatRecycler = v.findViewById(R.id.chat_recycler);
        chatRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        chatName = v.findViewById(R.id.chat_name);
        chatPic = v.findViewById(R.id.chat_pic);
        dbf = FirebaseFirestore.getInstance();
        chat = new ArrayList<>();

        //firebase
        mAuth = FirebaseAuth.getInstance();
        my_email = mAuth.getCurrentUser().getEmail().replace("@gmail.com", "");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        //Notif


        //method calls
        chatSend();
        getNames();
        setPhoto();
        processChatBubbles();
        return v;
    }


    public void processChatBubbles() {

        db.child(my_email)
                .child(email)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            chat.clear();
                            for (int i = 1; i < snapshot.getChildrenCount() + 1; i++) {
                                String message = snapshot.child(String.valueOf(i)).child("msg").getValue(String.class);
                                int receiver = snapshot.child(String.valueOf(i)).child("receiver").getValue(Integer.class);
                                int sender = snapshot.child(String.valueOf(i)).child("sender").getValue(Integer.class);
                                String timeStamp = snapshot.child(String.valueOf(i)).child("timeStamp").getValue(String.class);

                                chat.add(new chatClass(message, timeStamp, receiver, sender));

                                Log.d("tag", "rey :" + message + receiver + sender + timeStamp);
                            }
                            chatAdapter = new chatAdapter(chat, getContext());
                            chatAdapter.notifyDataSetChanged();
                            chatRecycler.setAdapter(chatAdapter);
                            chatRecycler.scrollToPosition(Math.toIntExact(new Long(snapshot.getChildrenCount())) - 1);

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    public void setPhoto() {
        storageReference
                .child("profilePhoto/" + email + "@gmail.com").getDownloadUrl()
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        Glide.with(getContext())
                                .load(task.getResult())
                                .into(chatPic);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        storageReference = storage.getReference();
                    }
                });
        ;
    }

    public void getNames() {

        dbf.collection("users")
                .document(email + "@gmail.com")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                chatName.setText(documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName"));
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    public void chatSend() {

        sends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get count

                if (TextUtils.isEmpty(message.getText())) {

                } else {
                    db.child(my_email)
                            .child(email)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    if (snapshot.exists()) {
                                        count = (int) snapshot.getChildrenCount() + 1;
                                        thisChat();
                                    } else {
                                        count = 1;
                                        thisChat();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }

            }
        });
    }

    public void thisChat() {
        //get unread


        //get Datetime
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a");
        String formattedDate = fmt.format(c);

        Map<String, Object> myMessage = new HashMap<>();
        myMessage.put("msg", message.getText().toString());
        myMessage.put("receiver", 1);
        myMessage.put("sender", 0);
        myMessage.put("timeStamp", formattedDate);

        Map<String, Object> myMessage1 = new HashMap<>();
        myMessage1.put("msg", message.getText().toString());
        myMessage1.put("receiver", 0);
        myMessage1.put("sender", 1);
        myMessage1.put("timeStamp", formattedDate);

        db.child(my_email)
                .child(email)
                .child(String.valueOf(count))
                .setValue(myMessage);

        db.child(email)
                .child(my_email)
                .child(String.valueOf(count))
                .setValue(myMessage1);




        message.setText("");

    }




}