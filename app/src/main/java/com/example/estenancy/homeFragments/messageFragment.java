package com.example.estenancy.homeFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.estenancy.R;


public class messageFragment extends Fragment {



    public messageFragment() {
        // Required empty public constructor
    }


    public static messageFragment newInstance(String param1, String param2) {
        messageFragment fragment = new messageFragment();
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
        View v = inflater.inflate(R.layout.fragment_message, container, false);

        return v;
    }
}