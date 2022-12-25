package com.example.chillmusic.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chillmusic.R
import com.example.chillmusic.databinding.ItemSongBinding
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song

class ListSongsAdapter(private var context: Context): RecyclerView.Adapter<ListSongsAdapter.ViewHolder>(){
    var listSong: List<Song> = mutableListOf()
    var onItemClick: (Int) -> Unit = {}
    var actionMore: (Song) -> Unit = {}

    var currentPosition = -1
    @SuppressLint("NotifyDataSetChanged")
    fun setStyle(style: MusicStyle?, position: Int = -1){
        currentPosition = position
        listSong.forEach { it.style = style }
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root){
        fun setData(song: Song, context: Context){
            binding.tvTitle.text = song.title

            val single = if(song.artist == "") context.getString(R.string.unknown) else song.artist
            val album = if(song.album == "") context.getString(R.string.unknown) else song.album
            binding.tvArtist.text = context.getString(R.string.other_info_song, single, song.strDuration)
            binding.tvAlbum.text = context.getString(R.string.album, album)

            if(song.smallImage != null)
                binding.imgAlbumArt.setImageBitmap(song.smallImage)
            else
                binding.imgAlbumArt.setImageResource(R.drawable.music_thumb1)
        }

        fun setStyle(style: MusicStyle){
            val color = style.titleColor ?: style.contentColor
            binding.tvTitle.setTextColor(color)
            binding.tvArtist.setTextColor(color)
            binding.tvAlbum.setTextColor(color)
            DrawableCompat.setTint(binding.imgMore.drawable.mutate(), color)
        }

        fun setCurrentStyle(style: MusicStyle){
            val color = style.contentColor
            binding.tvTitle.setTextColor(color)
            binding.tvArtist.setTextColor(color)
            binding.tvAlbum.setTextColor(color)
            DrawableCompat.setTint(binding.imgMore.drawable.mutate(), color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            lnSong.setOnClickListener {
                onItemClick(position)
            }

            imgMore.setOnClickListener {
                val song: Song = listSong[position]
                actionMore(song)
            }

            imgFavorite.visibility = View.GONE
        }
        holder.setData(listSong[position], context)

        listSong[position].style?.let {
            if(position != currentPosition)
                holder.setStyle(it)
            else
                holder.setCurrentStyle(it)
        }
    }

    override fun getItemCount() = listSong.size
}