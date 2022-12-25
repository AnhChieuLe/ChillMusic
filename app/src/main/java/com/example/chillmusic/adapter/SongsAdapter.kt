package com.example.chillmusic.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chillmusic.fragment.listsongs.ListSongsFragment

class SongsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount() = 3
    val listSongFragment = ListSongsFragment()

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> ListSongsFragment()
            1 -> ListSongsFragment()
            2 -> ListSongsFragment()
            else -> ListSongsFragment()
        }
    }
}