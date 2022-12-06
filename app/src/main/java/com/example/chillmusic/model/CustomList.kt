package com.example.chillmusic.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CustomList(
    var list: List<Parcelable> = mutableListOf()
) : Parcelable {}