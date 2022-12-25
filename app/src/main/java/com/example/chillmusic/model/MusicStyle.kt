package com.example.chillmusic.model

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.palette.graphics.Palette
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.Serializable

class MusicStyle(bitmap: Bitmap) : Serializable {
    var titleColor: Int
    var bodyTextColor: Int
    var backgroundColor: Int
    var contentColor: Int

    init {
        val palette = Palette.from(bitmap).clearFilters().generate()
        val swatch1 = palette.darkMutedSwatch ?: palette.mutedSwatch ?: palette.lightMutedSwatch ?: palette.dominantSwatch

        val listAllMuted = ArrayList<Int>()
        val listAllVibrant = ArrayList<Int>()
        val listAllColor = ArrayList<Int>()

        with(palette) {
            darkVibrantSwatch?.rgb?.let { listAllVibrant.add(it) }
            vibrantSwatch?.rgb?.let { listAllVibrant.add(it) }
            lightVibrantSwatch?.rgb?.let { listAllVibrant.add(it) }
            darkMutedSwatch?.rgb?.let { listAllMuted.add(it) }
            mutedSwatch?.rgb?.let { listAllMuted.add(it) }
            lightMutedSwatch?.rgb?.let { listAllMuted.add(it) }
        }
        listAllColor.addAll(listAllMuted)
        listAllColor.addAll(listAllVibrant)


        titleColor = swatch1?.titleTextColor ?: 0
        bodyTextColor = swatch1?.bodyTextColor ?: 0

        backgroundColor = listAllMuted.min()
        contentColor = if (backgroundColor == listAllColor.max())
            listAllVibrant.max()
        else
            listAllColor.max()
    }
}