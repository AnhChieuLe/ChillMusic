package com.example.chillmusic.activity

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.TextView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chillmusic.R
import com.example.chillmusic.adapter.AlbumAdapter
import com.example.chillmusic.adapter.ListSongsAdapter
import com.example.chillmusic.databinding.ActivityAlbumBinding
import com.example.chillmusic.model.Album
import com.example.chillmusic.model.CustomList
import com.example.chillmusic.model.Song
import com.example.chillmusic.service.*

class AlbumActivity : AppCompatActivity() {
    private var _binding: ActivityAlbumBinding? = null
    private val binding: ActivityAlbumBinding get() = _binding!!
    lateinit var adapter: ListSongsAdapter

    val album: Album get() = intent.getBundleExtra("data")?.getSerializable("album") as Album
    val listData: List<Song> get() = ScannerMusic.getListAudio(applicationContext, album.listSong)

    private val broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            when(p1?.action){
                "sendAction" -> {
                    when(p1.getIntExtra("action", 0)){
                        ACTION_START -> {
                            setInfo()
                        }
                    }
                }
            }
        }
    }

    var isConnected = false
    lateinit var service: MusicPlayerService
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            val binder = p1 as MusicPlayerService.MyBinder
            service = binder.getService()
            isConnected = true
            if(service.isSongInitialized)
                setInfo()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isConnected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, IntentFilter("sendAction"))

        setContent()

        connectService()
    }

    private fun setContent(){
        val listener = object: ListSongsAdapter.OnItemClickListener{
            override fun onSongClick(position: Int) {
                startMusicService(listData, position)
                connectService()
            }

            override fun onButtonAddClick(position: Int) {

            }
        }
        adapter = ListSongsAdapter(applicationContext, listener)
        adapter.listSong = listData
        with(binding){
            rcvListSong.adapter = adapter
            rcvListSong.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            tvTitle.text = album.name
            tvInfo.text = getString(R.string.other_info_album, album.listSong.size, 1024)
        }
    }

    private fun connectService(){
        val intent = Intent(this, MusicPlayerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun disConnectService(){
        if(isConnected){
            unbindService(serviceConnection)
            isConnected = false
        }
    }

    private fun startMusicService(listSong: List<Song>, position: Int){
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.action = "sendListSong"

        val bundle = Bundle()
        bundle.putSerializable("listSong", CustomList(listSong))
        bundle.putInt("position", position)

        intent.putExtra("dataBundle", bundle)
        startService(intent)
    }

    private fun setInfo(){
        adapter.style = service.style

        binding.root.setBackgroundColor(service.style.backgroundColor)
        window.statusBarColor = service.style.backgroundColor
        window.navigationBarColor = service.style.backgroundColor

        binding.tvTitle.setTextColor(service.style.contentColor)
        binding.tvInfo.setTextColor(service.style.contentColor)
    }

    override fun onDestroy() {
        super.onDestroy()
        disConnectService()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }
}