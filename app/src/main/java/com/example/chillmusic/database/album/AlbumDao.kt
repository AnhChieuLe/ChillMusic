package com.example.chillmusic.database.album

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.chillmusic.model.Album

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAlbum(album: Album)

    @Insert
    fun insertAlbum(vararg albums: Album)

    @Insert
    fun insertAlbum(list: List<Album>)

    @Query("SELECT * FROM album")
    fun getListAlbum():List<Album>

    @Query("SELECT * FROM album WHERE name LIKE :name")
    fun getAlbum(name: String):Album

    @Delete
    fun deleteAlbum(album: Album)

    @Update
    fun updateAlbum(album: Album)

}