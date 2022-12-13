package com.example.chillmusic.model

import android.graphics.*
import android.media.MediaMetadataRetriever
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*


@Parcelize
data class Song(
    var id: Int = 0,
    var path: String = "",
    var title: String = "",
    var artist: String = "",
    var duration: Long = 0L,
    var genre: String = "",
    var lyric: String = "",
    var _image: Bitmap? = null
) : Serializable, Parcelable{
    val image: Bitmap?
        get(){
            val meta = MediaMetadataRetriever()
            meta.setDataSource(path)
            return meta.embeddedPicture?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            }
        }
    val circleImage: Bitmap?
        get() {
            return getCircularBitmap(image)
        }

    fun isExistInAlbum(album: Album) = album.listSong.contains(id)

    val strDuration: String
        get() {
            val format = SimpleDateFormat("mm:ss", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = duration
            return format.format(calendar.time)
        }

    fun getCircularBitmap(bitmap: Bitmap?): Bitmap? {
        if(bitmap == null)  return null

        val output: Bitmap = if (bitmap.width > bitmap.height) {
            Bitmap.createBitmap(bitmap.height, bitmap.height, Bitmap.Config.ARGB_8888)
        } else {
            Bitmap.createBitmap(bitmap.width, bitmap.width, Bitmap.Config.ARGB_8888)
        }
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        var r = 0f
        r = if (bitmap.width > bitmap.height) {
            (bitmap.height / 2).toFloat()
        } else {
            (bitmap.width / 2).toFloat()
        }
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle(r, r, r, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }
}
