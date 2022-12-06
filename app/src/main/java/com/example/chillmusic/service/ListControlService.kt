package com.example.chillmusic.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.widget.Toast
import com.example.chillmusic.model.Song

class ListControlService : Service() {
    private lateinit var listSong:List<Song>
    private var position = 0

    private val binder = MyBinder()

    inner class MyBinder:Binder(){
        fun getService() = this@ListControlService
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        listSong = intent?.getSerializableExtra("song") as MutableList<Song>
        position = intent.getIntExtra("position", 0)

        return START_NOT_STICKY
    }

    fun startMusicPlayerActivity(){

    }

    fun startMusicPlayerService(){

    }
}