package com.example.chillmusic.adapter

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chillmusic.databinding.ActivityMainBinding
import com.example.chillmusic.fragment.AlbumsFragment
import com.example.chillmusic.fragment.SettingFragment
import com.example.chillmusic.fragment.SongsFragment

class MainViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3;
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0->SongsFragment();
            1->AlbumsFragment();
            2->SettingFragment();
            else->SongsFragment();
        }
    }
}