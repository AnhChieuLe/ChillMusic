package com.example.chillmusic.fragment.musicplayer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chillmusic.R
import com.example.chillmusic.`object`.PreferencesManager
import com.example.chillmusic.activity.MiniMusicPlayerActivity
import com.example.chillmusic.activity.MusicPlayerActivity
import com.example.chillmusic.adapter.TrackAdapter
import com.example.chillmusic.api.API
import com.example.chillmusic.databinding.FragmentMusicLyricBinding
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song
import com.example.chillmusic.model.api.lyrics.LyricsResponse
import com.example.chillmusic.model.api.track.TrackContainer
import com.example.chillmusic.model.api.track.TrackResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MusicLyricFragment : Fragment() {
    private var _binding: FragmentMusicLyricBinding? = null
    private val binding get() = _binding!!

    val song: Song get() = if(activity is MusicPlayerActivity)
        (activity as MusicPlayerActivity).service.song
    else
        (activity as MiniMusicPlayerActivity).service.song

    lateinit var callTrack: Call<TrackResponse>
    lateinit var callLyric: Call<LyricsResponse>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMusicLyricBinding.inflate(inflater, container, false)

        setInfo()
        setStyle()
        return binding.root
    }

    fun setInfo() {
        binding.tvTitle.setText(song.title)
        binding.tvArtist.text = if (song.artist == "") getString(R.string.unknown) else song.artist
        setLyric(song.title, song.artist, song.style)
        binding.tvLyric.text = "Đang tìm kiếm lời bài hát"
        binding.rcvTrack.visibility = View.GONE
    }

    fun setStyle() {
        song.style?.let {
            binding.tvTitle.setTextColor(it.contentColor)
            binding.tvArtist.setTextColor(it.contentColor)
            binding.tvLyric.setTextColor(it.contentColor)
        }
    }

    private fun setListTrack(list: List<TrackContainer>, style: MusicStyle?, action: (Int) -> Unit){
        binding.rcvTrack.visibility = View.VISIBLE
        checkIfFragmentAttached {
            val adapter = TrackAdapter(requireContext(), style)
            adapter.onItemClick = action
            adapter.listTrack = list
            binding.rcvTrack.adapter = adapter
        }
    }

    private fun checkIfFragmentAttached(operation: Context.() -> Unit) {
        if (isAdded && context != null) {
            operation(requireContext())
        }
    }

    private fun setLyric(title: String, artist: String, style: MusicStyle?) {
        val callBack = object : Callback<LyricsResponse> {
            override fun onResponse(
                call: Call<LyricsResponse>,
                response: Response<LyricsResponse>
            ) {
                val result = response.body()
                binding.tvLyric.text = result?.message?.body?.lyrics?.lyrics_body
            }

            override fun onFailure(call: Call<LyricsResponse>, t: Throwable) {
                getTrackId(title, style)
            }
        }

        callLyric = API.apiService.getLyric(q_track = title, q_artist = artist)
        callLyric.enqueue(callBack)
    }

    private fun getTrackId(title: String, style: MusicStyle?) {
        val callBack = object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                if (!response.isSuccessful) return
                val result = response.body()
                val trackList = result?.message?.body?.track_list
                if (trackList != null && trackList.isNotEmpty()) {
                    setListTrack(trackList, style) {
                        getLyric(trackList[it].track.track_id)
                    }
                } else {
                    binding.tvLyric.text = getString(R.string.cannot_find_lyric)
                    //binding.tvLyric.text = response.toString()
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {
                binding.tvLyric.text = getString(R.string.cannot_find_lyric)
            }
        }
        val tit = title.substringAfter("(").substringBefore(")")
        checkIfFragmentAttached {
            val numberOfSong = PreferencesManager(requireContext()).numberOfSong
            callTrack = API.apiService.getTrack(q_track = tit, page_size = numberOfSong)
            callTrack.enqueue(callBack)
        }
    }

    private fun getLyric(trackId: String) {
        val callBack = object : Callback<LyricsResponse> {
            override fun onResponse(call: Call<LyricsResponse>, response: Response<LyricsResponse>) {
                if (!response.isSuccessful) return
                val result = response.body()
                binding.tvLyric.text = result?.message?.body?.lyrics?.lyrics_body
            }

            override fun onFailure(call: Call<LyricsResponse>, t: Throwable) {
                binding.tvLyric.text = getString(R.string.lyric_not_exist)
            }
        }
        callLyric = API.apiService.getLyric(track_id = trackId)
        callLyric.enqueue(callBack)
    }
}