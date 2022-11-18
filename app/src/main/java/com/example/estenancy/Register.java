package com.example.estenancy;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Register extends Fragment {

    EditText et_fname, et_lname, et_email, et_password, et_confirm_password,et_age, gcash_name, gcash_number;
    Button btn_create_account;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    Spinner gender;

    public Register() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Register newInstance(String param1, String param2) {
        Register fragment = new Register();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_register, container, false);

        et_fname = v.findViewById(R.id.et_fname);
        et_lname = v.findViewById(R.id.et_lname);
        et_email = v.findViewById(R.id.et_email);
        et_password = v.findViewById(R.id.et_password);
        et_confirm_password = v.findViewById(R.id.et_confirmPassword);
        btn_create_account = v.findViewById(R.id.btn_create_account);
        et_age = v.findViewById(R.id.et_age);
        gender = v.findViewById(R.id.sp_gender);
        gcash_name = v.findViewById(R.id.gcash_name);
        gcash_number = v.findViewById(R.id.gcash_number);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        //method calls
        setBtn_create_account();
        populateSpinner();

        return v;
    }

    public void populateSpinner(){
        List<String> spinnerArray =  new ArrayList<String>();
        spinnerArray.add("Male");
        spinnerArray.add("Female");
        spinnerArray.add("I'd rather not say");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
               getContext(), android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(adapter);
    }

    public void setBtn_create_account(){
        btn_create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(et_age.getText().toString())){
                    et_age.setError("Please enter your Age.");
                    et_age.requestFocus();
                }else if(TextUtils.isEmpty(et_fname.getText().toString())){
                    et_fname.setError("Please enter your First Name.");
                    et_fname.requestFocus();
                }else if(TextUtils.isEmpty(et_lname.getText().toString())){
                    et_lname.setError("Please enter your Last Name.");
                    et_lname.requestFocus();
                }else if(TextUtils.isEmpty(gcash_name.getText().toString())){
                    gcash_name.setError("Please enter your GCash Name.");
                    gcash_name.requestFocus();
                }else if(TextUtils.isEmpty(gcash_number.getText().toString())){
                    gcash_number.setError("Please enter your GCash Number.");
                    gcash_number.requestFocus();
                }else if(TextUtils.isEmpty(et_email.getText().toString())){
                    et_email.setError("Please enter your Email.");
                    et_email.requestFocus();
                }else if(TextUtils.isEmpty(et_password.getText().toString())){
                    et_password.setError("Password Required.");
                    et_password.requestFocus();
                }else if(TextUtils.isEmpty(et_confirm_password.getText().toString())){
                    et_confirm_password.setError("Password Required.");
                    et_confirm_password.requestFocus();
                }else if(et_password.getText().toString().length() < 6){
                    et_password.setError("Password is too short!.");
                    et_password.requestFocus();
                }else if(et_confirm_password.getText().toString().length() < 6){
                    et_confirm_password.setError("Password is too short!.");
                    et_confirm_password.requestFocus();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(et_email.getText().toString()).matches()){
                    et_email.setError("Please enter a correct email.");
                    et_email.requestFocus();
                }else if(!et_password.getText().toString().equals(et_confirm_password.getText().toString())){
                    et_confirm_password.setError("Password doesn't match!");
                    et_confirm_password.requestFocus();
                }else{

                    auth.createUserWithEmailAndPassword(et_email.getText().toString(), et_password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                //save data to firestore
                                Map<String, Object> users = new HashMap<>();
                                users.put("email", et_email.getText().toString());
                                users.put("firstName", et_fname.getText().toString());
                                users.put("lastName", et_lname.getText().toString());
                                users.put("age", et_age.getText().toString());
                                users.put("gender", gender.getSelectedItem().toString());
                                users.put("gcash_name", gcash_name.getText().toString());
                                users.put("gcash_number", gcash_number.getText().toString());

                                db.collection("users").document(et_email.getText().toString())
                                        .set(users)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {

                                                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                                                user.sendEmailVerification();

                                                                Toast.makeText(getActivity(), "We sent a verification email, please check to verify your account.",
                                                                        Toast.LENGTH_LONG).show();

                                                                Login login = new Login();
                                                                FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                                                                transaction.replace(R.id.mainLayout, login).addToBackStack("tag");
                                                                transaction.commit();

                                                            }
                                                        })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getActivity(), "Error, please try again later.",
                                                                Toast.LENGTH_LONG).show();
                                                    }
                                                });
                            }
                        }
                    });
                }
            }
        });
    }
}