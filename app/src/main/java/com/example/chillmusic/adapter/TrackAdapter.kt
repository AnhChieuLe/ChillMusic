package com.example.chillmusic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chillmusic.R
import com.example.chillmusic.databinding.ItemTrackBinding
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.api.track.Track
import com.example.chillmusic.model.api.track.TrackContainer

class TrackAdapter(val context: Context, val style: MusicStyle?) : RecyclerView.Adapter<TrackAdapter.ViewHolder>() {
    var listTrack: List<TrackContainer> = mutableListOf()

    class ViewHolder(val binding: ItemTrackBinding): RecyclerView.ViewHolder(binding.root){
        fun setData(context: Context, track: Track){
            binding.tvTrackName.text = track.track_name
            binding.tvTrackArtist.text = context.getString(R.string.artist, track.artist_name)
            binding.tvTrackAlbum.text = context.getString(R.string.album, track.album_name)
        }

        fun setStyle(style: MusicStyle){
            binding.tvTrackName.setTextColor(style.contentColor)
            binding.tvTrackArtist.setTextColor(style.contentColor)
            binding.tvTrackAlbum.setTextColor(style.contentColor)
        }
    }

    var onItemClick: (Int) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(context, listTrack[position].track)
        style?.let {
            holder.setStyle(it)
        }

        holder.binding.root.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount() = listTrack.size
}