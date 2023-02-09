package com.example.chillmusic.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.chillmusic.R
import com.example.chillmusic.`object`.AlbumManager
import com.example.chillmusic.`object`.ListSongManager
import com.example.chillmusic.activity.AlbumActivity
import com.example.chillmusic.activity.MainActivity
import com.example.chillmusic.adapter.AlbumAdapter
import com.example.chillmusic.database.album.AlbumDatabase
import com.example.chillmusic.databinding.FragmentPlaylistBinding
import com.example.chillmusic.databinding.ItemAlbumBinding
import com.example.chillmusic.model.Album
import com.example.chillmusic.model.CustomList
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song
import com.example.chillmusic.service.MusicPlayerService


class PlaylistFragment : Fragment() {
    private var _binding: FragmentPlaylistBinding? = null
    private val binding get() = _binding!!
    lateinit var adapter: AlbumAdapter
    private val mainActivity get() = activity as MainActivity

    companion object{
        const val ARG_STYLE = "STYLE"
        fun newInstance(style: MusicStyle?): PlaylistFragment{
            val fragment = PlaylistFragment()
            val bundle = Bundle().apply {
                putSerializable(ARG_STYLE, style)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlaylistBinding.inflate(inflater, container, false)

        setRecyclerview()

        (arguments?.getSerializable(ARG_STYLE) as MusicStyle?)?.let { setStyle(it) }

        setEvent()

        return binding.root
    }

    private fun setRecyclerview(){
        val listAlbum = AlbumManager.getListAlbum(requireContext())
        adapter = AlbumAdapter(requireContext())
        adapter.list = listAlbum
        adapter.onItemClick = { position, _ ->
            startAlbumActivity(position)
        }
        adapter.onPlayClick = {
            startMusicService(ListSongManager.getSongFromID(it))
        }

        binding.rcvAlbum.adapter = adapter
        binding.rcvAlbum.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val dialog = AlertDialog.Builder(requireContext()).apply {
                    setTitle("Xác nhận xóa")
                    setMessage("Xác nhận xóa playlist ${adapter.list[viewHolder.adapterPosition].name}")
                    setPositiveButton("Xóa") { _, _ ->
                        AlbumManager.deleteAlbum(requireContext(), adapter.list[viewHolder.adapterPosition])
                        adapter.list = AlbumManager.getListAlbum(requireContext())
                    }
                    setNegativeButton("Hủy") { _, _ ->
                        adapter.notifyItemChanged(viewHolder.adapterPosition)
                    }
                    create()
                }
                dialog.show()
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.rcvAlbum)
    }

    private fun setEvent(){
        binding.rltAddAlbum.setOnClickListener {
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_playlist, null)
            val input: EditText = view.findViewById(R.id.edt_add)
            val alertDialog = AlertDialog.Builder(requireContext()).apply {
                setPositiveButton("OK") { _, _ ->
                    val album = Album(input.text.toString())
                    //AlbumManager.insertAlbum(requireContext(), album)
                    Room.databaseBuilder(requireContext(), AlbumDatabase::class.java, "album")
                        .allowMainThreadQueries()
                        .build()
                        .AlbumDao()
                        .insertAlbum(album)
                    AlbumManager.getListAlbum(requireContext()){
                        adapter.list = it
                    }
                }
                setNegativeButton("Cancel") { _, _ -> }
                setView(view)
                create()
            }
            alertDialog.show()
        }
    }

    private fun startAlbumActivity(position: Int){
        val intent = Intent(requireContext(), AlbumActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable("album" ,adapter.list[position])
        intent.putExtra("data", bundle)
        startActivity(intent)
    }

    private fun startMusicService(listSong: List<Song>, position: Int = 0){
        val intent = Intent(requireContext(), MusicPlayerService::class.java)
        intent.action = "sendListSong"

        val list = CustomList(listSong)
        val bundle = Bundle().apply {
            putParcelable("listSong", list)
            putInt("position", position)
        }

        intent.putExtra("dataBundle", bundle)
        activity?.startService(intent)
    }

    fun setStyle(style: MusicStyle) {
        adapter.style = style
        binding.tvYourAlbums.setTextColor(style.contentColor)
        binding.tvTitle.setTextColor(style.titleColor)
        DrawableCompat.setTint(binding.imgAdd.drawable, style.titleColor)
    }

    override fun onResume() {
        super.onResume()
        adapter.list = AlbumManager.getListAlbum(requireContext())
    }
}