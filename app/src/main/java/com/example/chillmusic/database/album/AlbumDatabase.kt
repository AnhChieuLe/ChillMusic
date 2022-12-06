package com.example.chillmusic.database.album

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.chillmusic.database.TypeConvert
import com.example.chillmusic.model.Album

@Database(entities = [Album::class], version = 1)
@TypeConverters(TypeConvert::class)
abstract class AlbumDatabase : RoomDatabase(){
    abstract fun AlbumDao() : AlbumDao
}