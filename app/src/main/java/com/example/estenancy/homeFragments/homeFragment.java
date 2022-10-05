package com.example.estenancy.homeFragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Toast;

import com.example.estenancy.Classes.post_model_getPosts;
import com.example.estenancy.Classes.post_model_recyclerView;
import com.example.estenancy.Home;
import com.example.estenancy.Post;
import com.example.estenancy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class homeFragment extends Fragment {

    RecyclerView recyclerView;
    List<post_model_getPosts> array_getPosts;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser firebaseUser;
    View v;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_home2, container, false);

        array_getPosts = new ArrayList<>();

        //firebase init
        //firebase init
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firebaseUser = mAuth.getCurrentUser();

        swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);


        recyclerView = v.findViewById(R.id.recyclerViewx);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //method calls
        getPosts();
        refreshFeed();
        return v;
    }

    public void refreshFeed(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                Home homeFragment = new Home();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.mainLayout,homeFragment);
                transaction.commit();
            }
        });
    }



    public void getPosts(){
        db.collection("posts").orderBy("timeStamp", Query.Direction.ASCENDING)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                               String title = document.getString("title_post");
                               String email = document.getString("email");
                               String id = document.getString("id");
                                Timestamp timeStampFire = document.getTimestamp("timeStamp");
                                Date date = timeStampFire.toDate();
                                String timeStamp  = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
                                //start of name
                                db.collection("users")
                                        .document(email)
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()){
                                                    DocumentSnapshot documentSnapshot = task.getResult();
                                                    if(documentSnapshot != null && documentSnapshot.exists()){
                                                      String  name = documentSnapshot.getString("firstName") +" "+ documentSnapshot.getString("lastName");

                                                      //start of profile pic
                                                        storageReference = FirebaseStorage.getInstance().getReference().child("profilePhoto/"+email);
                                                        try{
                                                            final File localFile = File.createTempFile(email, "jpg");
                                                            storageReference.getFile(localFile)
                                                                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                            Bitmap dp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                                                            storageReference = storage.getReference();
                                                                            //start of thumbnail
                                                                            storageReference = FirebaseStorage.getInstance().getReference().child("posts/"+id+"/site_image_0");
                                                                            try{
                                                                                final File localFile = File.createTempFile("site_image_0                                                                                           ", "jpg");
                                                                                storageReference.getFile(localFile)
                                                                                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                                                            @Override
                                                                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                                                Bitmap thumbnail = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                                                                                array_getPosts.add(new post_model_getPosts(title, name, dp, thumbnail, timeStamp, id, email));
                                                                                                storageReference = storage.getReference();
                                                                                                post_model_recyclerView post_model_recyclerView = new post_model_recyclerView(getContext(), array_getPosts, new post_model_recyclerView.ItemClickListener() {
                                                                                                    @Override
                                                                                                    public void onItemClick(post_model_getPosts post_model_getPosts) {
                                                                                                            showPost(post_model_getPosts.getId(), post_model_getPosts.getEmail());
                                                                                                    }
                                                                                                });
                                                                                                recyclerView.setAdapter(post_model_recyclerView);
                                                                                            }
                                                                                        }).addOnFailureListener(new OnFailureListener() {
                                                                                            @Override
                                                                                            public void onFailure(@NonNull Exception e) {
                                                                                                storageReference = storage.getReference();
                                                                                            }
                                                                                        });
                                                                            }catch (Exception e){

                                                                            }
                                                                            //end of thumbnail
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            storageReference = storage.getReference();
                                                                        }
                                                                    });
                                                        }catch (Exception e){

                                                        }

                                                        //end of profile pic
                                                    }
                                                }

                                            }
                                        });

                                //end of name

                            }

                        }else {

                        }
                    }
                });


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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }
}