package com.example.chillmusic.fragment.listsongs

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.chillmusic.R
import com.example.chillmusic.adapter.AlbumAdapter
import com.example.chillmusic.adapter.ListSongsAdapter
import com.example.chillmusic.database.album.AlbumDao
import com.example.chillmusic.database.album.AlbumDatabase
import com.example.chillmusic.databinding.FragmentListSongsBinding
import com.example.chillmusic.model.Album
import com.example.chillmusic.model.CustomList
import com.example.chillmusic.model.Song
import com.example.chillmusic.service.MusicPlayerService
import com.example.chillmusic.service.ScannerMusic
import com.google.android.material.bottomsheet.BottomSheetDialog

class ListSongsFragment : Fragment() {
    var _binding: FragmentListSongsBinding? = null
    val binding get() = _binding!!

    val listSong: List<Song>
        get() {
            return ScannerMusic.getListAudio(requireContext())
        }

    private val listAlbum:List<Album>
        get() = albumDao.getListAlbum()

    private val albumDao: AlbumDao
        get() {
            val albumDatabase = Room.databaseBuilder(requireContext(), AlbumDatabase::class.java, "album")
                .allowMainThreadQueries()
                .build()
            return albumDatabase.AlbumDao()
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentListSongsBinding.inflate(inflater, container, false)
        val view = binding.root
        val onItemClickListener = object:ListSongsAdapter.OnItemClickListener{
            override fun onSongClick(position: Int) {
                startMusicService(listSong, position)
            }

            override fun onLongClick(position: Int) {
                openBottomSheetDialog(listSong[position].id)
            }

            override fun onButtonAddClick(position: Int) {
                openBottomSheetDialog(listSong[position].id)
            }

            override fun onButtonFavoriteClick(position: Int) {
                val album = albumDao.getAlbum("Yêu thích")
                val list = album.listSong as MutableList<Int>
                list.add(listSong[position].id)
                album.listSong = list
                albumDao.updateAlbum(album)
            }
        }

        binding.rcvListSong.adapter = ListSongsAdapter(requireContext(), onItemClickListener, listSong)

        val layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        binding.rcvListSong.layoutManager = layoutManager

        return view
    }

    private fun openBottomSheetDialog(id: Int) {
        val view = layoutInflater.inflate(R.layout.bottom_sheet_album, null)
        val bottomSheet = BottomSheetDialog(requireContext())
        bottomSheet.setContentView(view)
        bottomSheet.show()
        bottomSheet.setCancelable(true)

        val rcvListAlbum: RecyclerView = view.findViewById(R.id.rcv_list_album)
        val adapter = AlbumAdapter(requireContext(), object : AlbumAdapter.OnItemClickListener{
            override fun OnAlbumClick(position: Int) {
                val album = listAlbum[position]
                val listId = album.listSong as MutableList<Int>
                listId.add(id)
                album.listSong = listId
                albumDao.insertAlbum(album)
            }

            override fun OnButtonPlayClick(position: Int) {
                TODO("Not yet implemented")
            }
        })

        adapter.list = listAlbum
        rcvListAlbum.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        rcvListAlbum.adapter = adapter
    }

    private fun startMusicService(listSong: List<Song>, position: Int){
        val intent = Intent(requireContext(), MusicPlayerService::class.java)
        intent.action = "sendListSong"

        val bundle = Bundle()
        val list = CustomList(listSong)
        bundle.putParcelable("listSong", list)
        bundle.putInt("position", position)

        intent.putExtra("dataBundle", bundle)
        activity?.startService(intent)
    }
}