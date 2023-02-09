package com.example.chillmusic.`object`

import android.content.Context
import androidx.room.Room
import com.example.chillmusic.database.album.AlbumDao
import com.example.chillmusic.database.album.AlbumDatabase
import com.example.chillmusic.model.Album

object AlbumManager {
    fun getListAlbum(context: Context, action: (List<Album>) -> Unit = {}): MutableList<Album> {
        val run = object : Runnable {
            lateinit var listAlbum: MutableList<Album>
            override fun run() {
                val albumDao =
                    Room.databaseBuilder(context, AlbumDatabase::class.java, "album").build()
                        .AlbumDao()
                listAlbum = albumDao.getListAlbum() as MutableList<Album>
            }
        }
        val thread = Thread(run)
        thread.start()
        thread.join()
        action(run.listAlbum)
        return run.listAlbum
    }

    fun getAlbumDao(context: Context): AlbumDao {
        val run = object : Runnable {
            lateinit var albumDao: AlbumDao
            override fun run() {
                albumDao = Room.databaseBuilder(context, AlbumDatabase::class.java, "album").build()
                    .AlbumDao()
            }
        }
        val thread = Thread(run)
        thread.start()
        thread.join()
        return run.albumDao
    }

    fun updateAlbum(context: Context, album: Album) {
        val thread = Thread {
            run {
                Room.databaseBuilder(context, AlbumDatabase::class.java, "album")
                    .build()
                    .AlbumDao()
                    .updateAlbum(album)
            }
        }
        thread.start()
    }

    fun deleteAlbum(context: Context, album: Album) {
        val thread = Thread {
            run {
                Room.databaseBuilder(context, AlbumDatabase::class.java, "album")
                    .build()
                    .AlbumDao()
                    .deleteAlbum(album)
            }
        }
        thread.start()
    }

    fun insertAlbum(context: Context, album: Album) {
        val thread = Thread {
            run {
                Room.databaseBuilder(context, AlbumDatabase::class.java, "album")
                    .build()
                    .AlbumDao()
                    .insertAlbum(album)
            }
        }
        thread.join()
        thread.start()
    }
}