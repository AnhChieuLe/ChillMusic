package com.example.chillmusic.activity

import android.app.ActivityOptions
import android.content.*
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.util.Pair
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.palette.graphics.Palette
import androidx.viewpager2.widget.ViewPager2
import com.example.chillmusic.R
import com.example.chillmusic.adapter.MainViewPagerAdapter
import com.example.chillmusic.databinding.ActivityMainBinding
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.service.*


class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    val binding get() = _binding!!

    private val broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            when(p1?.action){
                "sendAction" -> {
                    with(binding.layoutMiniPlayer){
                        when(p1.getIntExtra("action", 0)){
                            ACTION_START -> {
                                if(isConnected)
                                    setInfo()
                                else
                                    connectService()
                            }
                            ACTION_NEXT -> setInfo()
                            ACTION_PREVIOUS -> setInfo()
                            ACTION_PAUSE -> setInfo()
                            ACTION_RESUME -> setInfo()
                            ACTION_CLEAR -> {
                                frameMusicPlayer.visibility = View.GONE
                                disConnectService()
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

            if(service.isSongInitialized )
                setInfo()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isConnected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filter = IntentFilter("sendData")
        filter.addAction("sendAction")
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter)

        setEvent()
        setViewPager()
        connectService()
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

    private fun setEvent(){
        binding.layoutMiniPlayer.frameMusicPlayer.visibility = View.GONE
        with(binding.layoutMiniPlayer){
            imgPlayOrPause.setOnClickListener {
                if(service.isPlaying)
                    service.pauseMusic()
                else
                    service.resumeMusic()
                setInfo()
            }

            frameMusicPlayer.setOnClickListener {
                val intent = Intent(applicationContext, MusicPlayerActivity::class.java)
                val options = ActivityOptions.makeSceneTransitionAnimation(this@MainActivity,
                    Pair.create(imgAlbumArtist, "album_artist"),
                    Pair.create(tvTitle, "title"),
                    Pair.create(tvArtist, "artist"),
                    Pair.create(song, "frame_transition")
                )
                startActivity(intent, options.toBundle())
            }

            imgClear.setOnClickListener {
                service.stopMusic()
            }
        }
    }

    private fun setViewPager(){
        with(binding){
            mainViewpager.adapter = MainViewPagerAdapter(this@MainActivity)

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
        binding.layoutMiniPlayer.frameMusicPlayer.visibility = View.VISIBLE
        with(binding.layoutMiniPlayer){
            imgAlbumArtist.setImageBitmap(service.image)

            tvTitle.text = service.song.title
            tvArtist.text = if (service.song.artist == "") "Không rõ" else service.song.artist
            imgPlayOrPause.setImageResource(if (service.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)

            with(service){
                frameMusicPlayer.setBackgroundColor(style.backgroundColor)
                binding.layoutMiniPlayer.song.setBackgroundColor(style.backgroundColor)
                tvTitle.setTextColor(style.contentColor)
                tvArtist.setTextColor(style.contentColor)
                DrawableCompat.setTint(imgPlayOrPause.drawable, style.contentColor)
                DrawableCompat.setTint(imgClear.drawable, style.contentColor)
                divider.setBackgroundColor(style.contentColor)
            }

            setBottomNavigationColor(service.style)
        }
    }

    private fun setBottomNavigationColor(style: MusicStyle){
                // Set Color for bottom navigation
                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                )
                val colors = intArrayOf(
                    style.contentColor,
                    style.titleColor!!
                )
                val myList = ColorStateList(states, colors)

                binding.bottomNavigation.setBackgroundColor(style.backgroundColor)
                binding.bottomNavigation.itemTextColor = myList
                binding.bottomNavigation.itemIconTintList = myList

                window.statusBarColor = style.backgroundColor

                window.navigationBarColor = style.backgroundColor
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        disConnectService()
        Log.d("state", "main destroy")
    }
}