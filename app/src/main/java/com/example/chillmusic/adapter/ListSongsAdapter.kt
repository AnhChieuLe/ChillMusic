package com.example.chillmusic.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.chillmusic.R
import com.example.chillmusic.database.album.AlbumDatabase
import com.example.chillmusic.databinding.ItemSongBinding
import com.example.chillmusic.model.Album
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song

class ListSongsAdapter(
    private var context: Context,
    private var listener: OnItemClickListener,
) : RecyclerView.Adapter<ListSongsAdapter.ViewHolder>(){

    interface OnItemClickListener{
        fun onSongClick(position: Int)
        fun onButtonAddClick(position: Int)
    }

    var listSong: List<Song> = mutableListOf()
        set(value) {
            field = value
            for(song in field)
                song._image = song.image?.let { Bitmap.createScaledBitmap(it, 120, 120, false) }
        }

    var style: MusicStyle? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var favoriteAlbum: Album = _album
    private val _album: Album get() = Room.databaseBuilder(context, AlbumDatabase::class.java, "album")
        .allowMainThreadQueries()
        .build()
        .AlbumDao()
        .getAlbum("Yêu Thích")

    class ViewHolder(var binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root){
        fun setData(song: Song, context: Context, style: MusicStyle?){
            binding.tvTitle.text = song.title

            val single = if(song.artist == "") "Không rõ" else song.artist
            val genre = if(song.genre == "") "Không rõ" else song.genre
            binding.tvArtist.text = context.getString(R.string.other_info_song, single, genre, song.strDuration)

            if(song._image != null)
                binding.imgAlbumArt.setImageBitmap(song._image)
            else
                binding.imgAlbumArt.setImageResource(R.drawable.music_thumb1)

            if(style != null){
                binding.tvTitle.setTextColor(style.contentColor)
                binding.tvArtist.setTextColor(style.contentColor)
                DrawableCompat.setTint(binding.imgMore.drawable.mutate(), style.contentColor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder.binding){
            lnSong.setOnClickListener {
                listener.onSongClick(position)
            }

            imgMore.setOnClickListener {
                listener.onButtonAddClick(position)
                notifyItemChanged(position)
            }

            imgFavorite.visibility = View.GONE
        }
        holder.setData(listSong[position], context, style)
    }

    override fun getItemCount(): Int {
        return listSong.size
    }
}