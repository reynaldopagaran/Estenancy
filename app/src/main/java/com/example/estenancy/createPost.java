package com.example.estenancy;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class createPost extends Fragment{

    EditText post_title, monthly_payment, reservation_fee, location, description;
    Button addPost;
    double latitude, longitude;
    private CircleImageView profilePhotoPost;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser firebaseUser;
    TextView namePost;
    ImageView imagePost;

    public createPost() {
        // Required empty public constructor
    }
    public static createPost newInstance(String param1, String param2) {
        createPost fragment = new createPost();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_post, container, false);


        location = v.findViewById(R.id.geopoint_post);
        addPost = v.findViewById(R.id.btnAdd_post);
        profilePhotoPost = v.findViewById(R.id.profilePic_post);
        namePost = v.findViewById(R.id.profileName_post);
        post_title = v.findViewById(R.id.title_post);
        monthly_payment = v.findViewById(R.id.monthly_post);
        reservation_fee = v.findViewById(R.id.reservationFee_post);
        description = v.findViewById(R.id.description_post);
        imagePost = v.findViewById(R.id.imagePost);

        //firebase init
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firebaseUser = mAuth.getCurrentUser();

        //method calls
        openMaps();
        LatLongBundle();
        loadProfilePhotoAndName();
        imageOnClick();



        return v;
    }

    //METHODS


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==1 && resultCode==RESULT_OK){
            if(data!=null){
                cropImage(data.getData());
            }
        }

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imagePost.setImageURI(result.getUri());
            createPost(result.getUri());
        }
    }
    private void cropImage(Uri data) {
        CropImage.activity(data)
                .setMultiTouchEnabled(true)
                .setAspectRatio(1,1)
                .setMaxCropResultSize(3500,3500)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setOutputCompressQuality(50)
                .start(getContext(), this);
    }

    public void imageOnClick(){
        imagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
            }
        });
    }


    public void createPost(Uri uri){
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if(TextUtils.isEmpty(post_title.getText().toString())){
                    post_title.setError("Please enter title.");
                    post_title.requestFocus();
                }else if(TextUtils.isEmpty(monthly_payment.getText().toString())){
                    monthly_payment.setError("Please enter monthly payment.");
                    monthly_payment.requestFocus();
                }else if(TextUtils.isEmpty(reservation_fee.getText().toString())){
                    reservation_fee.setError("Please enter reservation fee.");
                    reservation_fee.requestFocus();
                }else if(TextUtils.isEmpty(location.getText().toString())){
                    location.setError("Please choose coordinates.");
                    location.requestFocus();
                }else if(TextUtils.isEmpty(description.getText().toString())){
                    description.setError("Please enter description.");
                    description.requestFocus();
                }else if(imagePost.getDrawable() == null){
                    Toast.makeText(getActivity(), "Please upload a photo of your site.",
                            Toast.LENGTH_LONG).show();
                }else{

                    Toast.makeText(getActivity(), "saved",
                            Toast.LENGTH_LONG).show();

                    //save data to firestore
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


                    Map<String, Object> post = new HashMap<>();
                    post.put("email", user.getEmail());
                    post.put("title_post", post_title.getText().toString());
                    post.put("monthly_payment", monthly_payment.getText().toString());
                    post.put("reservation_fee", reservation_fee.getText().toString());
                    post.put("description", description.getText().toString());
                    post.put("coordinates", new GeoPoint(latitude, longitude));

                    db.collection("posts")
                            .add(post)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                    StorageReference sr = storageReference.child("posts/" + documentReference.getId());

                                    sr.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            Toast.makeText(getActivity(), "Post Uploaded",
                                                    Toast.LENGTH_LONG).show();

                                            //go to home
                                            Home home = new Home();
                                            FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                                            transaction.replace(R.id.mainLayout, home);
                                            transaction.commit();

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                                        }
                                    });

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                }
            }
        });
    }

    public void loadProfilePhotoAndName(){
        //load profile photo
        storageReference = FirebaseStorage.getInstance().getReference().child("profilePhoto/"+mAuth.getCurrentUser().getEmail());
        try{
            final File localFile = File.createTempFile(mAuth.getCurrentUser().getEmail(), "jpg");
            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            profilePhotoPost.setImageBitmap(bitmap);
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

        //load name
        db.collection("users")
                .document(mAuth.getCurrentUser().getEmail())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot != null && documentSnapshot.exists()){
                                namePost.setText(documentSnapshot.getString("firstName") +" "+ documentSnapshot.getString("lastName"));
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public void LatLongBundle(){
        try{
            latitude = Double.parseDouble(createPost.this.getArguments().getString("lat"));
            longitude = Double.parseDouble(createPost.this.getArguments().getString("longi"));

            location.setText(latitude +","+ longitude);
        }catch (Exception e){

        }
    }

    public void openMaps(){
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapsFragment mapsFragment = new MapsFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                transaction.replace(R.id.mainLayout, mapsFragment).addToBackStack("tag");
                transaction.commit();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}