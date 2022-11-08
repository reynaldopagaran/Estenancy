package com.example.estenancy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.estenancy.Classes.my_listing;
import com.example.estenancy.Classes.my_listing_get_post;
import com.example.estenancy.Classes.theirProfile;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ViewProfile extends Fragment {

    RecyclerView vp_recyclerview;
    CircleImageView vp_image;
    TextView vp_name;
    String vp_email;
    ShimmerFrameLayout shimmerFrameLayout;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    List<my_listing_get_post> vp_myListing;


    public ViewProfile() {
        // Required empty public constructor
    }


    public static ViewProfile newInstance(String param1, String param2) {
        ViewProfile fragment = new ViewProfile();
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
        View v = inflater.inflate(R.layout.fragment_view_profile, container, false);

        vp_email = ViewProfile.this.getArguments().getString("email");

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        vp_myListing = new ArrayList<>();
        vp_image = v.findViewById(R.id.vp_image);
        vp_name = v.findViewById(R.id.vp_name);

        shimmerFrameLayout = v.findViewById(R.id.vp_shimmer);
        vp_recyclerview = v.findViewById(R.id.vp_recyclerView);

        vp_recyclerview.setLayoutManager(new LinearLayoutManager(getContext()));


        shimmerFrameLayout.startShimmer();
        //method calls
        vp_setNamePhoto();
        setProfilePhoto();
        vp_showMyListing();
        vp_image.getDrawable();

        return v;
    }

    private void showPost(String id, String email){
        Post post = new Post();
        Bundle bundle = new Bundle();
        bundle.putString("id_from_card", id);
        bundle.putString("email", email);
        post.setArguments(bundle);
        FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
        transaction.replace(R.id.mainLayout, post).addToBackStack("tag");
        transaction.commit();
    }

    public void vp_setNamePhoto(){
        //set photo

        storageReference = FirebaseStorage.getInstance().getReference().child("profilePhoto/"+vp_email);
        try{
            final File localFile = File.createTempFile(mAuth.getCurrentUser().getEmail(), "jpg");
            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            vp_image.setImageBitmap(bitmap);
                            storageReference = storage.getReference();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            storageReference = storage.getReference();
                        }
                    });
        }catch (Exception e){

        }

        db.collection("users")
                .document(vp_email)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot != null && documentSnapshot.exists()){
                                vp_name.setText(documentSnapshot.getString("firstName") +" "+ documentSnapshot.getString("lastName"));
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }


    public void setProfilePhoto() {
        vp_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Profile Photo");
                ImageView image = new ImageView(getActivity());
                image.setImageDrawable(vp_image.getDrawable());
                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.setView(image);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                alertDialog.getWindow().setLayout(1000, 1000);
            }
        });
    }

    public void vp_showMyListing(){

        db.collection("posts")
                .whereEqualTo("email", vp_email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String title = document.getString("title_post");
                                String id = document.getString("id");
                                String email = document.getString("email");
                                Timestamp timeStampFire = document.getTimestamp("timeStamp");
                                Date date = timeStampFire.toDate();
                                String timeStamp  = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
                                //start of my thumbnail
                                storageReference = FirebaseStorage.getInstance().getReference().child("posts/"+id+"/site_image_0");
                                try{
                                    final File localFile = File.createTempFile("site_image_0                                                                                           ", "jpg");
                                    storageReference.getFile(localFile)
                                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    shimmerFrameLayout.stopShimmer();
                                                    shimmerFrameLayout.setVisibility(View.GONE);
                                                    Bitmap thumbnail = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                                    vp_myListing.add(new my_listing_get_post(title,timeStamp, thumbnail, id, email));
                                                    storageReference = storage.getReference();

                                                    theirProfile theirProfile = new theirProfile(getContext(), vp_myListing, new theirProfile.ItemClickListener(){
                                                        @Override
                                                        public void onItemClick(my_listing_get_post my_listing_get_post) {
                                                            showPost(my_listing_get_post.getId(), my_listing_get_post.getEmail());
                                                        }
                                                    });


                                                    vp_recyclerview.setAdapter(theirProfile);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    storageReference = storage.getReference();
                                                }
                                            });
                                }catch (Exception e){

                                }
                                //end of my thumbnail;

                            }
                        } else {

                        }
                    }
                });
    }
}