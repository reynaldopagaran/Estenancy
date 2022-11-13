package com.example.estenancy;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.estenancy.Classes.Person;
import com.example.estenancy.Classes.PersonClass;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


public class currentBooking extends Fragment {

    Spinner spinner;
    RecyclerView list;
    String id;
    private FirebaseFirestore db;
    ArrayList<String> dates;
    ArrayList<Person> names;
    Button getAppoint;
    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "mypref";
    private FirebaseStorage storage;
    private StorageReference storageReference;
    PersonClass personClass;
    TextView no_appointment_text;

    public currentBooking() {
        // Required empty public constructor
    }


    public static currentBooking newInstance(String param1, String param2) {
        currentBooking fragment = new currentBooking();
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
        View v = inflater.inflate(R.layout.fragment_current_booking, container, false);


        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        id = sharedPreferences.getString("myId", null);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        spinner = v.findViewById(R.id.spin_dates);
        list = v.findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        dates = new ArrayList<>();
        names = new ArrayList<>();
        getAppoint = v.findViewById(R.id.getAppoint);
        no_appointment_text = v.findViewById(R.id.no_appointment_text);

        //method calls
        try {
            getAppointmentDates(id);
            populateList();
        } catch (Exception e) {

        }

        return v;
    }

    public void populateList() {
        getAppoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myList();
            }
        });
    }

    public void myList() {
        db.collection("appointmentOnPost")
                .document(id)
                .collection("booked")
                .document(spinner.getSelectedItem().toString())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot != null && documentSnapshot.exists()) {

                            Iterator iterator = documentSnapshot.getData().entrySet().iterator();

                            if(iterator.hasNext()){
                                no_appointment_text.setVisibility(View.GONE);
                                list.setVisibility(View.VISIBLE);
                                names.clear();
                                while (iterator.hasNext()) {
                                    Map.Entry me = (Map.Entry) iterator.next();
                                    //get image
                                    storageReference
                                            .child("profilePhoto/" + me.getKey() + "@gmail.com").getDownloadUrl()
                                            .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    names.add(new Person(task.getResult(), me.getValue().toString(), me.getKey().toString()));
                                                    personClass = new PersonClass(getContext(), names, spinner, new PersonClass.ItemClickListener() {
                                                        @Override
                                                        public void onItemClick(Person Person) {

                                                        }
                                                    });
                                                    list.setAdapter(personClass);
                                                    iterator.remove();
                                                }
                                            });

                                }
                            }else{
                                names.clear();
                                no_appointment_text.setVisibility(View.VISIBLE);
                                list.setVisibility(View.GONE);
                            }

                        }else{
                            names.clear();
                            no_appointment_text.setVisibility(View.VISIBLE);
                            list.setVisibility(View.GONE);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        no_appointment_text.setVisibility(View.VISIBLE);
                        list.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "x", Toast.LENGTH_SHORT).show();
                    }
                });

    }


    public void getAppointmentDates(String id) {
        db.collection("appointmentOnPost")
                .document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()) {

                                for (int x = 0; x < documentSnapshot.getData().size(); x++) {
                                    dates.add(documentSnapshot.getString("appointment_" + x));
                                }

                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                                        android.R.layout.simple_spinner_item, dates);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setAdapter(adapter);

                            }
                        } else {

                        }
                    }
                });
    }
}