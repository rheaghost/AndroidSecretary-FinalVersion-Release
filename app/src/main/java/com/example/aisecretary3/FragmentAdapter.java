package com.example.aisecretary3;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FragmentAdapter extends FragmentStateAdapter {

    public FragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {

            case 0: return new ChatFragment();  //Your main chat screen
            case 1: return new SaveTabFragment();
            case 2: return new PersonaFragment();   // Screen 3 (NEW!) persona fragment 2606032218

            default: return new ChatFragment();   //reverted 2601191939

        }
    }

    @Override
    public int getItemCount() {
        //return 2; // Total number of tabs
        return 3; //including personafragment 2606032218
    }
}