package com.example.chillmusic.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import com.example.chillmusic.R
import com.example.chillmusic.activity.MainActivity
import com.example.chillmusic.activity.UpdateMetaData
import com.example.chillmusic.databinding.FragmentMenuBinding
import com.example.chillmusic.model.CustomList
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song
import com.example.chillmusic.service.MusicPlayerService
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class BottomMenuFragment: BottomSheetDialogFragment() {
    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    lateinit var song: Song
    var style: MusicStyle? = null
    val mainActivity: MainActivity get() = (activity as MainActivity)
    val service: MusicPlayerService get() = mainActivity.service

    companion object{
        const val ARG_SONG = "SONG"
        const val ARG_STYLE = "STYLE"
        fun newInstance(song: Song, style: MusicStyle?): BottomMenuFragment{
            val fragment = BottomMenuFragment()
            val bundle = Bundle().apply {
                putSerializable(ARG_SONG, song)
                putSerializable(ARG_STYLE, style)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        _binding = FragmentMenuBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        song = arguments?.getSerializable(ARG_SONG) as Song
        style = arguments?.getSerializable(ARG_STYLE) as MusicStyle?

        setItemSelected()
        style?.let { setDialogStyle(it) }

        return dialog
    }

    private fun setItemSelected(){
        binding.navigation.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.next -> addToPlayNext()
                R.id.wait -> addToWaiting()
                R.id.playlist_add -> addToPlaylist()
                R.id.play_new -> startWithNewPlaylist()
                R.id.info -> showInfo()
                R.id.delete -> deleteFile()
            }
            dialog?.dismiss()

            true
        }
    }


    private fun addToPlayNext(){
        service.listSong.add(service.position, song)
        if(service.listSong.size == 1)
            service.startMusic()
    }

    private fun addToWaiting(){
        service.listSong.add(song)
        if(service.listSong.size == 1)
            service.startMusic()
    }

    private fun addToPlaylist(){
        val bottomSheetDialog = PlaylistAddFragment.newInstance(song.id, style)
        bottomSheetDialog.show(parentFragmentManager, bottomSheetDialog.tag)
    }

    private fun showInfo(){
        val detailDialog = DetailFragment.newInstance(song)
        detailDialog.show(parentFragmentManager, detailDialog.tag)
    }

    private fun startWithNewPlaylist(){
        startMusicService(listOf(song), 0)
    }

    private fun updateInfo(){
        val intent = Intent(requireContext(), UpdateMetaData::class.java)
        val bundle = Bundle().apply {
            putSerializable("song", song)
        }
        intent.putExtra("data", bundle)
        startActivity(intent)
    }

    private fun deleteFile(){
        val builder = AlertDialog.Builder(requireContext()).apply {
            setTitle("X??c nh???n x??a b??i h??t n??y")
            setMessage("File nh???c s??? b??? x??a v??nh vi???n v?? kh??ng th??? kh??i ph???c")
            setPositiveButton("X??a"){ _, _ ->
                val path = Paths.get(song.path)
                val result = Files.deleteIfExists(path)
                if(result)
                    Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(requireContext(), "False", Toast.LENGTH_SHORT).show()
            }
            setNegativeButton("H???y"){p0, p1 -> }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun setDialogStyle(style: MusicStyle){
        binding.navigation.setBackgroundColor(style.backgroundColor)
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val colors = intArrayOf(
            style.contentColor,
            style.titleColor
        )
        val myList = ColorStateList(states, colors)
        binding.navigation.itemTextColor = myList
        binding.navigation.itemIconTintList = myList
    }

    private fun startMusicService(listSong: List<Song>, position: Int = 0){
        val intent = Intent(requireContext(), MusicPlayerService::class.java)
        activity?.startService(intent)

        mainActivity.service.playListName = "Danh s??ch t??y ch???nh"
        mainActivity.service.listSong = listSong.toMutableList()
        mainActivity.service.position = position
        mainActivity.service.startMusic()
    }
}