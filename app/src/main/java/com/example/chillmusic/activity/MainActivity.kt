package com.example.chillmusic.activity

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.example.chillmusic.R
import com.example.chillmusic.`object`.PreferencesManager
import com.example.chillmusic.adapter.MainViewPagerAdapter
import com.example.chillmusic.databinding.ActivityMainBinding
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song
import com.example.chillmusic.service.*


class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    val binding get() = _binding!!
    lateinit var adapter: MainViewPagerAdapter

    private val broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            when(p1?.action){
                "sendAction" -> {
                    with(binding.layoutMiniPlayer){
                        when(p1.getIntExtra("action", 0)){
                            ACTION_START -> {
                                if(isConnected){
                                    setInfo()
                                    setStyle(service.song.style!!)
                                    setFragmentStyle(service.song)
                                } else
                                    connectService()
                            }
                            ACTION_NEXT -> {
                                setInfo()
                                setStatus()
                            }
                            ACTION_PREVIOUS -> {
                                setInfo()
                                setStatus()
                            }
                            ACTION_PAUSE -> {
                                setInfo()
                                setStatus()
                            }
                            ACTION_RESUME -> {
                                setInfo()
                                setStatus()
                            }
                            ACTION_CLEAR -> {
                                binding.cardMiniPlayer.visibility = View.GONE
                            }
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
            if(service.isInitialized){
                setInfo()
                setViewPager(service.song)
                service.song.style?.let { setStyle(it) }
            }else{
                setViewPager()
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isConnected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, IntentFilter("sendAction"))

        setEvent()
        connectService()
    }

    fun connectService(){
        val intent = Intent(this, MusicPlayerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        Log.d("serviceMain", "Connect")
    }

    fun disConnectService(){
        if(isConnected){
            unbindService(serviceConnection)
            isConnected = false
        }
        Log.d("serviceMain", "disConnect")
    }

    private fun setEvent(){
        binding.cardMiniPlayer.visibility = View.GONE
        with(binding.layoutMiniPlayer){
            imgPlayOrPause.setOnClickListener {
                if(service.isPlaying)
                    service.pauseMusic()
                else
                    service.resumeMusic()
            }

            frameMusicPlayer.setOnClickListener {
                val intent = Intent(applicationContext, MusicPlayerActivity::class.java)
                startActivity(intent)
            }

            imgClear.setOnClickListener {
                service.stopMusic()
                adapter.listSongsFragment.adapter.currentPosition = -1
            }
        }
    }

    private fun setViewPager(song: Song? = null){
        with(binding){
            mainViewpager.offscreenPageLimit = 2
            adapter = MainViewPagerAdapter(song, this@MainActivity)
            mainViewpager.adapter = adapter

            bottomNavigation.setOnItemSelectedListener {
                when(it.itemId){
                    R.id.nav_songs -> mainViewpager.currentItem = 0
                    R.id.nav_albums -> mainViewpager.currentItem = 1
                    R.id.nav_setting -> mainViewpager.currentItem = 2
                }
                true
            }

            mainViewpager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when(position){
                        0 -> bottomNavigation.menu.findItem(R.id.nav_songs).isChecked = true
                        1 -> bottomNavigation.menu.findItem(R.id.nav_albums).isChecked = true
                        2 -> bottomNavigation.menu.findItem(R.id.nav_setting).isChecked = true
                    }
                }
            })
        }
    }

    fun setInfo() {
        binding.cardMiniPlayer.visibility = View.VISIBLE
        with(binding.layoutMiniPlayer){
            imgAlbumArtist.setImageBitmap(service.image)
            tvTitle.text = service.song.title
            tvArtist.text = if (service.song.artist == "") "Không rõ" else service.song.artist
        }
    }

    fun setStatus(){
        binding.layoutMiniPlayer.imgPlayOrPause.setImageResource(if(service.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        DrawableCompat.setTint(binding.layoutMiniPlayer.imgPlayOrPause.drawable, service.song.style!!.contentColor)
    }

    fun setStyle(style: MusicStyle){
        with(binding.layoutMiniPlayer) {
            frameMusicPlayer.setBackgroundColor(style.backgroundColor)
            tvTitle.setTextColor(style.contentColor)
            tvArtist.setTextColor(style.contentColor)
            DrawableCompat.setTint(imgPlayOrPause.drawable, style.contentColor)
            DrawableCompat.setTint(imgClear.drawable, style.contentColor)
            divider1.setBackgroundColor(style.contentColor)
            divider2.setBackgroundColor(style.contentColor)
        }
        setBottomNavigationColor(style)
    }

    fun setFragmentStyle(song: Song){
        song.style?.let { adapter.albumFragment.setStyle(it) }
        adapter.listSongsFragment.setStyle(song)
    }

    private fun setBottomNavigationColor(style: MusicStyle){
        with(binding.bottomNavigation){
            setBackgroundColor(style.backgroundColor)
            itemTextColor = style.stateList
            itemIconTintList = style.stateList
        }
        window.statusBarColor = style.backgroundColor
        window.navigationBarColor = style.backgroundColor
        binding.root.setBackgroundColor(style.backgroundColor)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        if(PreferencesManager(applicationContext).stopOnDestroy){
            service.stopMusic()
        }
        disConnectService()
    }

    override fun onBackPressed() {
        if(binding.mainViewpager.currentItem == 0) {
            if(adapter.listSongsFragment.binding.edtSearch.text.toString() != ""){
                adapter.listSongsFragment.binding.edtSearch.text?.clear()
                adapter.listSongsFragment.binding.edtSearch.clearFocus()
            }else if(adapter.listSongsFragment.binding.edtSearch.isFocused)
                adapter.listSongsFragment.binding.edtSearch.clearFocus()
            else{
                AlertDialog.Builder(this).apply {
                    setTitle("Xác nhận thoát")
                    setMessage("Xác nhận thoát ứng dụng")
                    setPositiveButton("Thoát"){ p0, p1 ->
                        super.onBackPressed()
                    }
                    setNegativeButton("Hủy"){p0, p1 -> }
                    create()
                }.show()
            }
        }
        else
            binding.mainViewpager.currentItem = 0
    }
}