package com.example.chillmusic.activity

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chillmusic.adapter.TrackAdapter
import com.example.chillmusic.api.API
import com.example.chillmusic.databinding.ActivityUpdateMetadataBinding
import com.example.chillmusic.model.Song
import com.example.chillmusic.model.api.track.TrackContainer
import com.example.chillmusic.model.api.track.TrackResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UpdateMetaData : AppCompatActivity() {
    private var _binding: ActivityUpdateMetadataBinding? = null
    private val binding: ActivityUpdateMetadataBinding get() = _binding!!

    val bundle get() = intent.getBundleExtra("data")
    val song get() = bundle?.getSerializable("song") as Song?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityUpdateMetadataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        song?.let {
            setInfo(it)
            setEvent()
        }
    }

    private fun setEvent() {
        binding.btnSearch.setOnClickListener {
            searchTrack()
        }

        binding.btnUpdate.setOnClickListener {
            //updateMetadata(song!!, Song(title = binding.edtTitle.text.toString()))
        }
    }

    private fun updateMetadata(song: Song, updateInfo: Song) {
        val mediaId = song.id
        val resolver = applicationContext.contentResolver

        // When performing a single item update, prefer using the ID
        val selection = "${MediaStore.Audio.Media._ID} = ?"

        // By using selection + args we protect against improper escaping of // values.
        val selectionArgs = arrayOf(mediaId.toString())

        // Update an existing song.
        val updatedSongDetails = ContentValues().apply {
            put(MediaStore.Audio.Media.ARTIST, "con cac")
        }

        // Use the individual song's URI to represent the collection that's
        // updated.
        val numSongsUpdated = resolver.update(
            song.contentUri,
            updatedSongDetails,
            selection,
            selectionArgs)

        Log.d("numSongsUpdated", numSongsUpdated.toString())

    }

    private fun searchTrack() {
        val callback = object : Callback<TrackResponse> {
            override fun onResponse(call: Call<TrackResponse>, response: Response<TrackResponse>) {
                if (!response.isSuccessful) return
                val result = response.body()
                val trackList = result?.message?.body?.track_list
                if (trackList != null && trackList.isNotEmpty()) {
                    setRecyclerview(trackList)
                }
            }

            override fun onFailure(call: Call<TrackResponse>, t: Throwable) {

            }
        }
        API.apiService.getTrack(
            q_track = binding.edtTitle.text.toString(),
            q_artist = binding.edtArtist.text.toString(),
            page_size = 5
        ).enqueue(callback)
    }

    private fun setRecyclerview(list: List<TrackContainer>) {
        val adapter = TrackAdapter(applicationContext, null)
        adapter.listTrack = list
        binding.rcvResult.adapter = adapter
        binding.rcvResult.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
    }

    private fun setInfo(song: Song) {
        binding.edtTitle.setText(song.title)
        binding.edtArtist.setText(song.artist)
    }
}