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
import com.example.chillmusic.adapter.MusicInfoAdapter
import com.example.chillmusic.fragment.PlaylistAddFragment
import com.example.chillmusic.model.Song


class MusicPlayerActivity : AppCompatActivity() {
    private var _binding: ActivityMusicPlayerBinding? = null
    private val binding get() = _binding!!
    private val timer = Timer()
    private lateinit var adapter: MusicInfoAdapter

    private val broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            when(p1?.action){
                "sendAction" -> {
                    val action = p1.getIntExtra("action", 0)
                    Log.d("intentAction", action.toString())
                    when(action){
                        ACTION_PAUSE -> setStatus()
                        ACTION_RESUME -> setStatus()
                        ACTION_CLEAR -> {
                            finishAndRemoveTask()
                            timer.cancel()
                        }
                        ACTION_START -> {
                            setInfo()
                            setStyle()
                            setStatus()
                            if(seekBarThread.state == Thread.State.NEW)
                                seekBarThread.start()
                            adapter.setInfo()
                            adapter.setStyle()
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
            if(seekBarThread.state == Thread.State.NEW && service.isInitialized){
                setInfo()
                setStatus()
                setStyle()
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
                binding.seekbar.post(Runnable {
                    binding.seekbar.max = service.mediaPlayer.duration
                    binding.seekbar.progress = service.mediaPlayer.currentPosition
                })
            }
        }, 0, 1000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMusicPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, IntentFilter("sendAction"))
        setActionBar()
        setEvent()
        connectService()
    }

    private fun setInfo(){
        binding.tvPlaylist.text = service.playListName
        binding.seekbar.max = service.mediaPlayer.duration
        binding.seekbar.progress = service.mediaPlayer.currentPosition
    }

    private fun setViewPager(){
        binding.viewPager2.offscreenPageLimit = 3
        adapter = MusicInfoAdapter(this)
        binding.viewPager2.adapter = adapter
        binding.viewPager2.currentItem = 1
    }

    private fun setStatus(){
        binding.imgPlayOrPause.setImageResource(if(service.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)

        when(service.navigationStatus){
            NORMAL -> {
                binding.imgNavigation.setImageResource(R.drawable.ic_compare_arrows)
            }
            RANDOM -> {
                binding.imgNavigation.setImageResource(R.drawable.ic_shuffle)
            }
            REPEAT_ALL -> {
                binding.imgNavigation.setImageResource(R.drawable.ic_repeat)
            }
            REPEAT_ONE -> {
                binding.imgNavigation.setImageResource(R.drawable.ic_repeat_one)
            }
        }
        DrawableCompat.setTint(binding.imgNavigation.drawable.mutate(), service.song.style!!.contentColor)
    }

    private fun setStyle(){
        with(binding){
            with(service.song.style!!){
                frameTransition.setBackgroundColor(backgroundColor)

                window.statusBarColor = backgroundColor
                window.navigationBarColor = backgroundColor

                tvPlaylist.setTextColor(contentColor)
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
                DrawableCompat.setTint(imgVolume.drawable.mutate(), contentColor)
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
                when(service.navigationStatus){
                    NORMAL -> service.navigationStatus = RANDOM
                    RANDOM -> service.navigationStatus = REPEAT_ALL
                    REPEAT_ALL -> service.navigationStatus = REPEAT_ONE
                    REPEAT_ONE -> service.navigationStatus = NORMAL
                }
                setStatus()
            }

            imgBack.setOnClickListener {
                onBackPressed()
            }

            seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    val max = p0?.max ?: 0
                    Log.d("max", max.toString())
                    val current = p0?.progress ?: 0
                    val format = SimpleDateFormat(
                        if (max < 3600000)
                            "mm:ss"
                        else
                            "hh:mm:ss",
                        Locale.getDefault()
                    )
                    val calendar = Calendar.getInstance()

                    calendar.timeInMillis = max.toLong()
                    binding.seekbarMax.text = Song.getStringDuration(max.toLong())

                    calendar.timeInMillis = current.toLong()
                    binding.seekbarCurrent.text = Song.getStringDuration(current.toLong())
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    p0?.let { service.mediaPlayer.seekTo(p0.progress) }
                }
            })

            imgPlaylist.setOnClickListener {
                viewPager2.currentItem = 0
            }

            imgVolume.setOnClickListener {
                openVolumeDialog()
            }

            imgPlaylistAdd.setOnClickListener {
                val bottomSheetDialog = PlaylistAddFragment.newInstance(service.song.id, service.song.style)
                bottomSheetDialog.show(supportFragmentManager, bottomSheetDialog.tag)
            }
        }
    }

    private fun openVolumeDialog(){
        val viewGroup = FrameLayout(this@MusicPlayerActivity)
        viewGroup.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        viewGroup.setPadding(5, 5, 5, 5)
        val view = LayoutInflater.from(applicationContext).inflate(R.layout.dialog_volume, viewGroup)
        val seekBar = view.findViewById<SeekBar>(R.id.seekbar_volume)
        val current = view.findViewById<TextView>(R.id.seekbar_progress)

        seekBar.progress = (service.volume * 100).toInt()
        current.text = (service.volume * 100).toInt().toString()
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                p0?.let {
                    current.text = p0.progress.toString()
                    service.volume = it.progress.toFloat()/it.max
                    service.setVol()
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })
        val alertDialog = AlertDialog.Builder(this@MusicPlayerActivity)
            .setView(view)
            .create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window?.setGravity(Gravity.CENTER)
        alertDialog.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAndRemoveTask()
        timer.cancel()
    }

    private fun setActionBar(){
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
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