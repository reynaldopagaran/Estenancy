package com.example.estenancy;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.estenancy.homeFragments.homeFragment;
import com.example.estenancy.homeFragments.messageFragment;
import com.example.estenancy.homeFragments.notificationFragment;
import com.example.estenancy.homeFragments.profileFragment;
import com.example.estenancy.homeFragments.searchFragment;

public class MyViewPagerAdapter extends FragmentStateAdapter {
    public MyViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new homeFragment();
            case 1:
                return new searchFragment();
            case 2:
                return new messageFragment();
            case 3:
                return new notificationFragment();
            case 4:
                return new profileFragment();
            default:
                return new homeFragment();
        }
    }

    @Override
    public int getItemCount(){
        return 5;
    }
}
