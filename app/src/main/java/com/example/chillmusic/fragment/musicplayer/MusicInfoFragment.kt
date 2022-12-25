package com.example.chillmusic.fragment.musicplayer

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.chillmusic.activity.MusicPlayerActivity
import com.example.chillmusic.databinding.FragmentMusicInfoBinding
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song

class MusicInfoFragment : Fragment() {
    private var _binding: FragmentMusicInfoBinding? = null
    private val binding get() = _binding!!

    private val musicActivity: MusicPlayerActivity get() = (activity as MusicPlayerActivity)
    val song: Song get() = musicActivity.service.song
    val image: Bitmap get() = musicActivity.service.image

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMusicInfoBinding.inflate(inflater, container, false)

        setInfo()
        setStyle()

        return binding.root
    }

    fun setInfo(){
        binding.tvTitle.text = song.title
        binding.tvArtist.text = if(song.artist == "") "Không rõ" else song.artist
        binding.imgAlbumArt.setImageBitmap(image)
    }

    fun setStyle(){
        song.style?.let {
            binding.tvTitle.setTextColor(it.contentColor)
            binding.tvArtist.setTextColor(it.contentColor)
        }
    }
}