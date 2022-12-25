package com.example.chillmusic.`object`

import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song
import com.example.chillmusic.service.NORMAL

object MusicPlayerManager {
    var playList: MutableList<Song> = mutableListOf()
    var position: Int = 0
    val song: Song get() = playList[position]
    val style: MusicStyle? get() = song.style

    var isPlaying: Boolean = false
    var navigationStatus = NORMAL
}