package com.example.chillmusic.model.api.track

import com.example.chillmusic.model.api.Header

data class TrackMessage(
    val header: Header,
    val body: TrackBody
)
