package com.example.estenancy;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.estenancy.Adapters.ImagesAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mapbox.android.gestures.MultiFingerTapGestureDetector;
import com.watermark.androidwm.WatermarkBuilder;
import com.watermark.androidwm.bean.WatermarkText;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class createPost extends Fragment{

    EditText post_title, monthly_payment, reservation_fee, location, description;
    Button addPost;
    double latitude, longitude;
    String address;
    private CircleImageView profilePhotoPost;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser firebaseUser;
    TextView namePost;
    ViewPager imagePost;
    ImageView imageClick;
    private final int IMAGE_PICK_CODE = 39;
    private ArrayList<Uri> imagesUri;
    private int count = 0, imageCount=-1;
    ProgressDialog pd;
    private List<String> categories;


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
        imagesUri = new ArrayList<>();
        categories = new ArrayList<>();
        imageClick = v.findViewById(R.id.imageClick);

        pd = new ProgressDialog(getContext());

        //firebase init
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firebaseUser = mAuth.getCurrentUser();

        //method calls
        disableTexts();
        openMaps();
        LatLongBundle();
        loadProfilePhotoAndName();
        createPost();


        imageClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageOnClick();
            }
        });

        return v;
    }

    //METHODS


    public void disableTexts(){
        if(TextUtils.isEmpty(location.getText().toString())){
            imageClick.setEnabled(false);
            post_title.setEnabled(false);
            monthly_payment.setEnabled(false);
            reservation_fee.setEnabled(false);
            description.setEnabled(false);
        }else{
            imageClick.setEnabled(true);
            post_title.setEnabled(true);
            monthly_payment.setEnabled(true);
            reservation_fee.setEnabled(true);
            description.setEnabled(true);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK){
            if(data != null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Enter category");
                    builder.setMessage("Sample: Bedroom");

                    Context context = getContext();
                    LinearLayout layout = new LinearLayout(context);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    final EditText category = new EditText(context);
                    layout.addView(category);
                    builder.setView(category);

                    builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            imagesUri.add(data.getData());
                            categories.add(category.getText().toString());
                            imageCount++;
                            description.clearFocus();
                            setAdapter();
                            imagePost.setCurrentItem(imagesUri.size());
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getActivity(), "Cancelled.",
                                        Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setView(layout);
                    builder.show();
              }

        }

    }


    public void imageOnClick(){
        Intent gallery =
                new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.putExtra(Intent.ACTION_GET_CONTENT, true);
        startActivityForResult(gallery, IMAGE_PICK_CODE);
    }


    public void createPost(){

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
                }else if(imagesUri.size() < 5){
                    Toast.makeText(getActivity(), "Site photos must be 5 and up.",
                            Toast.LENGTH_SHORT).show();
                }else{
                    //save data to firestore

                    pd.setTitle("Posting...");
                    pd.show();

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    Map<String, Object> post = new HashMap<>();
                    post.put("email", user.getEmail());
                    post.put("title_post", post_title.getText().toString());
                    post.put("monthly_payment", monthly_payment.getText().toString());
                    post.put("reservation_fee", reservation_fee.getText().toString());
                    post.put("description", description.getText().toString());
                    post.put("coordinates", new GeoPoint(latitude, longitude));
                    post.put("address", location.getText().toString());
                    post.put("status", "Available");
                    post.put("timeStamp", FieldValue.serverTimestamp());
                    db.collection("posts")
                            .add(post)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                    db.collection("posts").document(documentReference.getId())
                                            .update("id", documentReference.getId());

                                    Map<String, Object> cat = new HashMap<>();

                                    for(int i = 0; i < imagesUri.size(); i++){
                                        cat.put("site_image_"+ (i), categories.get(i));
                                        try{
                                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imagesUri.get(i));
                                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
                                            byte[] imageByte = stream.toByteArray();
                                            uploadImage(imageByte, documentReference.getId(), "site_image_"+ (i));

                                        }catch (Exception e){
                                            Toast.makeText(getActivity(), e.toString(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    db.collection("categories").document(documentReference.getId())
                                            .set(cat);
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

    public void uploadImage(byte [] imageByte, String id, String photo_name){

        StorageReference sr = storageReference.child("posts/"+id+"/"+photo_name);

        sr.putBytes(imageByte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                count += 1;
                if(count == imagesUri.size()){
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Posted.",
                            Toast.LENGTH_LONG).show();
                    Home home = new Home();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                    transaction.replace(R.id.mainLayout, home);
                    transaction.commit();
                }
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
            address = createPost.this.getArguments().getString("address");

          ///  Toast.makeText(getActivity(), address,
                   // Toast.LENGTH_LONG).show();
           // location.setText("String.valueOf(latitude)");
            location.setText(String.valueOf(address));

        }catch (Exception e){
           // location.setText(e.toString());
            //Toast.makeText(getActivity(), String.valueOf(address),
                 //   Toast.LENGTH_LONG).show();
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

    public void setAdapter(){
        ImagesAdapter imagesAdapter = new ImagesAdapter(getContext(), imagesUri, categories, imagePost);
        imagePost.setAdapter(imagesAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        disableTexts();
    }
}