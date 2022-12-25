package com.example.chillmusic.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chillmusic.R
import com.example.chillmusic.`object`.AlbumManager
import com.example.chillmusic.adapter.AlbumAdapter
import com.example.chillmusic.databinding.FragmentPlaylistBinding
import com.example.chillmusic.model.MusicStyle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlaylistAddFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    lateinit var adapter: AlbumAdapter

    companion object{
        private const val ARG_ID= "song"
        private const val ARG_STYLE= "style"
        fun newInstance(id: Int, style: MusicStyle?): PlaylistAddFragment{
            val fragment = PlaylistAddFragment()
            val bundle = Bundle().apply {
                putInt(ARG_ID, id)
                putSerializable(ARG_STYLE, style)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        _binding = FragmentPlaylistBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        setListPlaylist()
        setStyle()

        return dialog
    }

    private fun setListPlaylist() {
        val listAlbum = AlbumManager.getListAlbum(requireContext())
        binding.rcvAlbum.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = AlbumAdapter(requireContext())
        adapter.onItemClick = {position, _ ->
            val id = arguments?.getInt(ARG_ID) ?: 0
            val album = listAlbum[position]
            if(!album.listSong.contains(id))
                album.listSong.add(id)
            adapter.notifyItemChanged(position)
            AlbumManager.updateAlbum(requireContext(), album)
            dialog?.dismiss()
        }
        adapter.list = listAlbum
        binding.rcvAlbum.adapter = adapter
    }

    private fun setStyle(){
        binding.tvYourAlbums.text = getString(R.string.add_playlist)
        binding.cardAdd.visibility = View.GONE

        val style = arguments?.getSerializable(ARG_STYLE) as MusicStyle?
        adapter.style = style
        style?.let {
            binding.root.setBackgroundColor(it.backgroundColor)
            binding.tvYourAlbums.setTextColor(style.contentColor)
        }
    }
}