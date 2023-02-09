package com.example.chillmusic.fragment.musicplayer

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.example.chillmusic.R
import com.example.chillmusic.`object`.PreferencesManager
import com.example.chillmusic.activity.MiniMusicPlayerActivity
import com.example.chillmusic.activity.MusicPlayerActivity
import com.example.chillmusic.databinding.FragmentMiniMusicInfoBinding
import com.example.chillmusic.databinding.FragmentMusicInfoBinding
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song
import com.example.chillmusic.service.NORMAL
import com.example.chillmusic.service.RANDOM
import com.example.chillmusic.service.REPEAT_ALL
import com.example.chillmusic.service.REPEAT_ONE
import java.text.SimpleDateFormat
import java.util.*

class MiniMusicInfoFragment : Fragment() {
    private var _binding: FragmentMiniMusicInfoBinding? = null
    private val binding get() = _binding!!

    val song: Song get() = (activity as MiniMusicPlayerActivity).service.song
    private val image: Bitmap get() = (activity as MiniMusicPlayerActivity).service.image
    private val service get() = (activity as MiniMusicPlayerActivity).service

    private val timer = Timer()
    val seekBarThread = Thread {
        timer.scheduleAtFixedRate(object : TimerTask(){
            override fun run() {
                binding.seekbar.post(Runnable {
                    binding.seekbar.max = service.mediaPlayer.duration
                    binding.seekbar.progress = service.mediaPlayer.currentPosition
                })
            }
        }, 0, 1000)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMiniMusicInfoBinding.inflate(inflater, container, false)

        setInfo()
        setStyle()
        setEvent()

        return binding.root
    }

    fun setStatus(){
        binding.imgPlayOrPause.setImageResource(if(service.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
    }

    private fun setEvent() {
        with(binding){
            imgPlayOrPause.setOnClickListener {
                if(service.isPlaying)
                    service.pauseMusic()
                else
                    service.resumeMusic()
            }

            imgNext.setOnClickListener {
                service.startNext()
            }

            imgPrevious.setOnClickListener {
                service.startPrevious()
            }

            seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    val max = p0?.max ?: 0
                    val current = p0?.progress ?: 0
                    val calendar = Calendar.getInstance()

                    calendar.timeInMillis = max.toLong()
                    binding.seekbarMax.text = Song.getStringDuration(max.toLong())

                    calendar.timeInMillis = current.toLong()
                    binding.seekbarCurrent.text = Song.getStringDuration(current.toLong())
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    p0?.let { service.mediaPlayer.seekTo(p0.progress) }
                }
            })
        }
    }

    fun setInfo(){
        binding.tvTitle.text = song.title
        binding.tvArtist.text = if(song.artist == "") "Không rõ" else song.artist
        binding.imgAlbumArt.setImageBitmap(image)

        if(seekBarThread.state == Thread.State.NEW && service.isInitialized){
            seekBarThread.start()
        }
    }

    fun setStyle(){
        song.style?.let {
            binding.tvTitle.setTextColor(it.contentColor)
            binding.tvArtist.setTextColor(it.contentColor)
        }

        with(binding){
            with(service.song.style!!){
                seekbarMax.setTextColor(contentColor)
                seekbarCurrent.setTextColor(contentColor)

                seekbar.progressDrawable.setTint(contentColor)
                val thumb = AppCompatResources.getDrawable(requireContext(), R.drawable.seekbar_thumb)
                thumb?.setTint(contentColor)
                seekbar.thumb = thumb

                DrawableCompat.setTint(imgPrevious.drawable.mutate(), contentColor)
                DrawableCompat.setTint(imgPlayOrPause.drawable.mutate(), contentColor)
                DrawableCompat.setTint(imgNext.drawable.mutate(), contentColor)
            }
        }
    }
}