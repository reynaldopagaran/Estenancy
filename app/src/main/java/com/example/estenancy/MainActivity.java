package com.example.estenancy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "mypref";
    private static final String KEY_EMAIL = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String spName = sharedPreferences.getString(KEY_EMAIL, null);

        if (spName != null) {
            getSupportFragmentManager().beginTransaction().add(R.id.mainLayout,new Home()).commit();
        }else{
            getSupportFragmentManager().beginTransaction().add(R.id.mainLayout,new Login()).commit();
        }
    }
}