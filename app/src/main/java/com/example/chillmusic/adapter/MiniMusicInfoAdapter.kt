package com.example.chillmusic.adapter

import android.graphics.Bitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chillmusic.fragment.musicplayer.MiniMusicInfoFragment
import com.example.chillmusic.fragment.musicplayer.MusicInfoFragment
import com.example.chillmusic.fragment.musicplayer.MusicLyricFragment
import com.example.chillmusic.fragment.musicplayer.MusicPlaylistFragment
import com.example.chillmusic.model.Song

class MiniMusicInfoAdapter(fragment: FragmentActivity): FragmentStateAdapter(fragment) {
    override fun getItemCount() = 3
    val fragmentPlaylist = MusicPlaylistFragment()
    val fragmentInfo = MiniMusicInfoFragment()
    val fragmentLyric = MusicLyricFragment()

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> fragmentPlaylist
            1 -> fragmentInfo
            2 -> fragmentLyric
            else -> fragmentInfo
        }
    }

    fun setInfo(){
        fragmentInfo.setInfo()
        fragmentLyric.setInfo()
    }

    fun setStyle(){
        fragmentPlaylist.setStyle()
        fragmentInfo.setStyle()
        fragmentLyric.setStyle()
    }
}