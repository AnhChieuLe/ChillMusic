package com.example.chillmusic.activity

import android.app.AlertDialog
import android.content.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.chillmusic.R
import com.example.chillmusic.databinding.ActivityMusicPlayerBinding
import com.example.chillmusic.service.*
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.example.chillmusic.`object`.PreferencesManager
import com.example.chillmusic.adapter.MiniMusicInfoAdapter
import com.example.chillmusic.adapter.MusicInfoAdapter
import com.example.chillmusic.databinding.ActivityMiniMusicPlayerBinding
import com.example.chillmusic.fragment.PlaylistAddFragment
import com.example.chillmusic.model.Song


class MiniMusicPlayerActivity : AppCompatActivity() {
    private var _binding: ActivityMiniMusicPlayerBinding? = null
    private val binding get() = _binding!!
    lateinit var adapter: MiniMusicInfoAdapter

    private val broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            when(p1?.action){
                "sendAction" -> {
                    when(p1.getIntExtra("action", 0)){
                        ACTION_PAUSE, ACTION_RESUME -> {
                            adapter.fragmentInfo.setStatus()
                            adapter.fragmentInfo.setStyle()
                        }
                        ACTION_CLEAR -> {
                            finishAndRemoveTask()
                        }
                        ACTION_START -> {
                            adapter.setInfo()
                            adapter.setStyle()
                            binding.frameTransition.setBackgroundColor(service.song.style!!.backgroundColor)
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
            setViewPager()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isConnected = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMiniMusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, IntentFilter("sendAction"))
        connectService()
    }

    private fun setViewPager(){
        binding.viewPager2.offscreenPageLimit = 3
        adapter = MiniMusicInfoAdapter(this)
        binding.viewPager2.adapter = adapter
        binding.viewPager2.currentItem = 1
        binding.frameTransition.setBackgroundColor(service.song.style!!.backgroundColor)
    }

    private fun connectService(){
        val intent = Intent(this, MusicPlayerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun disConnectService(){
        if(isConnected)
            unbindService(serviceConnection)
        isConnected = false
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        disConnectService()
        super.onDestroy()
    }
}