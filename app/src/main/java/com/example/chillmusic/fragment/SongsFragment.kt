package com.example.chillmusic.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.chillmusic.R
import com.example.chillmusic.adapter.MainViewPagerAdapter
import com.example.chillmusic.adapter.SongsAdapter
import com.example.chillmusic.databinding.FragmentSongsBinding
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SongsFragment : Fragment() {
    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!
    lateinit var adapter: SongsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        setViewPager()

        return binding.root
    }

    fun setViewPager(){
        adapter = SongsAdapter(this)
        binding.viewPager2.adapter = adapter

        val tabConfigurationStrategy = {tab: TabLayout.Tab, position: Int ->
            when(position){
                0 -> tab.text = "Tất cả"
                1 -> tab.text = "Yêu thích"
                2 -> tab.text = "Gần đây"
            }
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager2, tabConfigurationStrategy).attach()
    }

    fun setStyle(){
        //adapter.listSongFragment.setStyle()
        Log.d("setStyle", "setStyle SongFragment")
    }
}