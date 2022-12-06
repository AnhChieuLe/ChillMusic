package com.example.chillmusic.application

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import com.example.chillmusic.R
import com.example.chillmusic.database.album.AlbumDatabase
import com.example.chillmusic.model.Album
import com.example.chillmusic.service.ScannerMusic
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission

const val CHANNEL_MEDIA_PLAYER = "CHANNEL_MEDIA_PLAYER"

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChanel()
        createFavoriteAlbum()
    }

    private fun createNotificationChanel() {
        val channel = NotificationChannel(CHANNEL_MEDIA_PLAYER, getString(R.string.channel_media_player), NotificationManager.IMPORTANCE_DEFAULT)
        channel.setSound(null, null)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createFavoriteAlbum(){
        val db = Room.databaseBuilder(applicationContext, AlbumDatabase::class.java, "album")
            .allowMainThreadQueries()
            .build()
        val dao = db.AlbumDao()
        val album = Album("Yêu thích")
        dao.insertAlbum(album)
    }
}