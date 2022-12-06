package com.example.chillmusic.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.Transliterator.Position
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.MediaSession2
import android.media.session.MediaSession
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.chillmusic.R
import com.example.chillmusic.activity.MusicPlayerActivity
import com.example.chillmusic.application.CHANNEL_MEDIA_PLAYER
import com.example.chillmusic.model.CustomList
import com.example.chillmusic.model.Song
import com.example.chillmusic.receiver.MyReceiver
import java.util.*
import kotlin.collections.ArrayList

const val ACTION_PAUSE = 1
const val ACTION_RESUME = 2
const val ACTION_CLEAR = 3
const val ACTION_START = 4
const val ACTION_COMPLETE = 5
const val ACTION_NEXT = 6
const val ACTION_PREVIOUS = 7
const val ACTION_UPDATE = 8
const val REQUIRE_DATA = 9
const val REFRESH_SEEKBAR = 10
var isRunning = false

class MusicPlayerService : Service() {
    var mediaPlayer: MediaPlayer = MediaPlayer()
    var listSong:MutableList<Song> = ArrayList<Song>()
    lateinit var song:Song
    var position = 0
    var isPlaying = false
    val binder = MyBinder()

    inner class MyBinder: Binder(){
        fun getService() = this@MusicPlayerService
    }

    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        mediaPlayer.setOnCompletionListener {
            startNext()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //From fragment
        if(intent?.action == "sendListSong"){
            val bundle = intent.getBundleExtra("dataBundle")

            val list: CustomList = bundle?.getParcelable<CustomList>("listSong") as CustomList
            for(parcelable in list.list){
                listSong.add(parcelable as Song)
            }
            position = bundle.getInt("position") ?: 0
            if(listSong.isNotEmpty()){
                song = listSong[position]
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
            ACTION_UPDATE -> startMusicPlayerActivity()
        }

        return START_NOT_STICKY
    }

    private fun sendNotification(song: Song, playbackSpeed:Float = 1F){
        val intent = Intent(this, MusicPlayerActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val mediaSessionCompat = android.support.v4.media.session.MediaSessionCompat(this, "tag")

        val bitmap =
            if(song.imageByte == null)
                BitmapFactory.decodeResource(resources, R.drawable.avatar2)
            else
                song.image

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
            .setShowActionsInCompactView(0, 2, 4)
            .setMediaSession(mediaSessionCompat.sessionToken)

        val notification = NotificationCompat.Builder(this, CHANNEL_MEDIA_PLAYER)
            .setSmallIcon(R.drawable.ic_music)
            .setSubText("Chill Lê")
            .setContentTitle(song.title)
            .setContentText(if(song.artist == "") "Không rõ" else song.artist)
            .setLargeIcon(bitmap)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_favorite, "favorite", null)
            .addAction(R.drawable.ic_previous, "previous", getPendingIntent(this, ACTION_PREVIOUS))
            .addAction(if(isPlaying) R.drawable.ic_pause else R.drawable.ic_play, "pause_or_play",
                if (isPlaying) getPendingIntent(this, ACTION_PAUSE) else getPendingIntent(this, ACTION_RESUME))
            .addAction(R.drawable.ic_next, "next", getPendingIntent(this, ACTION_NEXT))
            .addAction(R.drawable.ic_clear, "clear", getPendingIntent(this, ACTION_CLEAR))
            .setStyle(style)
            .build()
        startForeground(1, notification)
    }

    fun startMusic(){
        mediaPlayer.reset()
        mediaPlayer.setDataSource(applicationContext, Uri.parse(song.path))
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
            isPlaying = true
            sendNotification(song)
            sendActionToActivity(ACTION_START)
        }
    }

    fun pauseMusic(){
        mediaPlayer.pause()
        isPlaying = false
        sendNotification(song, 0F)
        sendActionToActivity(ACTION_PAUSE)
    }

    fun resumeMusic(){
        mediaPlayer.start()
        isPlaying = true
        sendNotification(song)
        sendActionToActivity(ACTION_RESUME)
    }

    fun startNext(){
        if(position == listSong.size - 1)
            position = 0
        song = listSong[++position]
        startMusic()
    }

    fun startPrevious(){
        if(position == 0)
            position = listSong.size
        song = listSong[--position]
        startMusic()
    }

    fun stopMusic(){
        mediaPlayer.release()
        sendActionToActivity(ACTION_CLEAR)
        stopSelf()
    }

    //Service -> Broadcast -> Service
    private fun getPendingIntent(context: Context, action: Int): PendingIntent{
        val intent = Intent(this, MyReceiver::class.java)
        intent.putExtra("action", action)
        return PendingIntent.getBroadcast(context.applicationContext, action, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    fun startMusicPlayerActivity(){
        val intent = Intent(this, MusicPlayerActivity::class.java)
        val bundle = Bundle()
        bundle.putSerializable("song", song)
        bundle.putBoolean("status", isPlaying)
        intent.putExtra("data", bundle)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    fun sendActionToActivity(action: Int){
        val intent = Intent("sendAction")
        intent.putExtra("action", action)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        isRunning = false
    }
}