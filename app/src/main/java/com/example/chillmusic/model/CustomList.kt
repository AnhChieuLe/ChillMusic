package com.example.chillmusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class CustomList(
    var list: List<Song> = mutableListOf()
) : Parcelable, Serializable {}