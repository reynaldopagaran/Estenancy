package com.example.estenancy;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.List;


public class Login extends Fragment {

    EditText et_email, et_pass;
    Button btn_create_account;
    private FirebaseAuth auth;
    TextView tv_register,tv_forgotpass;

    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_EMAIL = "username";


    public Login() {
        // Required empty public constructor
    }

    public static Login newInstance(String param1, String param2) {
        Login fragment = new Login();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_login, container, false);

        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String spName = sharedPreferences.getString(KEY_EMAIL, null);


        tv_register = v.findViewById(R.id.tv_register);
        et_email = v.findViewById(R.id.et_email_login);
        et_pass = v.findViewById(R.id.et_email_reset);
        btn_create_account = v.findViewById(R.id.btn_login);
        tv_forgotpass = v.findViewById(R.id.tv_forgotpass);
        auth = FirebaseAuth.getInstance();


        //method calls
        DexterPermissions();
        register();
        login();
        resetPass();


        return v;
    }

    public void DexterPermissions(){
        Dexter.withContext(getContext())
                .withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

                    }
                }).check();
    }

    public void login(){
        btn_create_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(et_email.getText().toString())){
                    et_email.setError("Please enter your Email.");
                    et_email.requestFocus();
                }else if(TextUtils.isEmpty(et_pass.getText().toString())){
                    et_pass.setError("Please enter your Password.");
                    et_pass.requestFocus();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(et_email.getText().toString()).matches()){
                    et_email.setError("Please enter a correct email.");
                    et_email.requestFocus();
                }else{
                    auth.signInWithEmailAndPassword(et_email.getText().toString(), et_pass.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                if(user.isEmailVerified()){
                                    Toast.makeText(getActivity(), "Login Successful.",
                                            Toast.LENGTH_LONG).show();

                                    //shared preferences
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString(KEY_EMAIL, et_email.getText().toString());
                                    editor.apply();

                                    //go to home
                                    Home home = new Home();
                                    FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                                    transaction.replace(R.id.mainLayout, home);
                                    transaction.commit();

                                }else{
                                    Toast.makeText(getActivity(), "Check your email to verify your account.",
                                            Toast.LENGTH_LONG).show();
                                }

                            }else if(!task.isSuccessful()){
                                Toast.makeText(getActivity(), "Login Error, please check your details.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }


    public void register(){
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Register register = new Register();
                FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                transaction.replace(R.id.mainLayout, register).addToBackStack("tag");
                transaction.commit();
            }
        });
    }

    public void resetPass(){
        tv_forgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResetPassword resetPassword = new ResetPassword();
                FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                transaction.replace(R.id.mainLayout, resetPassword).addToBackStack("tag");
                transaction.commit();
            }
        });
    }
}