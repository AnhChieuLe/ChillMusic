package com.example.chillmusic.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chillmusic.fragment.AlbumsFragment
import com.example.chillmusic.fragment.SettingFragment
import com.example.chillmusic.fragment.SongsFragment
import com.example.chillmusic.fragment.listsongs.ListSongsFragment
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song

class MainViewPagerAdapter(song: Song?, fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    val listSongsFragment = ListSongsFragment.newInstance(song)
    val albumFragment = AlbumsFragment.newInstance(song?.style)
    val settingFragment = SettingFragment()


    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> listSongsFragment
            1 -> albumFragment
            2 -> settingFragment
            else -> SongsFragment()
        }
    }

    override fun getItemCount() = 3
}