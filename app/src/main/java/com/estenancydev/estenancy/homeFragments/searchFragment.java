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

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.estenancydev.estenancy.Classes.post_model_getPosts;
import com.estenancydev.estenancy.Classes.post_model_recyclerView;
import com.estenancydev.estenancy.Post;
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
import com.shawnlin.numberpicker.NumberPicker;

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


public class searchFragment extends Fragment {

    RecyclerView recyclerView;
    List<post_model_getPosts> array_getPosts;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser firebaseUser;
    ShimmerFrameLayout shimmerFrameLayout;
    View v;
    private SimpleLocation simpleLocation;
    private static final int REQUEST_CODE = 101;
    LatLng latLngDone;
    ArrayList<LatLng> geoPoints;
    EditText search;
    private MapboxDirections client;
    DirectionsRoute currentRoute;
    ImageView btn_search;
    TriStateToggleButton toggle;
    String token = "sk.eyJ1IjoiYW5kcm9tZWRhNyIsImEiOiJjbDg2YnZpa2IwNzk3M3VvaGN3ZnczNjcwIn0.axp5uCx677xYd9E6vxwWWA";
    String distance;
    Boolean ifNearby = false;
    NumberPicker numberPickers;
    public searchFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static searchFragment newInstance(String param1, String param2) {
        searchFragment fragment = new searchFragment();
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
        v = inflater.inflate(R.layout.fragment_search, container, false);

        array_getPosts = new ArrayList<>();
        //firebase init
        //firebase init
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        firebaseUser = mAuth.getCurrentUser();
        shimmerFrameLayout = v.findViewById(R.id.shimmery);
        simpleLocation = new SimpleLocation(getContext());
        numberPickers = v.findViewById(R.id.numberPicks);

        toggle = v.findViewById(R.id.switch_avail_homey);
        search = v.findViewById(R.id.et_search);
        btn_search = v.findViewById(R.id.btn_search);
        geoPoints = new ArrayList<>();


        recyclerView = v.findViewById(R.id.recyclerViewy);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //method calls
        setBtn_search();
        suggestNearby();
        getLatLang();
        return v;
    }


    public void suggestNearby() {

        numberPickers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(toggle.getToggleStatus().equals(TriStateToggleButton.ToggleStatus.on)){
                    getPosts(search.getText().toString());
                }else{

                }
            }
        });


        toggle.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {
                switch (toggleStatus) {
                    case on:
                        ifNearby = true;
                        getPosts(search.getText().toString());
                        break;
                    case off:
                        ifNearby = false;
                        getPosts(search.getText().toString());
                        break;
                    default:
                        break;
                }
            }
        });
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

    public void setBtn_search() {
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(search.getText().toString())) {
                    Toast.makeText(getContext(), "Search bar is empty.", Toast.LENGTH_SHORT).show();
                } else {
                    getPosts(search.getText().toString());
                }

            }
        });
    }


    public void getPosts(String search) {
        shimmerFrameLayout.setVisibility(View.VISIBLE);
        shimmerFrameLayout.startShimmer();
        array_getPosts.clear();
        db.collection("posts").orderBy("address").startAt(search).endAt(search + "\uf8ff")
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String title = document.getString("title_post");
                                String email = document.getString("email");
                                String id = document.getString("id");
                                String stat = document.getString("status");
                                String descr = document.getString("description");
                                String address = document.getString("address");
                                LatLng latLng = new LatLng(document.getGeoPoint("coordinates").getLatitude(), document.getGeoPoint("coordinates").getLongitude());
                                Timestamp timeStampFire = document.getTimestamp("timeStamp");
                                Date date = timeStampFire.toDate();
                                String timeStamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
                                //start of name
                                db.collection("users")
                                        .document(email)
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot documentSnapshot = task.getResult();
                                                    if (documentSnapshot != null && documentSnapshot.exists()) {
                                                        String name = documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName");
                                                        //start of profile pic
                                                        storageReference = FirebaseStorage.getInstance().getReference().child("profilePhoto/" + email);
                                                        try {
                                                            final File localFile = File.createTempFile(email, "jpg");
                                                            storageReference.getFile(localFile)
                                                                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                                        @Override
                                                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                                            Bitmap dp = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                                                            storageReference = storage.getReference();
                                                                            //start of thumbnail
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

                                                                                                //
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
                                                                                                        if(ifNearby){
                                                                                                            if(currentRoute.distance() / 1000 < numberPickers.getValue()){
                                                                                                                array_getPosts.add(new post_model_getPosts( String.format("%.2f", currentRoute.distance() / 1000) + " km", address, title, name, dp, thumbnail, timeStamp, id, email, stat, descr));
                                                                                                                storageReference = storage.getReference();
                                                                                                                post_model_recyclerView post_model_recyclerView = new post_model_recyclerView(getContext(), array_getPosts, new post_model_recyclerView.ItemClickListener() {
                                                                                                                    @Override
                                                                                                                    public void onItemClick(post_model_getPosts post_model_getPosts) {
                                                                                                                        showPost(post_model_getPosts.getId(), post_model_getPosts.getEmail(), post_model_getPosts.getDistance());
                                                                                                                    }
                                                                                                                });
                                                                                                                recyclerView.setAdapter(post_model_recyclerView);

                                                                                                            }
                                                                                                        }else{
                                                                                                            array_getPosts.add(new post_model_getPosts( String.format("%.2f", currentRoute.distance() / 1000) + " km" ,address, title, name, dp, thumbnail, timeStamp, id, email, stat, descr));
                                                                                                            storageReference = storage.getReference();
                                                                                                            post_model_recyclerView post_model_recyclerView = new post_model_recyclerView(getContext(), array_getPosts, new post_model_recyclerView.ItemClickListener() {
                                                                                                                @Override
                                                                                                                public void onItemClick(post_model_getPosts post_model_getPosts) {
                                                                                                                    showPost(post_model_getPosts.getId(), post_model_getPosts.getEmail(), post_model_getPosts.getDistance());
                                                                                                                }
                                                                                                            });
                                                                                                            recyclerView.setAdapter(post_model_recyclerView);

                                                                                                        }

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
                                                                            } catch (Exception e) {

                                                                            }
                                                                            //end of thumbnail
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                        @Override
                                                                        public void onFailure(@NonNull Exception e) {
                                                                            storageReference = storage.getReference();
                                                                        }
                                                                    });
                                                        } catch (Exception e) {

                                                        }

                                                        //end of profile pic
                                                    } else if (documentSnapshot == null && !documentSnapshot.exists()) {
                                                        shimmerFrameLayout.stopShimmer();
                                                        shimmerFrameLayout.setVisibility(View.GONE);
                                                    }
                                                }

                                            }
                                        });

                                //end of name

                            }

                        } else {
                            Toast.makeText(getContext(), "No results for " + "'" + search + "'", Toast.LENGTH_SHORT).show();
                            shimmerFrameLayout.stopShimmer();
                            shimmerFrameLayout.setVisibility(View.GONE);
                        }
                    }
                });

    }

    private void showPost(String id, String email, String distance) {
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