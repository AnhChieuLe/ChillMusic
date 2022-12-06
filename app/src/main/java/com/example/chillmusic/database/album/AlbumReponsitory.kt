package com.example.chillmusic.database.album

import com.example.chillmusic.model.Album

class AlbumRepository(var albumDao: AlbumDao) {
    val getListAlbum = albumDao.getListAlbum()

    fun insertAlbum(album: Album){
        albumDao.insertAlbum(album)
    }
}