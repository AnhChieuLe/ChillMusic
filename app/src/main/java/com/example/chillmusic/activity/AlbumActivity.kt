package com.example.chillmusic.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chillmusic.R
import com.example.chillmusic.adapter.ListSongsAdapter
import com.example.chillmusic.databinding.ActivityAlbumBinding
import com.example.chillmusic.model.Album
import com.example.chillmusic.model.Song
import com.example.chillmusic.service.MusicPlayerService
import com.example.chillmusic.service.ScannerMusic

class AlbumActivity : AppCompatActivity() {
    private var _binding: ActivityAlbumBinding? = null
    private val binding: ActivityAlbumBinding get() = _binding!!

    val album: Album get() = intent.getBundleExtra("data")?.getSerializable("album") as Album
    val listData: List<Song> get() = ScannerMusic.getListAudio(applicationContext, album.listSong)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setContent()

    }

    fun setContent(){
        val listener = object: ListSongsAdapter.OnItemClickListener{
            override fun onSongClick(position: Int) {
                startMusicService(listData, position)
            }

            override fun onLongClick(position: Int) {
                TODO("Not yet implemented")
            }

            override fun onButtonAddClick(position: Int) {
                TODO("Not yet implemented")
            }

            override fun onButtonFavoriteClick(position: Int) {
                TODO("Not yet implemented")
            }
        }
        val adapter = ListSongsAdapter(applicationContext, listener, listData)
        with(binding){
            rcvListSong.adapter = adapter
            rcvListSong.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            tvTitle.text = album.name
            tvInfo.text = getString(R.string.other_info_album, album.listSong.size, 1024)
        }
    }

    private fun startMusicService(listSong: List<Song>, position: Int){
        val intent = Intent(applicationContext, MusicPlayerService::class.java)
        intent.action = "sendListSong"
        intent.putExtra("listSong", listSong as java.io.Serializable)
        intent.putExtra("position", position)
        startService(intent)
    }
}