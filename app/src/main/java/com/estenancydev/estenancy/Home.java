package com.estenancydev.estenancy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.estenancy.R;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;


public class Home extends Fragment {

    TabLayout tabLayout;
    ViewPager2 viewPager2;
    MyViewPagerAdapter myViewPagerAdapter;
    ImageButton logout;

    SharedPreferences sharedPreferences;
    private static final String SHARED_PREF_NAME = "mypref";

    public Home() {
        // Required empty public constructor
    }


    public static Home newInstance(String param1, String param2) {
        Home fragment = new Home();
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
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        sharedPreferences = getActivity().getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);

        tabLayout = v.findViewById(R.id.tab_layout);
        viewPager2 = v.findViewById(R.id.view_pager);
        myViewPagerAdapter = new MyViewPagerAdapter(getActivity());
        viewPager2.setAdapter(myViewPagerAdapter);
        logout = v.findViewById(R.id.logout);

        viewPager2.setUserInputEnabled(false);

        //method calls
        logout();
        pager();


        return v;
    }

    public void pager(){
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });
    }

    public void logout(){
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Logging Out")
                        .setMessage("Are you sure you want to logout?")

                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseAuth.getInstance().signOut();

                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.clear();
                                editor.commit();

                                Toast.makeText(getActivity(), "Logged out.",
                                        Toast.LENGTH_LONG).show();

                                Login login = new Login();
                                FragmentTransaction transaction = getFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_slide_right, R.anim.exit_slide_left, R.anim.enter_slide_left, R.anim.exit_slide_right);
                                transaction.replace(R.id.mainLayout, login);
                                transaction.commit();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        });
    }
}