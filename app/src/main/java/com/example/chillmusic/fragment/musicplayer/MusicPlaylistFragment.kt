package com.example.chillmusic.fragment.musicplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chillmusic.`object`.ListSongManager
import com.example.chillmusic.activity.MiniMusicPlayerActivity
import com.example.chillmusic.activity.MusicPlayerActivity
import com.example.chillmusic.adapter.ListSongsAdapter
import com.example.chillmusic.databinding.FragmentMusicPlaylistBinding
import com.example.chillmusic.fragment.BottomMenuFragment
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song

class MusicPlaylistFragment : Fragment() {
    private var _binding: FragmentMusicPlaylistBinding? = null
    private val binding get() = _binding!!

    lateinit var adapter : ListSongsAdapter

    val service get() = if(activity is MusicPlayerActivity)
        (activity as MusicPlayerActivity).service
    else
        (activity as MiniMusicPlayerActivity).service

    val listSong: List<Song> get() = service.listSong
    val style: MusicStyle? get() = service.song.style

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMusicPlaylistBinding.inflate(inflater, container, false)

        setPlaylist()
        setStyle()
        setEvent()

        return binding.root
    }

    private fun setEvent() {
        adapter.onItemClick = {
            if(service.position != it){
                service.position = it
                service.startMusic()
            }
        }
        adapter.actionMore = {
            val bottomMenu = BottomMenuFragment.newInstance(it,
                if(service.isInitialized)
                    service.song.style
                else
                    null
            )
            bottomMenu.show(parentFragmentManager, bottomMenu.tag)
        }
    }

    override fun onResume() {
        binding.rcvPlaylist.smoothScrollToPosition(service.position)
        super.onResume()
    }

    private fun setPlaylist() {
        adapter = ListSongsAdapter(requireContext())
        adapter.listSong = service.listSong
        binding.rcvPlaylist.adapter = adapter
    }

    fun setStyle(){
        adapter.setStyle(style, service.position)
    }
}