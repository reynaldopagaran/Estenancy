package com.example.estenancy.homeFragments;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.estenancy.Classes.my_listing;
import com.example.estenancy.Classes.my_listing_get_post;
import com.example.estenancy.Classes.post_model_getPosts;
import com.example.estenancy.Classes.post_model_recyclerView;
import com.example.estenancy.Home;
import com.example.estenancy.Post;
import com.example.estenancy.R;
import com.example.estenancy.createPost;
import com.facebook.shimmer.ShimmerFrameLayout;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class profileFragment extends Fragment {

    TextView name;
    Button addListing, editProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CircleImageView profilePhoto;
    public Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    ShimmerFrameLayout shimmerFrameLayout;
    SwipeRefreshLayout swipeRefreshLayout;

    //for my listing
    RecyclerView recyclerView;
    List<my_listing_get_post> myListing;
    List<post_model_getPosts> array_getPosts;

    public profileFragment() {
        // Required empty public constructor
    }

    public static profileFragment newInstance(String param1, String param2) {
        profileFragment fragment = new profileFragment();
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
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        name = v.findViewById(R.id.tv_name);
        addListing = v.findViewById(R.id.btn_add_listing);
        editProfile = v.findViewById(R.id.btn_edit_profile);
        profilePhoto = v.findViewById(R.id.profile_photo);
        shimmerFrameLayout = v.findViewById(R.id.shimmer1);
        // swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout1);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firebaseUser = mAuth.getCurrentUser();

        //my listing
        shimmerFrameLayout.startShimmer();
        myListing = new ArrayList<>();
        array_getPosts = new ArrayList<>();
        recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //method calls
        setNamePhoto();
        setProfilePhoto();
        editProfile();
        addPost();
        showMyListing();
        // refreshFeed();

        return v;
    }

    //start of my listing feed

    private void showPost(String id, String email) {
        Post post = new Post();
        Bundle bundle = new Bundle();
        bundle.putString("id_from_card", id);
        bundle.putString("email", email);
        post.setArguments(bundle);
        FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
        transaction.replace(R.id.mainLayout, post).addToBackStack("tag");
        transaction.commit();
    }


    public void refreshFeed() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                profileFragment profileFragment = new profileFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.mainLayout, profileFragment);
                transaction.commit();
            }
        });
    }

    public void showMyListing() {
        db.collection("posts")
                .whereEqualTo("email", firebaseUser.getEmail())
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
                                String timeStamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
                                //start of my thumbnail
                                storageReference = FirebaseStorage.getInstance().getReference().child("posts/" + id + "/site_image_0");
                                try {
                                    final File localFile = File.createTempFile("site_image_0", "jpg");
                                    storageReference.getFile(localFile)
                                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                                                    shimmerFrameLayout.stopShimmer();
                                                    shimmerFrameLayout.setVisibility(View.GONE);
                                                    Bitmap thumbnail = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                                    myListing.add(new my_listing_get_post(title, timeStamp, thumbnail, id, email));
                                                    storageReference = storage.getReference();

                                                    my_listing my_listing = new my_listing(getContext(), myListing, new my_listing.ItemClickListener() {
                                                        @Override
                                                        public void onItemClick(my_listing_get_post my_listing_get_post) {
                                                            showPost(my_listing_get_post.getId(), my_listing_get_post.getEmail());
                                                        }
                                                    });

                                                    recyclerView.setAdapter(my_listing);
                                                    registerForContextMenu(recyclerView);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    storageReference = storage.getReference();
                                                    shimmerFrameLayout.stopShimmer();
                                                }
                                            });
                                } catch (Exception e) {

                                }
                                //end of my thumbnail;
                            }
                        } else {

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        shimmerFrameLayout.stopShimmer();
                    }
                });
    }
    //end of my listing feed

    public void addPost() {
        addListing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPost createPost = new createPost();
                Bundle bundle = new Bundle();
                bundle.putString("label", "Create Post");
                createPost.setArguments(bundle);
                FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                transaction.replace(R.id.mainLayout, createPost).addToBackStack("tag");
                transaction.commit();
            }
        });
    }

    public void editProfile() {
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Edit Profile");
                builder.setMessage("Please choose an option.");


                builder.setPositiveButton("Update Profile Photo",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(intent, 1);
                            }
                        });
                builder.setNeutralButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.setNegativeButton("Update Details",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Context context = getContext();
                                LinearLayout layout = new LinearLayout(context);
                                layout.setOrientation(LinearLayout.VERTICAL);

                                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                                final TextView f = new TextView(context);
                                f.setPadding(15, 0, 0, -5);
                                f.setText("\nFirst Name:");
                                layout.addView(f);
                                final EditText fname = new EditText(context);
                                layout.addView(fname);

                                final TextView l = new TextView(context);
                                l.setPadding(15, 0, 0, -5);
                                l.setText("\nLast Name:");
                                layout.addView(l);
                                final EditText lname = new EditText(context);
                                layout.addView(lname);

                                final TextView a = new TextView(context);
                                a.setPadding(15, 0, 0, -5);
                                a.setText("\nAge:");
                                layout.addView(a);
                                final EditText age = new EditText(context);
                                layout.addView(age);

                                final TextView g = new TextView(context);
                                g.setPadding(15, 0, 0, 0);
                                g.setText("\nGender:");
                                layout.addView(g);
                                final Spinner gender = new Spinner(context);
                                layout.addView(gender);
                                gender.setPadding(0, 0, 0, 50);

                                List<String> spinnerArray = new ArrayList<String>();
                                spinnerArray.add("Male");
                                spinnerArray.add("Female");
                                spinnerArray.add("I'd rather not say");

                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                        getContext(), android.R.layout.simple_spinner_item, spinnerArray);

                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                gender.setAdapter(adapter);

                                alert.setTitle("Update Details");
                                alert.setView(f);
                                alert.setView(fname);
                                alert.setView(l);
                                alert.setView(lname);
                                alert.setView(a);
                                alert.setView(age);
                                alert.setView(g);
                                alert.setView(gender);


                                //get data
                                db.collection("users")
                                        .document(mAuth.getCurrentUser().getEmail())
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot documentSnapshot = task.getResult();
                                                    if (documentSnapshot != null && documentSnapshot.exists()) {
                                                        fname.setText(documentSnapshot.getString("firstName"));
                                                        lname.setText(documentSnapshot.getString("lastName"));
                                                        age.setText(documentSnapshot.getString("age"));

                                                        switch (documentSnapshot.getString("gender")) {
                                                            case "Male":
                                                                gender.setSelection(0);
                                                                break;
                                                            case "Female":
                                                                gender.setSelection(1);
                                                                break;
                                                            case "I'd rather not say":
                                                                gender.setSelection(2);
                                                                break;
                                                            default:
                                                                break;
                                                        }

                                                    }
                                                }
                                            }
                                        });

                                alert.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                        db.collection("users")
                                                .document(mAuth.getCurrentUser().getEmail())
                                                .update("firstName", fname.getText().toString(), "lastName", lname.getText().toString(),
                                                        "age", age.getText().toString(), "gender", gender.getSelectedItem().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        Toast.makeText(getActivity(), "Name Updated",
                                                                Toast.LENGTH_SHORT).show();

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getActivity(), "Error.",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });

                                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.cancel();
                                    }
                                });
                                alert.setView(layout);
                                alert.show();

                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
    }

    //make this clean
    public void setProfilePhoto() {
        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Profile Photo");
                ImageView image = new ImageView(getActivity());
                image.setImageDrawable(profilePhoto.getDrawable());
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                cropImage(data.getData());
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            profilePhoto.setImageURI(result.getUri());
            uploadPicture(result.getUri());
        }
    }

    private void cropImage(Uri data) {
        CropImage.activity(data)
                .setMultiTouchEnabled(true)
                .setAspectRatio(1, 1)
                .setMaxCropResultSize(2500, 2500)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setOutputCompressQuality(50)
                .start(getContext(), this);
    }

    private void uploadPicture(Uri uri) {

        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setTitle("Uploading Image...");
        pd.show();

        StorageReference sr = storageReference.child("profilePhoto/" + mAuth.getCurrentUser().getEmail());

        sr.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                pd.dismiss();
                Toast.makeText(getActivity(), "Image Updated.",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(), "Error.",
                        Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                pd.setMessage("Percentage: " + (int) +progressPercent + "%");
            }
        });


    }

    public void setNamePhoto() {
        //set photo

        storageReference = FirebaseStorage.getInstance().getReference().child("profilePhoto/" + mAuth.getCurrentUser().getEmail());
        try {
            final File localFile = File.createTempFile(mAuth.getCurrentUser().getEmail(), "jpg");
            storageReference.getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                            profilePhoto.setImageBitmap(bitmap);
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

        db.collection("users")
                .document(mAuth.getCurrentUser().getEmail())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                name.setText(documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName"));
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}