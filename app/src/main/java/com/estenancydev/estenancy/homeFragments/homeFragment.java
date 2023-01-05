package com.estenancydev.estenancy.homeFragments;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.estenancydev.estenancy.Classes.post_model_recyclerView;
import com.estenancydev.estenancy.Post;
import com.estenancydev.estenancy.Classes.post_model_getPosts;
import com.example.estenancy.R;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Point;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import im.delight.android.location.SimpleLocation;
import it.beppi.tristatetogglebutton_library.TriStateToggleButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


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
    Fragment fragment;
    ShimmerFrameLayout shimmerFrameLayout;
    TriStateToggleButton switchHome;
    private MapboxDirections client;
    DirectionsRoute currentRoute;
    private SimpleLocation simpleLocation;
    private static final int REQUEST_CODE = 101;
    String token = "sk.eyJ1IjoiYW5kcm9tZWRhNyIsImEiOiJjbDg2YnZpa2IwNzk3M3VvaGN3ZnczNjcwIn0.axp5uCx677xYd9E6vxwWWA";
    LatLng latLngDone;
    String distance;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_home2, container, false);

        array_getPosts = new ArrayList<>();
        fragment = homeFragment.this;
        //firebase init
        //firebase init
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firebaseUser = mAuth.getCurrentUser();
        shimmerFrameLayout = v.findViewById(R.id.shimmer);
        switchHome = v.findViewById(R.id.switch_avail_home);
        simpleLocation = new SimpleLocation(getContext());
        swipeRefreshLayout = v.findViewById(R.id.swipeRefreshLayout);

        recyclerView = v.findViewById(R.id.recyclerViewx);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //method calls
        getPosts("Available");
        refreshFeed();
        setSwitchHome();
        getLatLang();
        return v;
    }


    @SuppressLint("MissingPermission")
    private void getLatLang() {
        if (ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext()
                , Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION
                    }, REQUEST_CODE);
            return;

        } else {

            if (!simpleLocation.hasLocationEnabled()) {
                // ask the user to enable location access
                SimpleLocation.openSettings(getContext());
            } else {
                latLngDone = new LatLng(simpleLocation.getLatitude(), simpleLocation.getLongitude());
            }
        }
    }


    public void setSwitchHome(){
        switchHome.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {
                switch (toggleStatus) {
                    case off:
                        getPosts("Not Available");
                        break;
                    case on:
                        getPosts("Available");
                        break;
                    default:
                        break;
                }
            }
        });
    }

    public void refreshFeed(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(switchHome.getToggleStatus().toString().equals("on")){
                    swipeRefreshLayout.setRefreshing(false);
                    getPosts("Available");
                }else{
                    swipeRefreshLayout.setRefreshing(false);
                    getPosts("Not Available");
                }
            }
        });
    }

    public void getPosts(String status){
        db.collection("posts").whereEqualTo("status", status)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            shimmerFrameLayout.setVisibility(View.VISIBLE);
                            shimmerFrameLayout.startShimmer();
                            array_getPosts.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                               String title = document.getString("title_post");
                               String email = document.getString("email");
                               String id = document.getString("id");
                               String stat = document.getString("status");
                               String descr = document.getString("description");
                               String address = document.getString("address");
                               LatLng latLng = new LatLng(document.getGeoPoint("coordinates").getLatitude(), document.getGeoPoint("coordinates").getLongitude());
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
                                                                                final File localFile = File.createTempFile("site_image_0", "jpg");
                                                                                storageReference.getFile(localFile)
                                                                                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                                                            @Override
                                                                                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                                                shimmerFrameLayout.stopShimmer();
                                                                                                shimmerFrameLayout.setVisibility(View.GONE);
                                                                                                Bitmap thumbnail = BitmapFactory.decodeFile(localFile.getAbsolutePath());

                                                                                                client = MapboxDirections.builder()
                                                                                                        .origin(Point.fromLngLat(latLngDone.longitude, latLngDone.latitude))
                                                                                                        .destination(Point.fromLngLat(latLng.longitude, latLng.latitude))
                                                                                                        .overview(DirectionsCriteria.OVERVIEW_FULL)
                                                                                                        .profile(DirectionsCriteria.PROFILE_DRIVING)
                                                                                                        .accessToken(token)
                                                                                                        .build();

                                                                                                client.enqueueCall(new Callback<DirectionsResponse>() {
                                                                                                    @Override
                                                                                                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                                                                                                        currentRoute = response.body().routes().get(0);
                                                                                                        distance = String.format("%.2f", currentRoute.distance() / 1000) + " km";

                                                                                                        array_getPosts.add(new post_model_getPosts(distance,address,
                                                                                                                title, name, dp, thumbnail, timeStamp, id, email,stat, descr));
                                                                                                        storageReference = storage.getReference();
                                                                                                        post_model_recyclerView post_model_recyclerView = new post_model_recyclerView(getContext(), array_getPosts, new post_model_recyclerView.ItemClickListener() {
                                                                                                            @Override
                                                                                                            public void onItemClick(post_model_getPosts post_model_getPosts) {
                                                                                                                showPost(post_model_getPosts.getId(), post_model_getPosts.getEmail(), post_model_getPosts.getDistance());
                                                                                                            }
                                                                                                        });
                                                                                                        recyclerView.setAdapter(post_model_recyclerView);
                                                                                                    }

                                                                                                    @Override
                                                                                                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                                                                                                    }
                                                                                                });


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

    private void showPost(String id, String email, String distance){
        Post post = new Post();
        Bundle bundle = new Bundle();
        bundle.putString("id_from_card", id);
        bundle.putString("email", email);
        bundle.putString("distance", distance);
        post.setArguments(bundle);
        FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
        transaction.replace(R.id.mainLayout, post).addToBackStack("tag");
        transaction.commit();
    }
}