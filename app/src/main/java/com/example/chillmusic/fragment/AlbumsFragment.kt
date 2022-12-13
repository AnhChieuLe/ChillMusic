package com.example.chillmusic.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.chillmusic.R
import com.example.chillmusic.activity.AlbumActivity
import com.example.chillmusic.adapter.AlbumAdapter
import com.example.chillmusic.database.album.AlbumDao
import com.example.chillmusic.database.album.AlbumDatabase
import com.example.chillmusic.databinding.FragmentAlbumsBinding
import com.example.chillmusic.model.Album
import com.example.chillmusic.model.CustomList
import com.example.chillmusic.model.Song
import com.example.chillmusic.service.MusicPlayerService
import com.example.chillmusic.service.ScannerMusic


class AlbumsFragment : Fragment() {
    private var _binding: FragmentAlbumsBinding? = null
    private val binding get() = _binding!!

    private val listAlbum:List<Album>
        get() = dao.getListAlbum()

    val dao:AlbumDao
        get() {
            val albumDatabase = Room.databaseBuilder(requireContext(), AlbumDatabase::class.java, "album")
                .allowMainThreadQueries()
                .build()
            return albumDatabase.AlbumDao()
        }

    lateinit var adapter: AlbumAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlbumsBinding.inflate(inflater, container, false)
        val view = binding.root

        setEvent()
        setRecyclerview()

        return view
    }

    private fun setRecyclerview(){
        val listener = object: AlbumAdapter.OnItemClickListener{
            override fun onAlbumClick(position: Int) {
                val intent = Intent(requireContext(), AlbumActivity::class.java)
                val bundle = Bundle()
                bundle.putSerializable("album" ,listAlbum[position])
                intent.putExtra("data", bundle)
                startActivity(intent)
            }

            override fun onButtonPlayClick(position: Int) {
                startMusicService(ScannerMusic.getListAudio(requireContext(), listAlbum[position].listSong), 0)
            }
        }

        adapter = AlbumAdapter(requireContext(), listener)
        adapter.list = listAlbum

        binding.rcvAlbum.adapter = adapter
        binding.rcvAlbum.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun setEvent(){
        binding.rltAddAlbum.setOnClickListener {
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_album, null)
            val input: EditText = view.findViewById(R.id.edt_add)
            val alertDialog = AlertDialog.Builder(requireContext())
                .setPositiveButton("OK") { p0, _ ->
                    val album = Album(input.text.toString())
                    dao.insertAlbum(album)
                    adapter.list = dao.getListAlbum()
                    p0?.dismiss()
                }
                .setNegativeButton("Cancel") { p0, _ -> p0?.dismiss() }
                .setView(view)
                .create()
            alertDialog.window?.setBackgroundDrawable(ResourcesCompat.getDrawable(resources, R.drawable.background_dialog, null))
            alertDialog.show()
        }

        val itemTouchHelper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                dao.deleteAlbum(listAlbum[position])
                adapter.list = listAlbum
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.rcvAlbum)
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

    override fun onPause() {
        super.onPause()
        Log.d("album", "pause")
    }

    override fun onResume() {
        super.onResume()
        adapter.list = dao.getListAlbum()
        Log.d("album", "resume")
    }
}