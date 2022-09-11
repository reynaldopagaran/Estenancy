package com.example.estenancy.homeFragments;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
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

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.estenancy.Login;
import com.example.estenancy.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    Fragment fragment = this;

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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firebaseUser = mAuth.getCurrentUser();

        //method calls
        setNamePhoto();
        setProfilePhoto();
        editProfile();
        return v;
    }

    public void editProfile(){
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
                                f.setPadding(15,0,0,-5);
                                f.setText("\nFirst Name:");
                                layout.addView(f);
                                final EditText fname = new EditText(context);
                                layout.addView(fname);

                                final TextView l = new TextView(context);
                                l.setPadding(15,0,0,-5);
                                l.setText("\nLast Name:");
                                layout.addView(l);
                                final EditText lname = new EditText(context);
                                layout.addView(lname);

                                final TextView a = new TextView(context);
                                a.setPadding(15,0,0,-5);
                                a.setText("\nAge:");
                                layout.addView(a);
                                final EditText age = new EditText(context);
                                layout.addView(age);

                                final TextView g = new TextView(context);
                                g.setPadding(15,0,0,0);
                                g.setText("\nGender:");
                                layout.addView(g);
                                final Spinner gender = new Spinner(context);
                                layout.addView(gender);
                                gender.setPadding(0,0,0,50);

                                List<String> spinnerArray =  new ArrayList<String>();
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
                                                if(task.isSuccessful()){
                                                    DocumentSnapshot documentSnapshot = task.getResult();
                                                    if(documentSnapshot != null && documentSnapshot.exists()){
                                                        fname.setText(documentSnapshot.getString("firstName"));
                                                        lname.setText(documentSnapshot.getString("lastName"));
                                                        age.setText(documentSnapshot.getString("age"));

                                                        switch(documentSnapshot.getString("gender")){
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
                                                .update("firstName",fname.getText().toString(), "lastName", lname.getText().toString(),
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
    public void setProfilePhoto(){
        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog pd = new ProgressDialog(getContext());
                pd.setTitle("Viewing image...");
                pd.show();

                storageReference = FirebaseStorage.getInstance().getReference().child("profilePhoto/"+mAuth.getCurrentUser().getEmail());
                try{
                    final File localFile = File.createTempFile(mAuth.getCurrentUser().getEmail(), "jpg");
                    storageReference.getFile(localFile)
                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    pd.dismiss();
                                    Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setTitle("Profile Photo");
                                    ImageView image = new ImageView(getActivity());
                                    image.setImageBitmap(bitmap);
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
                                    storageReference = storage.getReference();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    storageReference = storage.getReference();
                                }
                            });
                }catch (Exception e){
                    pd.dismiss();
                }
            }
        });
    }

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
            profilePhoto.setImageURI(result.getUri());
            uploadPicture(result.getUri());
        }
    }
    private void cropImage(Uri data) {
        CropImage.activity(data)
                .setMultiTouchEnabled(true)
                .setAspectRatio(1,1)
                .setMaxCropResultSize(2500,2500)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setOutputCompressQuality(50)
                .start(getContext(), this);
    }

    private void uploadPicture(Uri uri) {

        final ProgressDialog pd = new ProgressDialog(getContext());
        pd.setTitle("Uploading Image...");
        pd.show();

        StorageReference sr = storageReference.child("profilePhoto/" +mAuth.getCurrentUser().getEmail());

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
                pd.setMessage("Percentage: " +(int) + progressPercent + "%" );
            }
        });


    }

    public void setNamePhoto(){
        //set photo

        storageReference = FirebaseStorage.getInstance().getReference().child("profilePhoto/"+mAuth.getCurrentUser().getEmail());
        try{
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
        }catch (Exception e){

        }

        db.collection("users")
                .document(mAuth.getCurrentUser().getEmail())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot != null && documentSnapshot.exists()){
                        name.setText(documentSnapshot.getString("firstName") +" "+ documentSnapshot.getString("lastName"));
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