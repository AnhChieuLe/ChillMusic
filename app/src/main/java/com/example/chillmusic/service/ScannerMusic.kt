package com.example.chillmusic.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.chillmusic.model.Song
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission


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
            MediaStore.Audio.Media.DURATION
        )
        val cursor = context.contentResolver.query(uri, projection, null, null, MediaStore.Audio.Media.DISPLAY_NAME)
        val meta = MediaMetadataRetriever()
        cursor?.let {
            while (cursor.moveToNext()){
                val song = Song()

                song.path = cursor.getString(0) ?: ""
                song.id = cursor.getInt(1)

                meta.setDataSource(song.path)

                song.title = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: cursor.getString(2)
                song.artist = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
                song.duration = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L

                song.imageByte = meta.embeddedPicture

                if(song.duration < 60000)
                    continue
                if(listID != null && !listID.contains(song.id))
                    continue

                list.add(song)
            }
            cursor.close()
        }

        return list
    }
}