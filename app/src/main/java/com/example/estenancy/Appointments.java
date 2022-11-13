package com.example.estenancy;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.github.paolorotolo.expandableheightlistview.ExpandableHeightGridView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


public class Appointments extends Fragment {

    TabLayout tab_book;
    ViewPager2 book_pager;
    BookPagerAdapter bookPagerAdapter;
    String id;


    public Appointments() {
        // Required empty public constructor
    }

    public static Appointments newInstance(String param1, String param2) {
        Appointments fragment = new Appointments();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_appointments, container, false);

        tab_book = v.findViewById(R.id.tab_book);
        book_pager = v.findViewById(R.id.book_pager);
        bookPagerAdapter = new BookPagerAdapter(getActivity());
        book_pager.setAdapter(bookPagerAdapter);
        book_pager.setUserInputEnabled(false);
        pager();

        return v;
    }

    public void pager(){
        tab_book.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                book_pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        book_pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tab_book.getTabAt(position).select();
            }
        });
    }
}