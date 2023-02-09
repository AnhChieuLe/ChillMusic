package com.example.chillmusic.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.media.*
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.chillmusic.R
import com.example.chillmusic.`object`.PreferencesManager
import com.example.chillmusic.activity.MiniMusicPlayerActivity
import com.example.chillmusic.activity.MusicPlayerActivity
import com.example.chillmusic.application.CHANNEL_MEDIA_PLAYER
import com.example.chillmusic.model.CustomList
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song
import com.example.chillmusic.receiver.MyReceiver
import kotlin.math.max

const val ACTION_PAUSE = 1
const val ACTION_RESUME = 2
const val ACTION_CLEAR = 3
const val ACTION_START = 4
const val ACTION_NEXT = 6
const val ACTION_PREVIOUS = 7

const val NORMAL = 0
const val RANDOM = 3
const val REPEAT_ALL = 1
const val REPEAT_ONE = 2

class MusicPlayerService : Service(){
    var mediaPlayer: MediaPlayer = MediaPlayer()
    var playListName = ""
    var listSong: MutableList<Song> = mutableListOf()
    val song:Song get() = listSong[position]
    val isInitialized: Boolean get() = listSong.isNotEmpty()
    var position = -1
    var isPlaying = false
    val image: Bitmap get() = song.image ?: BitmapFactory.decodeResource(resources, R.drawable.avatar2)
    val navigationStatus get() = PreferencesManager(applicationContext).navigation
    val volume: Float get() = PreferencesManager(applicationContext).volume.toFloat() / 100

    private val binder = MyBinder()
    inner class MyBinder: Binder(){
        fun getService() = this@MusicPlayerService
    }

    override fun onBind(p0: Intent?): IBinder{
        Log.d("musicService", "onBind")
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("musicService", "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer.setOnCompletionListener {
            startNext()
        }
        Log.d("musicService", "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //From fragment
        if(intent?.action == "sendListSong"){
            val bundle = intent.getBundleExtra("dataBundle")

            val list: CustomList = bundle?.getSerializable("listSong") as CustomList
            listSong = list.list as MutableList<Song>
            position = bundle.getInt("position")

            if(listSong.isNotEmpty()){
                startMusic()
            }
        }

        //From broadcast + activity
        when(intent?.getIntExtra("action", 0)){
            ACTION_PAUSE -> pauseMusic()
            ACTION_RESUME -> resumeMusic()
            ACTION_CLEAR -> stopMusic()
            ACTION_PREVIOUS -> startPrevious()
            ACTION_NEXT -> startNext()
        }

        Log.d("musicService", "onStartCommand")

        return START_NOT_STICKY
    }

    private fun sendNotification(song: Song, playbackSpeed:Float = 1F){
        val bubbleIntent = Intent(this, MiniMusicPlayerActivity::class.java)
        val bubblePendingIntent = PendingIntent.getActivity(this, 0, bubbleIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val intent = Intent(this, MusicPlayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val mediaSessionCompat = android.support.v4.media.session.MediaSessionCompat(this, "tag")
        // Seekbar
        val mediaMetadata = MediaMetadata.Builder()
            .putLong(MediaMetadata.METADATA_KEY_DURATION, /*mediaPlayer.duration.toLong()*/ -1L)
            .build()
        mediaSessionCompat.setMetadata(MediaMetadataCompat.fromMediaMetadata(mediaMetadata))
        mediaSessionCompat.isActive = true
        mediaSessionCompat.setPlaybackState(PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer.currentPosition.toLong(), playbackSpeed)
            .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
            .build())

        val style = androidx.media.app.NotificationCompat.MediaStyle()
            .setShowActionsInCompactView(1, 3)
            .setMediaSession(mediaSessionCompat.sessionToken)

        fun cropCenter(bmp: Bitmap): Bitmap {
            val dimension = bmp.width.coerceAtMost(bmp.height);
            return ThumbnailUtils.extractThumbnail(bmp, dimension, dimension);
        }

        val bubbleMetadata = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            NotificationCompat.BubbleMetadata.Builder().apply {
                setDesiredHeight(200)
                setIcon(IconCompat.createWithAdaptiveBitmap(cropCenter(image)))
                setIntent(bubblePendingIntent)
                setSuppressNotification(false)
            }.build()
        }else null

        val notification = NotificationCompat.Builder(this, CHANNEL_MEDIA_PLAYER).apply {
            setSmallIcon(R.drawable.chill_music_small_icon)
            setContentTitle(song.title)
            setContentText(if(song.artist == "") getString(R.string.unknown) else song.artist)
            setLargeIcon(image)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentIntent(pendingIntent)
            addAction(R.drawable.ic_previous, "previous", getPendingIntent(this@MusicPlayerService, ACTION_PREVIOUS))
            if(isPlaying)
                addAction(R.drawable.ic_pause, "pause_or_play", getPendingIntent(this@MusicPlayerService, ACTION_PAUSE))
            else
                addAction(R.drawable.ic_play, "pause_or_play", getPendingIntent(this@MusicPlayerService, ACTION_RESUME))
            addAction(R.drawable.ic_next, "next", getPendingIntent(this@MusicPlayerService, ACTION_NEXT))
            addAction(R.drawable.ic_clear, "clear", getPendingIntent(this@MusicPlayerService, ACTION_CLEAR))
            setStyle(style)
            if(PreferencesManager(applicationContext).bubble)
                setBubbleMetadata(bubbleMetadata)
        }.build()

//        val notification2 = NotificationCompat.Builder(this, CHANNEL_TEST).apply {
//            setSmallIcon(R.drawable.chill_music_small_icon)
//            setContentTitle(song.title)
//            setContentText(if(song.artist == "") getString(R.string.unknown) else song.artist)
//            setLargeIcon(image)
//            bubbleMetadata?.let { setBubbleMetadata(it) }
//        }.build()
//
//        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        manager.notify(2, notification2)

        startForeground(1, notification)
    }

    fun startMusic(){
        song.style = MusicStyle(image)
        mediaPlayer.reset()
        mediaPlayer.setDataSource(applicationContext, song.contentUri)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            it.start()
            isPlaying = true
            sendNotification(song)
            sendActionToActivity(ACTION_START)
        }
        setVol()
    }

    fun pauseMusic(){
        sendActionToActivity(ACTION_PAUSE)
        mediaPlayer.pause()
        isPlaying = false
        sendNotification(song, 0F)
    }

    fun resumeMusic(){
        sendActionToActivity(ACTION_RESUME)
        mediaPlayer.start()
        isPlaying = true
        sendNotification(song)
    }

    fun startNext(){
        when(navigationStatus){
            REPEAT_ALL -> {
                if(position == listSong.size - 1)
                    position = 0
                else
                    position++
            }
            REPEAT_ONE -> {
                mediaPlayer.isLooping = true
            }
            NORMAL -> {
                if(position == listSong.size - 1){
                    stopMusic()
                    return
                } else
                    position++
            }
            RANDOM -> {
                val rd = (listSong.indices).random()
                if(rd == position)  startNext()
                position = rd
            }
        }

        startMusic()
        sendActionToActivity(ACTION_NEXT)
    }

    fun startPrevious(){
        when(navigationStatus){
            REPEAT_ALL -> {
                if(position == 0)
                    position = listSong.size
                position--
            }
            REPEAT_ONE -> {
                mediaPlayer.isLooping = true
            }
            NORMAL -> {
                if(position == 0){
                    stopMusic()
                    return
                } else
                    position--
            }
            RANDOM -> {
                val rd = (listSong.indices).random()
                if(rd == position)  startPrevious()
                position = rd
            }
        }
        startMusic()
        sendActionToActivity(ACTION_PREVIOUS)
    }

    fun stopMusic(){
        stopForeground(true)
        mediaPlayer.reset()
        listSong.clear()
        position = -1
        sendActionToActivity(ACTION_CLEAR)
    }

    //Service -> Broadcast -> Service
    private fun getPendingIntent(context: Context, action: Int): PendingIntent{
        val intent = Intent(this, MyReceiver::class.java)
        intent.putExtra("action", action)
        return PendingIntent.getBroadcast(context.applicationContext, action, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun setVol(){
        mediaPlayer.setVolume(volume, volume)
    }

    private fun sendActionToActivity(action: Int){
        val intent = Intent("sendAction")
        intent.putExtra("action", action)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        Log.d("musicService", "onDestroy")
    }
}