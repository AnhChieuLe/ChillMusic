package com.example.chillmusic.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Parcelize
data class Song(
    var id: Int = 0,
    var title: String = "",
    var artist: String = "",
    var path: String = "",
    var duration: Long = 0,
    var imageByte: ByteArray? = null
) : Serializable, Parcelable{
    val strDuration:String
        get(){
            val format = SimpleDateFormat("mm:ss", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = duration
            return format.format(calendar.time)
        }

    val image: Bitmap?
        get() = imageByte?.let {
            BitmapFactory.decodeByteArray(it, 0, it.size)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Song

        if (id != other.id) return false
        if (title != other.title) return false
        if (artist != other.artist) return false
        if (path != other.path) return false
        if (duration != other.duration) return false
        if (imageByte != null) {
            if (other.imageByte == null) return false
            if (!imageByte.contentEquals(other.imageByte)) return false
        } else if (other.imageByte != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + title.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + path.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + (imageByte?.contentHashCode() ?: 0)
        return result
    }
}
