package com.example.estenancy;
import static com.mapbox.api.geocoding.v5.GeocodingCriteria.TYPE_POI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.module.http.HttpRequestUtil;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;
import com.mapbox.search.common.AsyncOperationTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import im.delight.android.location.SimpleLocation;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MapsFragment extends Fragment implements PermissionsListener {

    private static final int REQUEST_CODE_AUTOCOMPLETE = 7171;
    private static final int PLACE_SELECTION_REQUEST_CODE = 56789;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    private MapView mapView;
    TextView search, currentLocText;
    Button done;
    LatLng latLngDone;
    Point firstResultPoint;
    GeocodingCriteria geocodingCriteria;
    private AsyncOperationTask searchRequestTask;
    GeocodingCriteria.GeocodingTypeCriteria geocodingTypeCriteria;
    String token = "sk.eyJ1IjoiYW5kcm9tZWRhNyIsImEiOiJjbDg2YnZpa2IwNzk3M3VvaGN3ZnczNjcwIn0.axp5uCx677xYd9E6vxwWWA";
    private SimpleLocation simpleLocation;
    String bundleAddress, lat, longi;
    private Geocoder geocoder;
    List <Address> addresses;
    Address address;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        Mapbox.getInstance(getContext(), token);
        View v = inflater.inflate(R.layout.fragment_maps, container, false);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        search = v.findViewById(R.id.search);
        done = v.findViewById(R.id.btn_done);
        mapView = v.findViewById(R.id.mapView);
        currentLocText = v.findViewById(R.id.currentLoc);
        mapView.onCreate(savedInstanceState);
        HttpRequestUtil.setLogEnabled(false);

        simpleLocation = new SimpleLocation(getContext());
        geocoder = new Geocoder(getContext());

        //method calls

        showMap();
        searchLoc();
        setDone();
        return v;
    }



//METHODS

   public void getAddress(LatLng latLng){
       try {
           addresses = geocoder.getFromLocation(latLng.getLatitude(), latLng.getLongitude(), 1);
       } catch (IOException e) {
           e.printStackTrace();
       }
       address = addresses.get(0);
       bundleAddress =  address.getAddressLine(0);
   }

    public void setDone(){
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                lat = Location.convert(latLngDone.getLatitude(), Location.FORMAT_DEGREES);
                longi = Location.convert(latLngDone.getLongitude(), Location.FORMAT_DEGREES);

                createPost createPost = new createPost();
                Bundle bundle = new Bundle();
                bundle.putString("lat", lat);
                bundle.putString("longi", longi);
                bundle.putString("address", bundleAddress);

                bundle.putString("label", MapsFragment.this.getArguments().getString("label"));
                bundle.putString("id", MapsFragment.this.getArguments().getString("id"));
                bundle.putString("stat", MapsFragment.this.getArguments().getString("stat"));
                bundle.putString("title", MapsFragment.this.getArguments().getString("title"));
                bundle.putString("month", MapsFragment.this.getArguments().getString("month"));
                bundle.putString("res", MapsFragment.this.getArguments().getString("res"));
                bundle.putString("desc", MapsFragment.this.getArguments().getString("desc"));
                bundle.putParcelableArrayList("photos", MapsFragment.this.getArguments().getParcelableArrayList("photos"));
                bundle.putStringArrayList("appointments", MapsFragment.this.getArguments().getStringArrayList("appointments"));
                bundle.putStringArrayList("names", MapsFragment.this.getArguments().getStringArrayList("names"));
                bundle.putStringArrayList("uriId", MapsFragment.this.getArguments().getStringArrayList("uriId"));
                bundle.putStringArrayList("removedId", MapsFragment.this.getArguments().getStringArrayList("removedId"));

                createPost.setArguments(bundle);

                FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                transaction.replace(R.id.mainLayout, createPost);
                transaction.commit();

            }
        });
    }


@SuppressLint("MissingPermission")
    private void getLatLang() {
        if(ContextCompat.checkSelfPermission(
                getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getContext()
                , Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(getActivity(), new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION
                    }, REQUEST_CODE);
            return;

        }else{

            if (!simpleLocation.hasLocationEnabled()) {
                // ask the user to enable location access
                SimpleLocation.openSettings(getContext());
            }else{
                latLngDone = new LatLng(simpleLocation.getLatitude(), simpleLocation.getLongitude());
            }
        }
    }



    public void showMap(){
        getLatLang();
        try{
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mapboxMap) {

                   getAddress(latLngDone);

                    mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
                        @Override
                        public boolean onMapClick(@NonNull LatLng point) {
                            latLngDone = point;
                            getAddress(point);

                                mapboxMap.removeAnnotations();
                                mapboxMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(point))
                                        .title(address.getAddressLine(0)));

                            return true;
                        }
                    });


                        mapboxMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latLngDone))
                                .title(address.getAddressLine(0)));

                    mapboxMap.setStyle(Style.MAPBOX_STREETS);

                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(latLngDone))
                            .zoom(16)
                            .tilt(20)
                            .build();
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
                }
            });
        }catch (Exception e){
            Toast.makeText(getActivity(), "Oops, an error occurred. Please check your network and try again.",
                    Toast.LENGTH_LONG).show();
        }
    }


    public void searchLoc() {

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(token)
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .country("PH")
                                .geocodingTypes(TYPE_POI)
                                .build(PlaceOptions.MODE_CARDS))
                        .build(getActivity());
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }

    public void placePicker(){
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new PlacePicker.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken())
                        .placeOptions(
                                PlacePickerOptions.builder()
                                        .statingCameraPosition(new CameraPosition.Builder()
                                                .target(new LatLng(40.7544, -73.9862))
                                                .zoom(18)
                                                .build())
                                        .build())
                        .build(getActivity());
                startActivityForResult(intent, PLACE_SELECTION_REQUEST_CODE);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            //getting location name
            CarmenFeature feature = PlaceAutocomplete.getPlace(data);

            MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                    .accessToken(token)
                    .query(feature.text())
                    .build();

            currentLocText.setText(feature.text());

            mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                    List<CarmenFeature> results = response.body().features();
                    if (results.size() > 0) {
                        // Move Map Camera after search result
                        firstResultPoint = results.get(0).center();
                        mapView.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                                mapboxMap.setStyle(Style.MAPBOX_STREETS);
                                CameraPosition position = new CameraPosition.Builder()
                                        .target(new LatLng(firstResultPoint.latitude(), firstResultPoint.longitude()))
                                        .zoom(16)
                                        .tilt(20)
                                        .build();
                                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));

                                getAddress(new LatLng(firstResultPoint.latitude(), firstResultPoint.longitude()));

                                mapboxMap.removeAnnotations();
                                mapboxMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(firstResultPoint.latitude(), firstResultPoint.longitude()))
                                        .title(address.getAddressLine(0)));

                                latLngDone = new LatLng(firstResultPoint.latitude(), firstResultPoint.longitude());
                            }
                        });

                    } else {

                    }
                }

                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                    Toast.makeText(getActivity(), "Location not found.",
                            Toast.LENGTH_LONG).show();
                }
            });

        }

        //place pick
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (REQUEST_CODE){
            case REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
                break;
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

    }


    @Override
    public void onExplanationNeeded(List<String> list) {

    }

    @Override
    public void onPermissionResult(boolean b) {

    }

    @Override
    public void onResume() {
        super.onResume();
        simpleLocation.beginUpdates();
    }

    @Override
    public void onPause() {
        simpleLocation.endUpdates();
        super.onPause();
    }

}