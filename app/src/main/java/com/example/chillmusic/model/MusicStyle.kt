package com.example.chillmusic.model

import android.graphics.Bitmap
import androidx.palette.graphics.Palette

class MusicStyle(bitmap: Bitmap) {
    private var palette = Palette.from(bitmap).clearFilters().generate()
    private var swatch1 = palette.darkMutedSwatch ?: palette.mutedSwatch ?: palette.lightMutedSwatch ?: palette.dominantSwatch
    private var swatch2 = palette.lightVibrantSwatch ?: palette.vibrantSwatch ?: palette.darkVibrantSwatch ?: palette.dominantSwatch

    private val listAllMuted = ArrayList<Int>()
    private val listAllVibrant = ArrayList<Int>()
    private val listAllColor = ArrayList<Int>()

    init {
        palette.darkVibrantSwatch?.rgb?.let { listAllVibrant.add(it) }
        palette.vibrantSwatch?.rgb?.let { listAllVibrant.add(it) }
        palette.lightVibrantSwatch?.rgb?.let { listAllVibrant.add(it) }
        palette.darkMutedSwatch?.rgb?.let { listAllMuted.add(it) }
        palette.mutedSwatch?.rgb?.let { listAllMuted.add(it) }
        palette.lightMutedSwatch?.rgb?.let { listAllMuted.add(it) }

        listAllColor.addAll(listAllMuted)
        listAllColor.addAll(listAllVibrant)
    }

    val backgroundColor: Int get() = listAllColor.min()
    val contentColor: Int get() = listAllColor.max()
    val titleColor: Int? get() = swatch1?.titleTextColor

    val isLightBackground: Boolean get() = swatch1 == palette.lightMutedSwatch
    val isDarkBackground: Boolean get() = swatch1 == palette.darkMutedSwatch
}