package com.example.estenancy;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class createPost extends Fragment {

    EditText post_title, monthly_payment, reservation_fee, location, description;
    Button addPost;
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
    private int count = 0, imageCount = -1;
    ProgressDialog pd;
    private List<String> categories;
    TextView label;
    GeoPoint geoPoint;
    private List<String> uriNames;
    private List<String> uriId;
    private List<String> removedId;
    private ArrayList<Uri> uriPhotos;
    String id;
    File localFile;
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
        label = v.findViewById(R.id.textView4);

        pd = new ProgressDialog(getContext());

        //firebase init
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firebaseUser = mAuth.getCurrentUser();

        uriPhotos = new ArrayList<>();
        uriNames = new ArrayList<>();
        uriId = new ArrayList<>();
        removedId = new ArrayList<>();
        //method calls

        openMaps();
        //LatLongBundle();
        loadProfilePhotoAndName();
        setAddPost();


        if (createPost.this.getArguments().getString("address") == null) {
            editOrCreate();
        } else {
            setSavedStates();
        }


        imageClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageOnClick();
            }
        });

        return v;
    }


    //METHODS

    public void setAddPost() {
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (label.getText().toString().equals("Edit Post")) {
                    try {
                        editPost();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (label.getText().toString().equals("Create Post")) {
                    createPost();
                }

            }
        });
    }

    public void setSavedStates() {
        try {

            geoPoint = new GeoPoint(Double.parseDouble(createPost.this.getArguments().getString("lat")), Double.parseDouble(createPost.this.getArguments().getString("longi")));
            address = createPost.this.getArguments().getString("address");
            location.setText(String.valueOf(address));
            label.setText(createPost.this.getArguments().getString("label"));
            post_title.setText(createPost.this.getArguments().getString("title"));
            monthly_payment.setText(createPost.this.getArguments().getString("month"));
            reservation_fee.setText(createPost.this.getArguments().getString("res"));
            description.setText(createPost.this.getArguments().getString("desc"));
            uriNames.addAll(createPost.this.getArguments().getStringArrayList("names"));
            id = createPost.this.getArguments().getString("id");

            ArrayList<Parcelable> uris =
                    createPost.this.getArguments().getParcelableArrayList("photos");
            for (Parcelable p : uris) {
                uriPhotos.add((Uri) p);
                setAdapter(uriPhotos, uriNames);
            }

            if (label.getText().toString().equals("Edit Post")) {
                addPost.setText("Edit");
            } else if (label.getText().toString().equals("Create Post")) {
                addPost.setText("Add");
            }

        } catch (Exception e) {
            Log.d("tag", "bug =" + e.toString());
        }

    }

    public void editOrCreate() {
        try {
            if (createPost.this.getArguments().getString("label").equals("Edit Post")) {
                id = createPost.this.getArguments().getString("id");

                label.setText("Edit Post");
                addPost.setText("Edit");

                //get data
                db.collection("posts")
                        .document(id)
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if (documentSnapshot != null && documentSnapshot.exists()) {

                                        geoPoint = new GeoPoint(documentSnapshot.getGeoPoint("coordinates").getLatitude(), documentSnapshot.getGeoPoint("coordinates").getLongitude());
                                        location.setText(documentSnapshot.getString("address"));
                                        post_title.setText(documentSnapshot.getString("title_post"));
                                        monthly_payment.setText(documentSnapshot.getString("monthly_payment"));
                                        reservation_fee.setText(documentSnapshot.getString("reservation_fee"));
                                        description.setText(documentSnapshot.getString("description"));
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

                //get photos
                db.collection("categories")
                        .document(id)
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if (documentSnapshot != null && documentSnapshot.exists()) {
                                        storageReference = FirebaseStorage.getInstance().getReference().child("posts/" + id);
                                        storageReference.listAll()
                                                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                                                    @Override
                                                    public void onSuccess(ListResult listResult) {
                                                        for (StorageReference item : listResult.getItems()) {
                                                            item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                                @Override
                                                                public void onSuccess(Uri uri){
                                                                        uriPhotos.add(uri);
                                                                        uriNames.add(documentSnapshot.getString(item.getName()));
                                                                        uriId.add(item.getName());
                                                                        setAdapter(uriPhotos, uriNames);
                                                                    //end of save uri to temp file
                                                                }
                                                            });
                                                        }

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getActivity(), e.toString(),
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                    }
                                }
                            }
                        });

            } else if (createPost.this.getArguments().getString("label").equals("Create Post")) {
                label.setText("Create Post");
                addPost.setText("Add");
            }
        } catch (Exception e) {

        }

    }




    public void disableTexts() {
        if (TextUtils.isEmpty(location.getText().toString())) {
            imageClick.setEnabled(false);
            post_title.setEnabled(false);
            monthly_payment.setEnabled(false);
            reservation_fee.setEnabled(false);
            description.setEnabled(false);
        } else {
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

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK) {
            if (data != null) {
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
                        if (label.getText().toString().equals("Edit Post")) {
                            uriPhotos.add(data.getData());
                            uriNames.add(category.getText().toString());
                            for(int a = 0; a < removedId.size(); a++){
                                uriId.add(removedId.get(a));
                            }
                            imageCount++;
                            description.clearFocus();
                            setAdapter(uriPhotos, uriNames);
                            imagePost.setCurrentItem(uriPhotos.size());

                        } else if (label.getText().toString().equals("Create Post")) {
                            imagesUri.add(data.getData());
                            categories.add(category.getText().toString());
                            imageCount++;
                            description.clearFocus();
                            setAdapter(imagesUri, categories);
                            imagePost.setCurrentItem(imagesUri.size());
                        }
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


    public void imageOnClick() {
        Intent gallery =
                new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.putExtra(Intent.ACTION_GET_CONTENT, true);
        startActivityForResult(gallery, IMAGE_PICK_CODE);
    }

    public void editPost() throws IOException {

        if (TextUtils.isEmpty(post_title.getText().toString())) {
            post_title.setError("Please enter title.");
            post_title.requestFocus();
        } else if (TextUtils.isEmpty(monthly_payment.getText().toString())) {
            monthly_payment.setError("Please enter monthly payment.");
            monthly_payment.requestFocus();
        } else if (TextUtils.isEmpty(reservation_fee.getText().toString())) {
            reservation_fee.setError("Please enter reservation fee.");
            reservation_fee.requestFocus();
        } else if (TextUtils.isEmpty(location.getText().toString())) {
            location.setError("Please choose coordinates.");
            location.requestFocus();
        } else if (TextUtils.isEmpty(description.getText().toString())) {
            description.setError("Please enter description.");
            description.requestFocus();
        } else if (uriPhotos.size() < 5) {
            Toast.makeText(getActivity(), "Site photos must be 5 and up.",
                    Toast.LENGTH_SHORT).show();
        } else {
            pd.setTitle("Updating...");
            pd.show();
            db.collection("posts")
                    .document(id)
                    .update("address", location.getText().toString(),
                            "title_post", post_title.getText().toString(),
                            "monthly_payment", monthly_payment.getText().toString(),
                            "reservation_fee", reservation_fee.getText().toString(),
                            "coordinates", new GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude()),
                            "description", description.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), "Error.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

            db.collection("categories").document(id).delete();
            Map<String, Object> cat = new HashMap<>();
            for (int i = 0; i < uriPhotos.size(); i++) {
                cat.put(uriId.get(i), uriNames.get(i));
                try {
                    editImage(uriPhotos.get(i), id, uriId.get(i));
                } catch (Exception e) {
                    Log.d("tag", "rez: "+e);
                }

            }
            db.collection("categories").document(id).set(cat);

        }

    }


    public void createPost() {

        if (TextUtils.isEmpty(post_title.getText().toString())) {
            post_title.setError("Please enter title.");
            post_title.requestFocus();
        } else if (TextUtils.isEmpty(monthly_payment.getText().toString())) {
            monthly_payment.setError("Please enter monthly payment.");
            monthly_payment.requestFocus();
        } else if (TextUtils.isEmpty(reservation_fee.getText().toString())) {
            reservation_fee.setError("Please enter reservation fee.");
            reservation_fee.requestFocus();
        } else if (TextUtils.isEmpty(location.getText().toString())) {
            location.setError("Please choose coordinates.");
            location.requestFocus();
        } else if (TextUtils.isEmpty(description.getText().toString())) {
            description.setError("Please enter description.");
            description.requestFocus();
        } else if (imagesUri.size() < 5) {
            Toast.makeText(getActivity(), "Site photos must be 5 and up.",
                    Toast.LENGTH_SHORT).show();
        } else {
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
            post.put("coordinates", new GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude()));
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

                            for (int i = 0; i < imagesUri.size(); i++) {
                                cat.put("site_image_" + (i), categories.get(i));
                                try {
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imagesUri.get(i));
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
                                    byte[] imageByte = stream.toByteArray();
                                    uploadImage(imageByte, documentReference.getId(), "site_image_" + (i));
                                } catch (Exception e) {
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

    public void uploadImage(byte[] imageByte, String id, String photo_name) {

        StorageReference sr = storageReference.child("posts/" + id + "/" + photo_name);

        sr.putBytes(imageByte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                count += 1;
                if (count == imagesUri.size()) {
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

    public void editImage(Uri imageByts, String id, String photo_name) {

        StorageReference sr = storageReference.child("posts/" + id + "/" + photo_name);

        sr.putFile(imageByts)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                count += 1;
                if (count == uriPhotos.size()) {
                    pd.dismiss();
                    Toast.makeText(getActivity(), "Updated.",
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

                Log.d("tag", "bug x= " + e.toString());
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

            }
        });
    }

    public void loadProfilePhotoAndName() {
        //load profile photo
        storageReference = FirebaseStorage.getInstance().getReference().child("profilePhoto/" + mAuth.getCurrentUser().getEmail());
        try {
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
        } catch (Exception e) {

        }

        //load name
        db.collection("users")
                .document(mAuth.getCurrentUser().getEmail())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                namePost.setText(documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName"));
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }


    public void openMaps() {
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapsFragment mapsFragment = new MapsFragment();
                //bundles
                Bundle outState = new Bundle();
                outState.putString("title", post_title.getText().toString());
                outState.putString("month", monthly_payment.getText().toString());
                outState.putString("res", reservation_fee.getText().toString());
                outState.putString("desc", description.getText().toString());
                outState.putString("id", id);
                outState.putParcelableArrayList("photos", uriPhotos);
                outState.putStringArrayList("names", (ArrayList<String>) uriNames);
                if (label.getText().toString().equals("Edit Post")) {
                    outState.putString("label", "Edit Post");
                } else if (label.getText().toString().equals("Create Post")) {
                    outState.putString("label", "Create Post");
                }
                mapsFragment.setArguments(outState);
                FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                transaction.replace(R.id.mainLayout, mapsFragment).addToBackStack("tag");
                transaction.commit();

            }
        });
    }

    public void setAdapter(ArrayList<Uri> uriPhotos, List<String> uriNames) {
        ImagesAdapter imagesAdapter = new ImagesAdapter(getContext(), uriPhotos, uriNames, imagePost, id, uriId, removedId);
        imagePost.setAdapter(imagesAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        //disableTexts();
    }
}