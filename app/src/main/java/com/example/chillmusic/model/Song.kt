package com.example.chillmusic.model

import android.content.res.Resources
import android.graphics.*
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Parcelable
import com.example.chillmusic.R
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


@Parcelize
data class Song(
    var id: Int = 0,
    var path: String = "",
    var contentUri: Uri,
    var title: String = "",
    var artist: String = "",
    var date: Long = 0,
    var duration: Long = 0L,
    var genre: String = "",
    var lyric: String = "",
    var album: String = "",
    var bitrate: Long = 0,
    @IgnoredOnParcel
    var smallImage: Bitmap? = null,
) : Serializable, Parcelable{
    val image: Bitmap?
        get() {
            val meta = MediaMetadataRetriever().apply { setDataSource(path) }
            return meta.embeddedPicture?.let { decodeSampledBitmapFromByteArray(it, 1024, 1024) }
        }

    @IgnoredOnParcel
    var style: MusicStyle? = null
    val strDuration: String get() = getStringDuration(duration)
    val strDate: String get() = getStringDate(date)
    val fileSize: Double get() = File(path).length() / 1024.0 / 1024.0 // File size in MB

    companion object {
        fun getStringDuration(duration: Long): String {
            val hh: Long = TimeUnit.MILLISECONDS.toHours(duration)
            val mm: Long = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
            val ss: Long = TimeUnit.MILLISECONDS.toSeconds(duration) % 60
            return if (hh != 0L)
                String.format("%02d:%02d:%02d", hh, mm, ss)
            else
                String.format("%02d:%02d", mm, ss)
        }

        fun getStringDate(date: Long): String {
            val resultDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val input = Date(date)
            return resultDateFormat.format(input)
        }

        fun decodeSampledBitmapFromByteArray(byteArray: ByteArray, reqWidth: Int, reqHeight: Int): Bitmap {
            return BitmapFactory.Options().run {
                inJustDecodeBounds = true
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
                inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
                inJustDecodeBounds = false
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
            }
        }

        fun decodeSampledBitmapFromResource(resources: Resources, reqWidth: Int, reqHeight: Int): Bitmap {
            return BitmapFactory.Options().run {
                inJustDecodeBounds = true
                BitmapFactory.decodeResource(resources, R.drawable.avatar2, this)
                inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
                inJustDecodeBounds = false
                BitmapFactory.decodeResource(resources, R.drawable.avatar2, this)
            }
        }

        private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            val (height: Int, width: Int) = options.run { outHeight to outWidth }
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2
                while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                    inSampleSize *= 2
                }
            }

            return inSampleSize
        }
    }

    override fun equals(other: Any?): Boolean {
        return id == (other as Song?)?.id
    }
}
