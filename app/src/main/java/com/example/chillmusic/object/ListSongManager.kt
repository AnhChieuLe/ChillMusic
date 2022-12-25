package com.example.chillmusic.`object`

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadata
import android.media.MediaMetadataEditor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.chillmusic.model.Song
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.io.File

object ListSongManager {
    var listSong = mutableListOf<Song>()
        get() {
            field.sortBy { it.title.first() }
            return field
        }
    fun setListAudio(context: Context, action: (List<Song>) -> Unit = {}){
        val list: MutableList<Song> = ArrayList()
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media.DATA, //path
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.ALBUM,
        )
        val selection = "${MediaStore.Audio.Media.DURATION} >= 60000"
        val oder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"
        val query = context.contentResolver.query(uri, projection, null, null, oder)
        query?.use { cursor ->
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)

            while (cursor.moveToNext()) {
                val path = cursor.getString(pathColumn) ?: continue
                val id = cursor.getInt(idColumn)
                val title = cursor.getString(titleColumn) ?: continue
                val artist = cursor.getString(artistColumn) ?: ""
                val duration = cursor.getLong(durationColumn) ?: 0
                val album = cursor.getString(albumColumn) ?: ""
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id.toLong()
                )

                //val song = Song(id, path, contentUri, title, artist, duration, album = album)
                val song = getSongWithMetadata(context, id, path)

                song?.let { list.add(it) }
            }
            cursor.close()
        }

        action(list)
        listSong = list
    }

    private fun getSongWithMetadata(context: Context, id: Int, path: String): Song?{
        val meta = MediaMetadataRetriever()
        try { meta.setDataSource(path) } catch (_: Exception) { return null}
        val contentUri = ContentUris.withAppendedId(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            id.toLong()
        )
        val title = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: File(path).nameWithoutExtension
        val artist = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
        val duration = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
        if(duration < 60000)    return null
        val genre = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE) ?: ""
        val album = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""
        val bitrate = meta.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLong() ?: 0
        val smallImage = if(meta.embeddedPicture == null)
            Song.decodeSampledBitmapFromResource(context.resources, 120, 120)
        else
            Song.decodeSampledBitmapFromByteArray(meta.embeddedPicture!!, 120, 120)

        return Song(id, path, contentUri, title, artist, duration, genre, "", album, bitrate, smallImage)
    }

    fun getSongFromID(listID: List<Int>): MutableList<Song> {
        val list = mutableListOf<Song>()
        for (song in listSong)
            if (listID.contains(song.id))
                list.add(song)
        return list
    }

    fun requestPermission(actionGranted: () -> Unit = {}, actionDenied: () -> Unit = {}){
        val permissionListener = object: PermissionListener {
            override fun onPermissionGranted() {
                actionGranted()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                actionDenied()
            }
        }

        TedPermission.create()
            .setDeniedMessage("Từ chối con cac")
            .setPermissionListener(permissionListener)
            .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .check()
    }
}