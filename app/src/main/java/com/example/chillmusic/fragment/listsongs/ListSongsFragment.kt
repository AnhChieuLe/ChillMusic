package com.example.chillmusic.fragment.listsongs

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chillmusic.`object`.ListSongManager
import com.example.chillmusic.activity.MainActivity
import com.example.chillmusic.activity.MusicPlayerActivity
import com.example.chillmusic.adapter.ListSongsAdapter
import com.example.chillmusic.databinding.FragmentListSongsBinding
import com.example.chillmusic.fragment.BottomMenuFragment
import com.example.chillmusic.model.CustomList
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song
import com.example.chillmusic.service.MusicPlayerService

class ListSongsFragment : Fragment() {
    private var _binding: FragmentListSongsBinding? = null
    private val binding get() = _binding!!
    lateinit var adapter : ListSongsAdapter
    private val mainActivity get() = activity as MainActivity

    companion object{
        private const val ARG_SONG = "SONG"
        fun newInstance(song: Song?): ListSongsFragment{
            val fragment = ListSongsFragment()
            val bundle = Bundle().apply {
                putSerializable(ARG_SONG, song)
            }
            Log.d("bundle", bundle.size().toString() + "bytes")
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentListSongsBinding.inflate(inflater, container, false)
        val song = arguments?.getSerializable(ARG_SONG) as Song?
        setRecyclerview(song)
        return binding.root
    }

    private fun setRecyclerview(song: Song?){
        adapter = ListSongsAdapter(requireContext())

        adapter.onItemClick = {
            if(mainActivity.service.position != it)
                startMusicService(adapter.listSong, it)
            else{
                val intent = Intent(requireContext(), MusicPlayerActivity::class.java)
                startActivity(intent)
            }
        }

        adapter.actionMore = {
            val bottomMenu = BottomMenuFragment.newInstance(it,
                if(mainActivity.service.isInitialized)
                    mainActivity.service.song.style
                else
                    null
            )
            bottomMenu.show(parentFragmentManager, bottomMenu.tag)
        }
        adapter.listSong = ListSongManager.listSong
        setStyle(song)
        binding.rcvListSong.adapter = adapter
    }

    fun setStyle(song: Song?){
        val position = adapter.listSong.indexOf(song)
        adapter.setStyle(song?.style, position)
    }

    private fun startMusicService(_listSong: List<Song>, _position: Int){
        val intent = Intent(requireContext(), MusicPlayerService::class.java)
        activity?.startService(intent)
        with(mainActivity.service){
            playListName = "Tất cả bản nhạc"
            listSong = _listSong.toMutableList()
            position = _position
            startMusic()
        }
    }
}