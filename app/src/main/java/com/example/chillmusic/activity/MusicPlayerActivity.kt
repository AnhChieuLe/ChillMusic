package com.example.chillmusic.activity

import android.content.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.palette.graphics.Palette
import com.example.chillmusic.R
import com.example.chillmusic.databinding.ActivityMusicPlayerBinding
import com.example.chillmusic.service.*
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.example.chillmusic.model.MusicStyle


const val NORMAL = 0
const val RANDOM = 1
const val REPEAT_ALL = 2
const val REPEAT_ONE = 3
class MusicPlayerActivity : AppCompatActivity() {
    private var _binding: ActivityMusicPlayerBinding? = null
    private val binding get() = _binding!!

    private var navigationStatus = NORMAL

    private val timer = Timer()

    private val broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            when(p1?.action){
                "sendAction" -> {
                    val action = p1.getIntExtra("action", 0)
                    Log.d("intentAction", action.toString())
                    when(action){
                        ACTION_PAUSE -> setInfo()
                        ACTION_RESUME -> setInfo()
                        ACTION_CLEAR -> {
                            finishAndRemoveTask()
                            timer.cancel()
                        }
                        ACTION_START -> {
                            setInfo()
                            if(seekBarThread.state == Thread.State.NEW)
                                seekBarThread.start()
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
            if(seekBarThread.state == Thread.State.NEW && service.isSongInitialized){
                setInfo()
                seekBarThread.start()
            }
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isConnected = false
        }
    }

    private val seekBarThread = Thread {
        timer.scheduleAtFixedRate(object : TimerTask(){
            override fun run() {
                val max = service.mediaPlayer.duration
                val current = service.mediaPlayer.currentPosition

                val format = SimpleDateFormat("mm:ss", Locale.getDefault())
                val calendar = Calendar.getInstance()

                binding.seekbar.post(Runnable {
                    binding.seekbar.max = max
                    binding.seekbar.progress = current
                })

                binding.seekbarMax.post(Runnable {
                    calendar.timeInMillis = max.toLong()
                    binding.seekbarMax.text = format.format(calendar.time).toString()
                })

                binding.seekbarCurrent.post(Runnable {
                    calendar.timeInMillis = current.toLong()
                    binding.seekbarCurrent.text = format.format(calendar.time).toString()
                })

                Log.d("timer", "seekbar: $current / $max")
            }
        }, 0, 1000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, IntentFilter("sendAction"))

        connectService()
        setActionBar()
        setEvent()
    }

    private fun setInfo(){
        with(binding){
            imgPlayOrPause.setImageResource(if(service.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            tvTitle.text = service.song.title
            tvArtist.text = if(service.song.artist == "") "Không rõ" else service.song.artist
            imgAlbumArt.setImageBitmap(service.image)

            with(service.style){
                frameTransition.setBackgroundColor(backgroundColor)

                window.statusBarColor = backgroundColor

                window.navigationBarColor = backgroundColor

                tvTitle.setTextColor(contentColor)
                tvArtist.setTextColor(contentColor)
                tvAlbum.setTextColor(contentColor)
                seekbarMax.setTextColor(contentColor)
                seekbarCurrent.setTextColor(contentColor)
                seekbar.progressDrawable.setTint(contentColor)
                val thumb = AppCompatResources.getDrawable(applicationContext, R.drawable.seekbar_thumb)
                thumb?.setTint(contentColor)
                seekbar.thumb = thumb

                DrawableCompat.setTint(imgBack.drawable.mutate(), contentColor)
                DrawableCompat.setTint(imgMore.drawable.mutate(), contentColor)
                DrawableCompat.setTint(imgPlaylist.drawable.mutate(), contentColor)
                DrawableCompat.setTint(imgPlaylistAdd.drawable.mutate(), contentColor)
                DrawableCompat.setTint(imgFavorite.drawable.mutate(), contentColor)
                DrawableCompat.setTint(imgShare.drawable.mutate(), contentColor)
                DrawableCompat.setTint(imgNavigation.drawable.mutate(), contentColor)
                DrawableCompat.setTint(imgPrevious.drawable.mutate(), contentColor)
                DrawableCompat.setTint(imgPlayOrPause.drawable.mutate(), contentColor)
                DrawableCompat.setTint(imgNext.drawable.mutate(), contentColor)
                DrawableCompat.setTint(imgClear.drawable.mutate(), contentColor)
            }
        }
    }

    private fun setEvent() {
        with(binding){
            imgPlayOrPause.setOnClickListener {
                if(service.isPlaying)
                    service.pauseMusic()
                else
                    service.resumeMusic()
            }

            imgNext.setOnClickListener {
                service.startNext()
            }

            imgPrevious.setOnClickListener {
                service.startPrevious()
            }

            imgClear.setOnClickListener {
                finishAndRemoveTask()
                timer.cancel()
                service.stopMusic()
            }

            imgNavigation.setOnClickListener {
                when(navigationStatus){
                    NORMAL -> {
                        binding.imgNavigation.setImageResource(R.drawable.ic_shuffle)
                        navigationStatus = RANDOM
                    }
                    RANDOM -> {
                        binding.imgNavigation.setImageResource(R.drawable.ic_repeat)
                        navigationStatus = REPEAT_ALL
                    }
                    REPEAT_ALL -> {
                        binding.imgNavigation.setImageResource(R.drawable.ic_repeat_one)
                        navigationStatus = REPEAT_ONE
                    }
                    REPEAT_ONE -> {
                        binding.imgNavigation.setImageResource(R.drawable.ic_shuffle_05)
                        navigationStatus = NORMAL
                    }
                }
            }

            imgBack.setOnClickListener {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAndRemoveTask()
        timer.cancel()
    }

    private fun setActionBar(){
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.tvAlbum.text = "Tất cả bản nhạc"
    }

    private fun connectService(){
        val intent = Intent(this, MusicPlayerService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun disConnectService(){
        if(isConnected){
            unbindService(serviceConnection)
        }
        isConnected = false
        Log.d("state", "unBind")
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        disConnectService()
        super.onDestroy()
        Log.d("state", "Destroy")
    }
}