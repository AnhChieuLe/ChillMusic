package com.example.chillmusic.activity

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.util.Pair
import android.view.View
import androidx.core.graphics.drawable.DrawableCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chillmusic.R
import com.example.chillmusic.`object`.AlbumManager
import com.example.chillmusic.`object`.ListSongManager
import com.example.chillmusic.adapter.ListSongsAdapter
import com.example.chillmusic.databinding.ActivityAlbumBinding
import com.example.chillmusic.model.Album
import com.example.chillmusic.model.CustomList
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song
import com.example.chillmusic.service.*

class AlbumActivity : AppCompatActivity() {
    private var _binding: ActivityAlbumBinding? = null
    private val binding: ActivityAlbumBinding get() = _binding!!
    lateinit var adapter: ListSongsAdapter

    val album: Album get() = intent.getBundleExtra("data")?.getSerializable("album") as Album

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when (p1?.action) {
                "sendAction" -> {
                    when (p1.getIntExtra("action", 0)) {
                        ACTION_START -> {
                            setInfo()
                            setStyle(service.song.style)
                        }
                        ACTION_CLEAR -> {
                            binding.cardAlbumMiniPlayer.visibility = View.GONE
                            disConnectService()
                        }
                        ACTION_PAUSE -> setInfo()
                        ACTION_RESUME -> setInfo()
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
            if (service.isInitialized){
                setInfo()
                setStyle(service.song.style)
            }
            Log.d("service", "connected")
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isConnected = false
            Log.d("service", "disconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, IntentFilter("sendAction"))

        setRecyclerview()
        setEvent()
        connectService()
    }

    private fun setRecyclerview() {
        adapter = ListSongsAdapter(applicationContext)
        adapter.onItemClick = {
            if(!isConnected) connectService()
            startMusicService(adapter.listSong, it)
        }
        adapter.listSong = ListSongManager.getSongFromID(album.listSong)
        binding.rcvListSong.adapter = adapter
    }

    fun openDialog(position: Int){
        val dialog = AlertDialog.Builder(this).apply {
            setTitle("Xác nhận xóa")
            setMessage("Xác nhận xóa bài hát khỏi playlist")
            setPositiveButton("Xóa") { p0, p1 ->
                album.listSong.removeIf { it == adapter.listSong[position].id.toInt() }
                AlbumManager.updateAlbum(this@AlbumActivity, album)
            }
            setNegativeButton("Hủy") { p0, p1 ->
                adapter.notifyItemChanged(position)
            }
            create()
        }
        dialog.show()
    }

    private fun connectService() {
        val intent = Intent(this, MusicPlayerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun disConnectService() {
        if (isConnected) {
            unbindService(serviceConnection)
            isConnected = false
        }
    }

    private fun startMusicService(listSong: List<Song>, position: Int) {
        val intent = Intent(this, MusicPlayerService::class.java)
        startService(intent)
        service.listSong = listSong.toMutableList()
        service.position = position
        service.startMusic()
        service.playListName = album.name
    }

    private fun setEvent() {
        binding.tvTitle.text = album.name
        binding.tvInfo.text = getString(R.string.other_info_album, album.listSong.size)

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                openDialog(viewHolder.adapterPosition)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rcvListSong)

        with(binding) {
            cardAlbumMiniPlayer.visibility = View.GONE
            layoutAlbumMiniPlayer.frameMusicPlayer.setOnClickListener {
                val intent = Intent(this@AlbumActivity, MusicPlayerActivity::class.java)
                startActivity(intent)
            }

            layoutAlbumMiniPlayer.imgPlayOrPause.setOnClickListener {
                with(service) {
                    if (isPlaying)
                        pauseMusic()
                    else
                        resumeMusic()
                }
            }

            layoutAlbumMiniPlayer.imgClear.setOnClickListener {
                service.stopMusic()
            }
        }
    }

    private fun setInfo() {
        binding.cardAlbumMiniPlayer.visibility = View.VISIBLE
        binding.layoutAlbumMiniPlayer.imgPlayOrPause.setImageResource(if (service.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)

        with(binding.layoutAlbumMiniPlayer) {
            tvTitle.text = service.song.title
            tvArtist.text = if (service.song.artist == "") "Không rõ" else service.song.artist
            imgAlbumArtist.setImageBitmap(service.image)
            imgPlayOrPause.setImageResource(if (service.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        }
    }

    fun setStyle(style: MusicStyle?){
        style?.let{
            with(binding.layoutAlbumMiniPlayer){
                DrawableCompat.setTint(imgPlayOrPause.drawable.mutate(), style.contentColor)
                DrawableCompat.setTint(imgClear.drawable.mutate(), style.contentColor)
                frameMusicPlayer.setBackgroundColor(style.backgroundColor)
                tvTitle.setTextColor(style.contentColor)
                tvArtist.setTextColor(style.contentColor)
            }

            window.statusBarColor = style.backgroundColor
            window.navigationBarColor = style.backgroundColor

            binding.root.setBackgroundColor(style.backgroundColor)
            binding.tvTitle.setTextColor(style.contentColor)
            binding.tvInfo.setTextColor(style.contentColor)
        }

        val position = adapter.listSong.indexOf(service.song)
        adapter.setStyle(style, position)
    }

    override fun onDestroy() {
        super.onDestroy()
        disConnectService()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }
}