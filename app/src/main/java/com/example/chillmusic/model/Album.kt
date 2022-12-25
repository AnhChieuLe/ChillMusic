package com.example.chillmusic.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "album")
data class Album(
    @PrimaryKey
    var name: String,
    var listSong: MutableList<Int> = mutableListOf()
):Serializable
