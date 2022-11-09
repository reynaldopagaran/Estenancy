package com.example.estenancy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;


import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import proj.me.bitframe.BeanBitFrame;
import proj.me.bitframe.BeanImage;
import proj.me.bitframe.FrameCallback;
import proj.me.bitframe.ImageType;
import proj.me.bitframe.ViewFrame;
import proj.me.bitframe.helper.FrameType;

public class Post extends Fragment {

    CircleImageView circleImageView;
    TextView name, title, time, desc, m, r, address_post, status;
    Button msg, book, nav, view_profile, reservation_payment;
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
    List<String> uriNames;
    List<String> uriId;
    Button seeMore;
    GeoPoint latLng;
    String[] items;
    String appointment_name = "", appointment_date = "", viewAppoint = "";
    boolean isBooked = false;
    String title1 = "";
    String addres1 = "";

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
        uriNames = new ArrayList<>();
        uriId = new ArrayList<>();


        circleImageView = v.findViewById(R.id.profile);
        name = v.findViewById(R.id.name1);
        title = v.findViewById(R.id.title);
        time = v.findViewById(R.id.timeStamp1);
        desc = v.findViewById(R.id.desc);
        m = v.findViewById(R.id.monthly_payment);
        r = v.findViewById(R.id.reservation_fee);
        viewFrame = v.findViewById(R.id.view_frame);
        address_post = v.findViewById(R.id.address_post);
        status = v.findViewById(R.id.status);
        seeMore = v.findViewById(R.id.seeMore);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            desc.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        //buttons
        msg = v.findViewById(R.id.msg_btn);
        book = v.findViewById(R.id.book_post);
        nav = v.findViewById(R.id.map_post);
        view_profile = v.findViewById(R.id.profile_btn);
        reservation_payment = v.findViewById(R.id.reservation_btnx);


        id = Post.this.getArguments().getString("id_from_card");
        email = Post.this.getArguments().getString("email");


        //method calls
        getImages();
        displayPost();
        loadAuthor();
        seeMore();
        setView_profile();

        //buttons
        navigate();
        setMsg();
        setBook();
        // buttonsVisibility(v);
        return v;
    }

    public void toasted(String m) {
        Toast.makeText(getActivity(), String.valueOf(m),
                Toast.LENGTH_SHORT).show();
    }

    public void setBook() {

        //getName of Current user
        db.collection("users")
                .document(mAuth.getCurrentUser().getEmail())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                appointment_name = documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName");

                            }
                        }
                    }
                });


        //get appointments
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if user already booked

                db.collection("appointmentOnPost").document(id).collection("booked").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (int y = 0; y < queryDocumentSnapshots.getDocuments().size(); y++) {
                            try {
                                if (appointment_name.equals(queryDocumentSnapshots.getDocuments().get(y).getData().get(mAuth.getCurrentUser().getEmail()).toString())) {
                                    viewAppoint = queryDocumentSnapshots.getDocuments().get(y).getId();
                                    isBooked = true;
                                }
                            } catch (Exception e) {

                            }
                        }

                        if (isBooked) {
                            viewAppointment();
                            isBooked = false;
                        } else {
                            appoint();
                        }
                    }
                });

            }
        });
    }

    public void removeAppointment() {
        db.collection("appointmentOnPost")
                .document(id)
                .collection("booked")
                .document(viewAppoint)
                .update(mAuth.getCurrentUser().getEmail(), FieldValue.delete())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        toasted("asd");
                    }
                });
    }

    public void viewAppointment() {

        //get title and address
        db.collection("posts")
                .document(id)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                title1 = documentSnapshot.getString("title_post");
                                addres1 = documentSnapshot.getString("address");
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Your Appointment is on");
        builder.setMessage(viewAppoint);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setNegativeButton("Add to calendar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent calIntent = new Intent(Intent.ACTION_INSERT);
                calIntent.setData(CalendarContract.Events.CONTENT_URI);
                calIntent.putExtra(CalendarContract.Events.TITLE, title1);
                calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, addres1);
                final Calendar startTime = Calendar.getInstance();
                String myDate[] = viewAppoint.split(" ");
                String myTime[] = myDate[4].split(":");
                String removedDay = String.valueOf(myDate[1]).replace(",", "");
                int month = 0;
                switch (myDate[0]) {
                    case "Jan":
                        month = 0;
                        break;
                    case "Feb":
                        month = 1;
                        break;
                    case "Mar":
                        month = 2;
                        break;
                    case "Apr":
                        month = 3;
                        break;
                    case "May":
                        month = 4;
                        break;
                    case "Jun":
                        month = 5;
                        break;
                    case "Jul":
                        month = 6;
                        break;
                    case "Aug":
                        month = 7;
                        break;
                    case "Sep":
                        month = 8;
                        break;
                    case "Oct":
                        month = 9;
                        break;
                    case "Nov":
                        month = 10;
                        break;
                    case "Dec":
                        month = 11;
                        break;
                }

                int hr = Integer.parseInt(myTime[0]);

                if(myDate[5].equals("pm")){
                    switch (Integer.parseInt(myTime[0]))
                    {
                        case 1:
                            hr = 13;
                            break;
                        case 2:
                            hr = 14;
                            break;
                        case 3:
                            hr = 15;
                            break;
                        case 4:
                            hr = 16;
                            break;
                        case 5:
                            hr = 17;
                            break;
                        case 6:
                            hr = 18;
                            break;
                        case 7:
                            hr = 19;
                            break;
                        case 8:
                            hr = 20;
                            break;
                        case 9:
                            hr = 21;
                            break;
                        case 10:
                            hr = 22;
                            break;
                        case 11:
                            hr = 23;
                            break;
                        case 12:
                            hr = 24;
                            break;
                    }
                }




                int year = Integer.parseInt(myDate[2]);
                int dayOfMonth = Integer.parseInt(removedDay);

                int min = Integer.parseInt(myTime[1]);
                startTime.set(year, month, dayOfMonth, hr,min);

                calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                        startTime.getTimeInMillis());
                startActivity(calIntent);
            }
        }).setNeutralButton("Remove Appointment", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeAppointment();
            }
        });

        builder.show();
    }

    public void appoint() {
        db.collection("appointmentOnPost")
                .document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()) {

                                items = new String[documentSnapshot.getData().size()];

                                for (int x = 0; x < documentSnapshot.getData().size(); x++) {
                                    items[x] = documentSnapshot.getString("appointment_" + x);
                                }
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());

                                alertDialog.setTitle("Choose an appointment");
                                // alertDialog.setMessage("Here are the list of appointments");
                                int checkedItem = 0;

                                appointment_date = items[checkedItem];

                                alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        appointment_date = items[which];

                                    }
                                }).setPositiveButton("Book", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        //get full name
                                        db.collection("users")
                                                .document(mAuth.getCurrentUser().getEmail())
                                                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            DocumentSnapshot documentSnapshot = task.getResult();
                                                            if (documentSnapshot != null && documentSnapshot.exists()) {

                                                                Map<String, Object> data = new HashMap<>();
                                                                data.put(mAuth.getCurrentUser().getEmail(), documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName"));
                                                                db.collection("appointmentOnPost")
                                                                        .document(id)
                                                                        .collection("booked")
                                                                        .document(appointment_date)
                                                                        .set(data, SetOptions.merge());

                                                                Toast.makeText(getActivity(), "Booked successfully.",
                                                                        Toast.LENGTH_SHORT).show();

                                                                appointment_date = "";
                                                            }
                                                        }
                                                    }
                                                });
                                    }
                                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        appointment_date = "";
                                    }
                                });

                                alertDialog.show();

                            }
                        } else {

                        }
                    }
                });
    }

    public void setMsg() {
        msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void setView_profile() {
        view_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ViewProfile viewProfile = new ViewProfile();
                Bundle bundle = new Bundle();
                bundle.putString("email", email);
                viewProfile.setArguments(bundle);
                FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                transaction.replace(R.id.mainLayout, viewProfile).addToBackStack("tag");
                transaction.commit();

            }
        });
    }

    public void navigate() {
        nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Navigation");
                builder.setMessage(address_post.getText() + " \n\nDo you want to start the navigation?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.google.android.apps.maps");
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("google.navigation:q=" + latLng.getLatitude() + "," + latLng.getLongitude()));
                        startActivity(intent);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "Cancelled.",
                                Toast.LENGTH_LONG).show();
                    }
                });

                builder.show();


            }
        });
    }

    public void getImages() {
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
                                                            uriNames.add(documentSnapshot.getString(item.getName()));
                                                            uriString.add(uri.toString());
                                                            BeanBitFrame beanBitFrame = new BeanBitFrame();
                                                            beanBitFrame.setWidth(500);
                                                            beanBitFrame.setHeight(200);
                                                            beanBitFrame.setImageLink(uri.toString());
                                                            beanBitFrame.setLoaded(true);
                                                            beanImageList.add(beanBitFrame);
                                                            showBitFrames();
                                                        }
                                                    });
                                                }
                                                buttonsVisibility();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getActivity(), e.toString(),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        });
                                //

                            }
                        }
                    }
                });
    }

    public void seeMore() {
        seeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageViewer viewer = new ImageViewer();
                Bundle bundle = new Bundle();
                ArrayList<String> strings = new ArrayList<>();
                strings.addAll(uriString);

                ArrayList<String> names = new ArrayList<>();
                names.addAll(uriNames);

                bundle.putStringArrayList("uri", strings);
                bundle.putStringArrayList("names", names);
                viewer.setArguments(bundle);
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.mainLayout, viewer).addToBackStack("tag");
                transaction.commit();
            }
        });
    }

    public void showBitFrames() {
        viewFrame.showBitFrame(beanImageList, new FrameCallback() {
            @Override
            public void imageClick(ImageType imageType, int imagePosition, String imageLink, ViewFrame actionableViewFrame) {

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
        }, FrameType.FRAMED);
    }

    public void loadAuthor() {
        db.collection("users")
                .document(email)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                String namex = documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName");
                                name.setText(namex);
                                //start of profile pic
                                storageReference = FirebaseStorage.getInstance().getReference().child("profilePhoto/" + email);
                                try {
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
                                } catch (Exception e) {

                                }

                                //end of profile pic
                            }
                        }

                    }
                });
    }

    public void displayPost() {

        db.collection("posts")
                .whereEqualTo("id", id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                title.setText(document.getString("title_post"));
                                latLng = document.getGeoPoint("coordinates");

                                if (document.getString("status").equals("Available")) {
                                    status.setText("Available.");
                                    status.setTextColor(Color.parseColor("#01DA15"));

                                    msg.setVisibility(View.VISIBLE);
                                    book.setVisibility(View.VISIBLE);
                                    nav.setVisibility(View.VISIBLE);
                                    reservation_payment.setVisibility(View.VISIBLE);

                                } else {
                                    status.setText("Not Available.");
                                    status.setTextColor(Color.parseColor("#FD0000"));

                                    msg.setVisibility(View.GONE);
                                    book.setVisibility(View.GONE);
                                    nav.setVisibility(View.GONE);
                                    reservation_payment.setVisibility(View.GONE);
                                }

                                m.setText("Monthly payment: " + pesoFormat.format(Integer.parseInt(document.getString("monthly_payment"))));
                                r.setText("Reservation Fee: " + pesoFormat.format(Integer.parseInt(document.getString("reservation_fee"))));
                                address_post.setText("Address: " + document.getString("address"));
                                desc.setText(document.getString("description"));
                                Timestamp timeStampFire = document.getTimestamp("timeStamp");
                                Date date = timeStampFire.toDate();
                                String timeStamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(date);
                                time.setText(timeStamp);
                            }
                        } else {

                        }
                    }
                });
    }

    public void buttonsVisibility() {
        if (!email.equals(firebaseUser.getEmail())) {
            msg.setVisibility(View.VISIBLE);
            book.setVisibility(View.VISIBLE);
            view_profile.setVisibility(View.VISIBLE);
            reservation_payment.setVisibility(View.VISIBLE);
        } else {
            msg.setVisibility(View.GONE);
            book.setVisibility(View.GONE);
            view_profile.setVisibility(View.GONE);
            reservation_payment.setVisibility(View.GONE);
        }
    }

}