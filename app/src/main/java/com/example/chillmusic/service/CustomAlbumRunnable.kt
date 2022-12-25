package com.example.chillmusic.service

import android.content.Context
import androidx.room.Room
import com.example.chillmusic.database.album.AlbumDao
import com.example.chillmusic.database.album.AlbumDatabase
import com.example.chillmusic.model.Album
import kotlinx.coroutines.Runnable


const val ACTION_GET = 1
const val ACTION_INSERT = 2
const val ACTION_UPDATE = 3
const val ACTION_DELETE = 4

class CustomAlbumRunnable(val context: Context, action: Int = 0): Runnable {
    var listAlbum: MutableList<Album> = mutableListOf()
    lateinit var albumDao: AlbumDao
    override fun run() {
        albumDao = Room.databaseBuilder(context, AlbumDatabase::class.java, "album").build().AlbumDao()
        listAlbum = albumDao.getListAlbum() as MutableList<Album>
    }
}