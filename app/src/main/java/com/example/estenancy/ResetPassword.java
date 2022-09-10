package com.example.estenancy;

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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ResetPassword extends Fragment {

    EditText et_reset;
    Button btn_reset;
    FirebaseAuth auth;


    public ResetPassword() {
        // Required empty public constructor
    }


    public static ResetPassword newInstance(String param1, String param2) {
        ResetPassword fragment = new ResetPassword();
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
        View v = inflater.inflate(R.layout.fragment_reset_password, container, false);

        et_reset = v.findViewById(R.id.et_email_reset);
        btn_reset = v.findViewById(R.id.btn_reset);
        auth = FirebaseAuth.getInstance();

        //method calls
        resetPass();

        return v;
    }

    public void resetPass(){
        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(et_reset.getText().toString().trim())){
                    et_reset.setError("Please enter your Email.");
                    et_reset.requestFocus();
                }else if(!Patterns.EMAIL_ADDRESS.matcher(et_reset.getText().toString()).matches()){
                    et_reset.setError("Please enter a correct email.");
                    et_reset.requestFocus();
                }else{
                    auth.sendPasswordResetEmail(et_reset.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getActivity(), "Check your email to reset your password.",
                                        Toast.LENGTH_LONG).show();

                                Login login = new Login();
                                FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                                transaction.replace(R.id.mainLayout, login).addToBackStack("tag");
                                transaction.commit();

                            }else{
                                Toast.makeText(getActivity(), "Error, please try again.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }
}