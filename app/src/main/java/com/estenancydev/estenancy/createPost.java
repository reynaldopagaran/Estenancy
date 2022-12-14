package com.estenancydev.estenancy;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.estenancydev.estenancy.Adapters.ImagesAdapter;
import com.example.estenancy.R;
import com.github.paolorotolo.expandableheightlistview.ExpandableHeightGridView;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shawnlin.numberpicker.NumberPicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import it.beppi.tristatetogglebutton_library.TriStateToggleButton;


public class createPost extends Fragment {

    EditText post_title, monthly_payment, reservation_fee, location, description;
    Button addPost;
    String address;
    String stat = "Available";
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
    private List<String> appointment;
    private List<String> removedAppointment;
    private List<String> addedAppointment;
    private ArrayList<Uri> uriPhotos;
    TriStateToggleButton triStateToggleButton;
    String id;
    ImageButton book;
    ExpandableHeightGridView appointments;
    SimpleDateFormat fmt = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a");
    SimpleDateFormat fmt2 = new SimpleDateFormat("h:mm a");
    NumberPicker numberPicker;
    int hour, minute, hour2, minute2;

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
        triStateToggleButton = v.findViewById(R.id.switch_avail);
        book = v.findViewById(R.id.book);
        appointments = v.findViewById(R.id.appointment_list);
        numberPicker = v.findViewById(R.id.numberPick);

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
        appointment = new ArrayList<>();
        removedAppointment = new ArrayList<>();
        addedAppointment = new ArrayList<>();
        //method calls

        stat();
        openMaps();
        //LatLongBundle();
        loadProfilePhotoAndName();
        setAddPost();
        openCalendar();
        removeDate();

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
    public void openCalendar() {
        final Calendar currentDate = Calendar.getInstance();
        final Calendar currentDate2 = Calendar.getInstance();
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                            TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener()
                            {
                                @Override
                                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
                                {
                                    hour = selectedHour;
                                    minute = selectedMinute;
                                    TimePickerDialog.OnTimeSetListener onTimeSetListener2 = new TimePickerDialog.OnTimeSetListener()
                                    {
                                        @Override
                                        public void onTimeSet(TimePicker timePicker2, int selectedHour2, int selectedMinute2)
                                        {
                                            hour2 = selectedHour2;
                                            minute2 = selectedMinute2;
                                            currentDate.set(year, month, dayOfMonth, selectedHour, selectedMinute);
                                            currentDate2.set(year, month, dayOfMonth, selectedHour2, selectedMinute2);

                                            appointment.add(fmt.format(currentDate.getTime()) + " to " + fmt2.format(currentDate2.getTime()));

                                            Set<String> set = new HashSet<>(appointment);
                                            appointment.clear();
                                            appointment.addAll(set);
                                            appointments.setAdapter(null);
                                            Arrays.sort(appointment.toArray());

                                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                                    getContext(),
                                                    android.R.layout.simple_list_item_1,
                                                    appointment);

                                            appointments.setAdapter(arrayAdapter);
                                            appointments.setExpanded(true);

                                        }
                                    };

                                    TimePickerDialog timePickerDialog2 = new TimePickerDialog(getContext(), onTimeSetListener2, hour2, minute2, false);
                                    timePickerDialog2.setTitle("Select Ending Time");
                                    timePickerDialog2.show();

                                }
                            };

                            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), onTimeSetListener, hour, minute, false);
                            timePickerDialog.setTitle("Select Starting Time");
                            timePickerDialog.show();
                        }
                    }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
                }
            }
        });
    }

    public void removeDate(){
        appointments.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Remove Appointment Date");
                builder.setMessage("Are you sure to remove this appointment date?");

                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removedAppointment.add(appointment.get(position));
                        appointment.remove(position);
                        appointments.setAdapter(null);
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                getContext(),
                                android.R.layout.simple_list_item_1,
                                appointment);
                        appointments.setAdapter(arrayAdapter);
                        appointments.setExpanded(true);
                        Toast.makeText(getContext(), "Removed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                return true;
            }
        });
    }


    public void stat() {
        triStateToggleButton.setOnToggleChanged(new TriStateToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(TriStateToggleButton.ToggleStatus toggleStatus, boolean booleanToggleStatus, int toggleIntValue) {

                switch (toggleStatus) {
                    case off:
                        stat = "Not Available";
                        break;
                    case on:
                        stat = "Available";
                        break;
                    default:
                        break;
                }
            }
        });
    }

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
            appointment.addAll(createPost.this.getArguments().getStringArrayList("appointments"));
            uriId.addAll(createPost.this.getArguments().getStringArrayList("uriId"));
            removedId.addAll(createPost.this.getArguments().getStringArrayList("removedId"));
            id = createPost.this.getArguments().getString("id");
            stat = createPost.this.getArguments().getString("stat");
            numberPicker.setValue(createPost.this.getArguments().getInt("nob"));


            appointments.setAdapter(null);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    getContext(),
                    android.R.layout.simple_list_item_1,
                    appointment);
            appointments.setAdapter(arrayAdapter);
            appointments.setExpanded(true);


            ArrayList<Parcelable> uris =
                    createPost.this.getArguments().getParcelableArrayList("photos");
            for (Parcelable p : uris) {
                uriPhotos.add((Uri) p);
                setAdapter(uriPhotos, uriNames);
            }

            if (label.getText().toString().equals("Edit Post")) {
                addPost.setText("Edit Listing");
            } else if (label.getText().toString().equals("Create Post")) {
                addPost.setText("Create Listing");
            }

            if (stat.equals("Available")) {
                triStateToggleButton.setToggleOn();
            } else if (stat.equals("Not Available")) {

                triStateToggleButton.setToggleOff();
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
                                        stat = documentSnapshot.getString("status");
                                        numberPicker.setValue(Integer.parseInt(documentSnapshot.getString("nob")));

                                        if (stat.equals("Available")) {
                                            triStateToggleButton.setToggleOn();
                                        } else if (stat.equals("Not Available")) {
                                            triStateToggleButton.setToggleOff();
                                        }
                                    }
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

                //getAppointments
                db.collection("appointmentOnPost")
                        .document(id)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if (documentSnapshot != null && documentSnapshot.exists()) {

                                        for(int x = 0; x <documentSnapshot.getData().size(); x++){
                                            appointment.add(documentSnapshot.getString("appointment_"+x));
                                        }

                                        appointments.setAdapter(null);

                                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                                getContext(),
                                                android.R.layout.simple_list_item_1,
                                                appointment);

                                        appointments.setAdapter(arrayAdapter);
                                        appointments.setExpanded(true);
                                    }
                                } else {

                                }
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
                                                                public void onSuccess(Uri uri) {
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
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }
                            }
                        });

            } else if (createPost.this.getArguments().getString("label").equals("Create Post")) {
                label.setText("Create Post");
                addPost.setText("Create Listing");
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

                            if (removedId.size() == 0) {
                                uriId.add("site_image_" + String.valueOf(uriId.size() - 1));
                            } else {
                                for (int a = 0; a < removedId.size(); a++) {
                                    uriId.add(removedId.get(a));
                                }
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
            stat();
            pd.setTitle("Updating...");
            pd.show();
            db.collection("posts")
                    .document(id)
                    .update("address", location.getText().toString(),
                            "title_post", post_title.getText().toString(),
                            "monthly_payment", monthly_payment.getText().toString(),
                            "reservation_fee", reservation_fee.getText().toString(),
                            "coordinates", new GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude()),
                            "description", description.getText().toString(),
                            "status", stat,
                            "nob", String.valueOf(numberPicker.getValue()))
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

            //photos and categories
            db.collection("categories").document(id).delete();
            Map<String, Object> cat = new HashMap<>();
            for (int i = 0; i < uriPhotos.size(); i++) {
                cat.put(uriId.get(i), uriNames.get(i));
                try {
                    editImage(uriPhotos.get(i), id, uriId.get(i));
                } catch (Exception e) {
                    Log.d("tag", "rez: " + e);
                }

            }
            db.collection("categories").document(id).set(cat);

            //appointments
            db.collection("appointmentOnPost").document(id).delete();
            Map<String, Object> appointments = new HashMap<>();
            for (int i = 0; i < appointment.size(); i++) {
                appointments.put("appointment_"+i, appointment.get(i));

                for(int x = 0; x < removedAppointment.size(); x++){
                    if(appointment.get(i).equals(removedAppointment.get(x))){
                        removedAppointment.remove(x);
                    }
                }
            }

            db.collection("appointmentOnPost").document(id).set(appointments);

            for(int y = 0; y < removedAppointment.size(); y++){
                db.collection("appointmentOnPost")
                        .document(id)
                        .collection("booked")
                        .document(removedAppointment.get(y))
                        .delete();
            }
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
        } else if(appointment.size() == 0){
            Toast.makeText(getActivity(), "Please add appointment.",
                    Toast.LENGTH_SHORT).show();
        } else {
            //save data to firestore
            stat();
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
            post.put("status", stat);
            post.put("timeStamp", FieldValue.serverTimestamp());
            post.put("nob", String.valueOf(numberPicker.getValue()));
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

                            Map<String, Object> date = new HashMap<>();

                            for(int a = 0; a < appointment.size(); a++){
                                date.put("appointment_"+a, appointment.get(a));
                            }
                            db.collection("appointmentOnPost")
                                    .document(documentReference.getId())
                                    .set(date);

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
                outState.putInt("nob", numberPicker.getValue());
                outState.putParcelableArrayList("photos", uriPhotos);
                outState.putStringArrayList("names", (ArrayList<String>) uriNames);
                outState.putStringArrayList("appointments", (ArrayList<String>) appointment);
                outState.putStringArrayList("uriId", (ArrayList<String>) uriId);
                outState.putStringArrayList("removedId", (ArrayList<String>) removedId);
                if (label.getText().toString().equals("Edit Post")) {
                    outState.putString("label", "Edit Post");
                } else if (label.getText().toString().equals("Create Post")) {
                    outState.putString("label", "Create Post");
                }

                if (stat.equals("Available")) {
                    outState.putString("stat", "Available");
                } else if (stat.equals("Not Available")) {
                    outState.putString("stat", "Not Available");
                }

                mapsFragment.setArguments(outState);
                FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                transaction.replace(R.id.mainLayout, mapsFragment).addToBackStack("tag");
                transaction.commit();

            }
        });
    }

    public void setAdapter(ArrayList<Uri> uriPhotos, List<String> uriNames) {
        ImagesAdapter imagesAdapter = new ImagesAdapter(getContext(), label.getText().toString(), uriPhotos, uriNames, imagePost, id, uriId, removedId);
        imagePost.setAdapter(imagesAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        //disableTexts();
    }
}