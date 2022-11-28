package com.estenancydev.estenancy;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.estenancy.R;
import com.google.android.material.tabs.TabLayout;


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