package com.example.chillmusic.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.chillmusic.R
import com.example.chillmusic.databinding.FragmentDetailBinding
import com.example.chillmusic.model.Song
import java.io.File
import kotlin.math.pow

class DetailFragment : DialogFragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    companion object{
        private const val ARG_SONG = "SONG"
        fun newInstance(song: Song): DetailFragment{
            val fragment = DetailFragment()
            val bundle = Bundle()
            bundle.putSerializable(ARG_SONG, song)
            fragment.arguments = bundle

            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentDetailBinding.inflate(LayoutInflater.from(context))
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setContentView(binding.root)

        setInfo()
        setStyle()

        return dialog
    }

    @SuppressLint("SetTextI18n")
    private fun setInfo() {
        val song = arguments?.getSerializable(ARG_SONG) as Song? ?: return
        val file = File(song.path)
        val fileSize = file.length() / 1024.0 / 1024.0//File size in MB
        binding.apply {
            path.tvName.text = "Vị trí"
            title.tvName.text = "Tiêu đề"
            album.tvName.text = "Album"
            artist.tvName.text = "Nghệ sĩ"
            genre.tvName.text = "Thể loại"
            duration.tvName.text = "Thời lượng"
            size.tvName.text = "Kích thước"
            quality.tvName.text = "Bitrate"

            path.tvInfo.text = song.path
            title.tvInfo.text = song.title
            album.tvInfo.text = if(song.album == "") getString(R.string.unknown) else song.album
            artist.tvInfo.text = if(song.artist == "") getString(R.string.unknown) else song.artist
            genre.tvInfo.text = if(song.genre == "") getString(R.string.unknown) else song.genre
            duration.tvInfo.text = song.strDuration
            quality.tvInfo.text = getString(R.string.bitrate_in_kbps, song.bitrate/1000)
            size.tvInfo.text = getString(R.string.file_size_in_MB, fileSize)
        }
    }

    private fun setStyle(){
        val song = arguments?.getSerializable(ARG_SONG) as Song? ?: return
        val style = song.style ?: return
        fun TextView.setStyle(){
            this.setTextColor(style.contentColor)
        }
        binding.apply {
            root.setBackgroundColor(style.backgroundColor)

            tvDetail.setStyle()

            path.tvName.setStyle()
            path.tvInfo.setStyle()

            title.tvName.setStyle()
            title.tvInfo.setStyle()

            album.tvName.setStyle()
            album.tvInfo.setStyle()

            artist.tvName.setStyle()
            artist.tvInfo.setStyle()

            genre.tvInfo.setStyle()
            genre.tvName.setStyle()

            duration.tvInfo.setStyle()
            duration.tvName.setStyle()

            size.tvInfo.setStyle()
            size.tvName.setStyle()

            quality.tvInfo.setStyle()
            quality.tvName.setStyle()
        }

    }
}