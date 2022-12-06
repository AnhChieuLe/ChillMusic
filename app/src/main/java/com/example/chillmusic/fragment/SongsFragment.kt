package com.example.chillmusic.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.chillmusic.R
import com.example.chillmusic.adapter.SongsAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class SongsFragment : Fragment() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_songs, container, false)

        tabLayout = view.findViewById(R.id.songs_tab_layout)

        viewPager2 = view.findViewById(R.id.songs_view_pager2)
        viewPager2.adapter = SongsAdapter(this)

        val tabConfigurationStrategy = {tab: TabLayout.Tab, position: Int ->
            when(position){
                0 -> tab.text = "Tất cả"
                1 -> tab.text = "Yêu thích"
                2 -> tab.text = "Gần đây"
            }
        }
        TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy).attach()

        return view
    }
}