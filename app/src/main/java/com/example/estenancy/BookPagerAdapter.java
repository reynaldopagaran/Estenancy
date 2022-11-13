package com.example.estenancy;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.estenancy.homeFragments.homeFragment;

public class BookPagerAdapter extends FragmentStateAdapter {
    public BookPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new currentBooking();
            case 1:
                return new completedBooking();
            default:
                return new homeFragment();
        }
    }

    @Override
    public int getItemCount(){
        return 2;
    }
}
