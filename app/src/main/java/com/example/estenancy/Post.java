package com.example.estenancy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.jsibbold.zoomage.ZoomageView;

import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import proj.me.bitframe.BeanBitFrame;
import proj.me.bitframe.BeanImage;
import proj.me.bitframe.FrameCallback;
import proj.me.bitframe.ImageType;
import proj.me.bitframe.ViewFrame;
import proj.me.bitframe.helper.FrameType;

public class Post extends Fragment {

    CircleImageView circleImageView;
    TextView name, title, time, desc, m, r;
    Button book, nav;
    String id, email;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser firebaseUser;
    private ArrayList<Uri> imagesUri;
    Locale ph = new Locale("en", "PH");
    Currency peso = Currency.getInstance(ph);
    NumberFormat pesoFormat = NumberFormat.getCurrencyInstance(ph);
    ViewFrame viewFrame;
    List<BeanImage> beanImageList;
    List<String> uriString;

    public Post() {
        // Required empty public constructor
    }

    public static Post newInstance(String param1, String param2) {
        Post fragment = new Post();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_post, container, false);

        //firebase init
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firebaseUser = mAuth.getCurrentUser();

        imagesUri = new ArrayList<>();
        beanImageList = new ArrayList<>();
        uriString = new ArrayList<>();

        circleImageView = v.findViewById(R.id.profile);
        name = v.findViewById(R.id.name1);
        title = v.findViewById(R.id.title);
        time = v.findViewById(R.id.timeStamp1);
        desc = v.findViewById(R.id.desc);
        book = v.findViewById(R.id.book_post);
        nav = v.findViewById(R.id.map_post);
        m = v.findViewById(R.id.monthly_payment);
        r = v.findViewById(R.id.reservation_fee);
        viewFrame = v.findViewById(R.id.view_frame);

        id = Post.this.getArguments().getString("id_from_card");
        email = Post.this.getArguments().getString("email");
        desc.setMovementMethod(new ScrollingMovementMethod());
       // title.setMovementMethod(new ScrollingMovementMethod());


        //method calls
        displayPost();
        loadAuthor();
        getImages();

        return v;
    }

    public void getImages(){
        storageReference = FirebaseStorage.getInstance().getReference().child("posts/"+id);
        storageReference.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {

                        for(StorageReference item : listResult.getItems()){
                            item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    uriString.add(uri.toString());
                                    BeanBitFrame beanBitFrame = new BeanBitFrame();
                                    beanBitFrame.setWidth(500);
                                    beanBitFrame.setHeight(200);
                                    beanBitFrame.setImageLink(uri.toString());
                                    beanBitFrame.setLoaded(true);
                                    beanImageList.add(beanBitFrame);
                                    viewFrame.showBitFrame(beanImageList, new FrameCallback() {
                                        @Override
                                        public void imageClick(ImageType imageType, int imagePosition, String imageLink, ViewFrame actionableViewFrame) {
                                            /*
                                            ImageViewer viewer = new ImageViewer();
                                            Bundle bundle = new Bundle();
                                            ArrayList<String> strings = new ArrayList<String>();
                                            strings.addAll(uriString);
                                            bundle.putStringArrayList("uri", strings);
                                            viewer.setArguments(bundle);
                                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                            transaction.replace(R.id.mainLayout, viewer).addToBackStack("tag");
                                            transaction.commit();

                                             */
                                        }

                                        @Override
                                        public void frameResult(List<BeanBitFrame> beanBitFrameList, ViewFrame actionableViewFrame) {

                                        }

                                        @Override
                                        public void addMoreClick(ViewFrame actionableViewFrame) {

                                        }

                                        @Override
                                        public void containerAdded(int containerWidth, int containerHeight, boolean isAddInLayout, ViewFrame actionableViewFrame) {

                                        }

                                        @Override
                                        public void loadedFrameColors(int lastLoadedFrameColor, int mixedLoadedColor, int inverseMixedLoadedColor, ViewFrame actionableViewFrame) {

                                        }
                                    }, FrameType.UNFRAMED);
                                }
                            });
                        }
                        //

                        //
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), e.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                });



    }

    public void loadAuthor(){
        db.collection("users")
                .document(email)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot != null && documentSnapshot.exists()){
                                String  namex = documentSnapshot.getString("firstName") +" "+ documentSnapshot.getString("lastName");
                                name.setText(namex);
                                //start of profile pic
                                storageReference = FirebaseStorage.getInstance().getReference().child("profilePhoto/"+email);
                                try{
                                    final File localFile = File.createTempFile(email, "jpg");
                                    storageReference.getFile(localFile)
                                            .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                    Bitmap dp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                                    circleImageView.setImageBitmap(dp);
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

                                //end of profile pic
                            }
                        }

                    }
                });
    }

    public void displayPost(){
        db.collection("posts")
                .whereEqualTo("id", id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                title.setText(document.getString("title_post"));

                                m.setText("Monthly payment: " + pesoFormat.format(Integer.parseInt(document.getString("monthly_payment"))));
                                r.setText("Reservation Fee: " + pesoFormat.format(Integer.parseInt(document.getString("reservation_fee"))));
                                desc.setText(document.getString("description"));
                                Timestamp timeStampFire = document.getTimestamp("timeStamp");
                                Date date = timeStampFire.toDate();
                                String timeStamp  = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
                                time.setText(timeStamp);
                            }
                        } else {

                        }
                    }
                });
    }


}