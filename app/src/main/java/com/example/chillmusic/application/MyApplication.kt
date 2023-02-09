package com.example.chillmusic.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.room.Room
import com.example.chillmusic.R
import com.example.chillmusic.database.album.AlbumDatabase
import com.example.chillmusic.model.Album
import com.example.chillmusic.`object`.ListSongManager
import com.example.chillmusic.`object`.PreferencesManager

const val CHANNEL_MEDIA_PLAYER = "CHANNEL_MEDIA_PLAYER"
const val CHANNEL_TEST = "CHANNEL_TEST"

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChanel()
        createNotificationChanel2()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
    private fun createNotificationChanel() {
        val channel = NotificationChannel(CHANNEL_MEDIA_PLAYER, getString(R.string.channel_media_player), NotificationManager.IMPORTANCE_DEFAULT)
        channel.setSound(null, null)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
    private fun createNotificationChanel2() {
        val channel = NotificationChannel(CHANNEL_TEST,"Test bubble", NotificationManager.IMPORTANCE_DEFAULT)
        channel.setSound(null, null)
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

}