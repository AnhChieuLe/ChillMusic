package com.example.chillmusic.fragment.musicplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chillmusic.`object`.ListSongManager
import com.example.chillmusic.activity.MusicPlayerActivity
import com.example.chillmusic.adapter.ListSongsAdapter
import com.example.chillmusic.databinding.FragmentMusicPlaylistBinding
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song

class MusicPlaylistFragment : Fragment() {
    private var _binding: FragmentMusicPlaylistBinding? = null
    private val binding get() = _binding!!

    lateinit var adapter : ListSongsAdapter

    private val musicActivity: MusicPlayerActivity get() = (activity as MusicPlayerActivity)
    val listSong: List<Song> get() = musicActivity.service.listSong
    val style: MusicStyle? get() = musicActivity.service.song.style

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMusicPlaylistBinding.inflate(inflater, container, false)

        setPlaylist()
        setStyle()
        setEvent()

        return binding.root
    }

    private fun setEvent() {
        adapter.onItemClick = {
            if(musicActivity.service.position != it){
                musicActivity.service.position = it
                musicActivity.service.startMusic()
            }
        }
    }

    override fun onResume() {
        binding.rcvPlaylist.scrollToPosition(musicActivity.service.position)
        super.onResume()
    }

    private fun setPlaylist() {
        binding.rcvPlaylist.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = ListSongsAdapter(requireContext())
        adapter.listSong = ListSongManager.getSongFromID(listSong.map { it.id })
        binding.rcvPlaylist.adapter = adapter
    }

    fun setStyle(){
        adapter.setStyle(style, musicActivity.service.position)
    }
}