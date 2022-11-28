package com.estenancydev.estenancy.homeFragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.estenancydev.estenancy.Chat.MessageAdapterForPerson;
import com.estenancydev.estenancy.Chat.MessageList;
import com.estenancydev.estenancy.Chat.chat;
import com.example.estenancy.R;
import com.facebook.shimmer.ShimmerFrameLayout;
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

import java.util.ArrayList;


public class messageFragment extends Fragment {


    private String email, my_email;
    String lastMessage = "";
    private RecyclerView messagesRecyclerView;
    private FirebaseFirestore dbf;
    private FirebaseAuth mAuth;
    TextView no_msg;
    int notifCounter = 1;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    ArrayList<MessageList> messageLists;
    int counter;
    ShimmerFrameLayout shimmerFrameLayout;
    MessageAdapterForPerson messageAdapterForPerson;
   // NotificationManagerCompat notificationCompat;
    //Notification notification;

    int count1 = 0;

    FirebaseDatabase database = FirebaseDatabase.getInstance("https://estenancy-2eca5-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference db = database.getReference("chats");

    public messageFragment() {
        // Required empty public constructor
    }


    public static messageFragment newInstance(String param1, String param2) {
        messageFragment fragment = new messageFragment();
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
        View v = inflater.inflate(R.layout.fragment_message, container, false);

        messagesRecyclerView = v.findViewById(R.id.mgsPersonList);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        my_email = mAuth.getCurrentUser().getEmail().replace("@gmail.com", "");
        no_msg = v.findViewById(R.id.no_msg);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        dbf = FirebaseFirestore.getInstance();
        shimmerFrameLayout = v.findViewById(R.id.shimmer1);
        shimmerFrameLayout.startShimmer();
        messageLists = new ArrayList<>();

        getChats();

        return v;
    }

    public void pushNotif(String message) {
        /*
        notifCounter++;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("myCh", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getActivity().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), "myCh")
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Estenancy")
                .setContentText(message);

       // notification = builder.build();
        //notificationCompat = NotificationManagerCompat.from(getContext());

        //notificationCompat.notify(notifCounter, notification);

         */
    }


    public void gotoChat(String email) {
        counter = 0;
        chat chat = new chat();
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        chat.setArguments(bundle);
        FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
        transaction.replace(R.id.mainLayout, chat).addToBackStack("tag");
        transaction.commit();
    }

    public void getChats() {
        //get chat parent
        db.child(my_email)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            no_msg.setVisibility(View.GONE);
                            messagesRecyclerView.setVisibility(View.VISIBLE);
                            for (DataSnapshot ds : snapshot.getChildren()) {

                                String email = ds.getKey() + "@gmail.com";
                                //get last message.
                                db.child(my_email)
                                        .child(ds.getKey())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot ssnapshot) {
                                                db.child(my_email)
                                                        .child(ds.getKey())
                                                        .child(String.valueOf(ssnapshot.getChildrenCount()))
                                                        .child("msg").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DataSnapshot> tasks) {
                                                                //get name
                                                                dbf.collection("users")
                                                                        .document(email)
                                                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    DocumentSnapshot documentSnapshot = task.getResult();
                                                                                    if (documentSnapshot != null && documentSnapshot.exists()) {
                                                                                        //get image
                                                                                        storageReference
                                                                                                .child("profilePhoto/" + email).getDownloadUrl()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Uri> task) {
                                                                                                        shimmerFrameLayout.stopShimmer();
                                                                                                        shimmerFrameLayout.setVisibility(View.GONE);
                                                                                                        messageLists.add(new MessageList(
                                                                                                                documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName"),
                                                                                                                tasks.getResult().getValue().toString(),
                                                                                                                email.replace("@gmail.com", ""),
                                                                                                                task.getResult(), counter));
                                                                                                        messageAdapterForPerson = new MessageAdapterForPerson(messageLists, getContext(), new MessageAdapterForPerson.ItemClickListener() {
                                                                                                            @Override
                                                                                                            public void onItemClick(MessageList messageList) {
                                                                                                                gotoChat(messageList.getEmail());
                                                                                                            }
                                                                                                        });
                                                                                                        messagesRecyclerView.setAdapter(messageAdapterForPerson);
                                                                                                    }
                                                                                                }).addOnFailureListener(new OnFailureListener() {
                                                                                                    @Override
                                                                                                    public void onFailure(@NonNull Exception e) {
                                                                                                        storageReference = storage.getReference();
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            }
                                                                        });
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                            messageLists.clear();

                        } else {
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                            no_msg.setVisibility(View.VISIBLE);
                            messagesRecyclerView.setVisibility(View.GONE);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}