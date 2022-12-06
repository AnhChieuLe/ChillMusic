package com.example.chillmusic.activity

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.palette.graphics.Palette
import androidx.viewpager2.widget.ViewPager2
import com.example.chillmusic.R
import com.example.chillmusic.adapter.MainViewPagerAdapter
import com.example.chillmusic.databinding.ActivityMainBinding
import com.example.chillmusic.model.Song
import com.example.chillmusic.service.*

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    val binding get() = _binding!!

    private val broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            when(p1?.action){
                "sendAction" -> {
                    with(binding){
                        when(p1.getIntExtra("action", 0)){
                            ACTION_START ->{
                                frameMusicPlayer.visibility = View.VISIBLE
                                setInfo()
                            }
                            ACTION_CLEAR ->{
                                frameMusicPlayer.visibility = View.GONE
                                disConnectService()
                            }
                            ACTION_PAUSE ->
                                imgPlayOrPause.setImageResource(R.drawable.ic_play)
                            ACTION_RESUME ->
                                imgPlayOrPause.setImageResource(R.drawable.ic_pause)
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

        if(isRunning)
            setInfo()
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
        with(binding){
            imgPlayOrPause.setOnClickListener {
                if(service.isPlaying)
                    service.pauseMusic()
                else
                    service.resumeMusic()
            }

            frameMusicPlayer.setOnClickListener {
                service.startMusicPlayerActivity()
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
        with(binding){
            if (service.song.image != null) {
                imgAlbumArtist.setImageBitmap(service.song.image)
                Palette.from(service.song.image!!).clearFilters().generate {
                    var swatch = it?.vibrantSwatch
                    if (swatch == null)
                        swatch = it?.mutedSwatch
                    frameMusicPlayer.setBackgroundColor(swatch!!.rgb)
                    tvTitle.setTextColor(swatch.titleTextColor)
                    tvArtist.setTextColor(swatch.bodyTextColor)
                }
            } else {
                imgAlbumArtist.setImageResource(R.drawable.avatar2)
                frameMusicPlayer.setBackgroundResource(R.color.white)
                tvTitle.setTextColor(resources.getColor(R.color.text))
                tvArtist.setTextColor(resources.getColor(R.color.text))
            }

            tvTitle.text = service.song.title
            tvArtist.text = if (service.song.artist == "") "Không rõ" else service.song.artist
            imgPlayOrPause.setImageResource(if (service.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        }
    }

    private fun sendAction(action: Int){
        val intent = Intent(this, MusicPlayerService::class.java)
        intent.putExtra("action", action)
        startService(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        disConnectService()
    }
}