package com.example.chillmusic.model.api.lyrics

import com.example.chillmusic.model.api.Header

data class LyricsMessage(
    val header: Header,
    val body: LyricsBody
)
