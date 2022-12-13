package com.example.chillmusic.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioFormat
import android.media.MediaMetadata
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.chillmusic.model.Song
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.io.File

object ScannerMusic {
    private fun requestPermission(){
        val permissionListener = object: PermissionListener {
            override fun onPermissionGranted() {

            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {

            }
        }

        TedPermission.create()
            .setDeniedMessage("Từ chối con cac")
            .setPermissionListener(permissionListener)
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .check()
    }

    @SuppressLint("Range")
    fun getListAudio(context: Context, listID: List<Int>? = null): List<Song> {
        requestPermission()
        val list: MutableList<Song> = ArrayList()
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
        )
        val cursor = context.contentResolver.query(uri, projection, null, null, MediaStore.Audio.Media.DISPLAY_NAME)
        cursor?.let {
            while (cursor.moveToNext()){
                val song = Song()

                song.path = cursor.getString(0) ?: continue
                song.id = cursor.getInt(1)

                val meta = MediaMetadataRetriever()

                try{
                    meta.setDataSource(song.path)
                }catch (_: Exception){
                    Log.d("pathTest", song.path)
                    continue
                }

                song.title = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: cursor.getString(2)
                song.artist = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
                song.duration = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
                song.genre = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: ""

                if(listID != null && !listID.contains(song.id))
                    continue

                if(song.duration < 60000)
                    continue

                list.add(song)
            }
            cursor.close()
        }

        return list
    }
}