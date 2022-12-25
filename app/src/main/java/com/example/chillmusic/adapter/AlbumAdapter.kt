package com.example.chillmusic.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.chillmusic.R
import com.example.chillmusic.databinding.ItemAlbumBinding
import com.example.chillmusic.model.Album
import com.example.chillmusic.model.MusicStyle

class AlbumAdapter(
    private val context: Context
) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    var list:List<Album> = mutableListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var style: MusicStyle? = null
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var onItemClick: (position: Int, binding: ItemAlbumBinding) -> Unit = { _, _ -> }
    var onPlayClick: (List<Int>) -> Unit = {}

    class ViewHolder(var binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root){
        fun setData(album: Album, context: Context){
            binding.imgAlbumArt.setImageResource(R.drawable.avatar2)
            binding.tvTitle.text = album.name
            binding.tvNumberOfSong.text = context.getString(R.string.numb_of_song, album.listSong.size.toString())
        }

        fun setStyle(style: MusicStyle){
            binding.tvTitle.setTextColor(style.titleColor)
            binding.tvNumberOfSong.setTextColor(style.titleColor)
            DrawableCompat.setTint(binding.btnPlay.drawable.mutate(), style.titleColor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(list[position], context)
        style?.let { holder.setStyle(it) }

        holder.binding.rtlAlbum.setOnClickListener {
            onItemClick(position, holder.binding)
        }

        holder.binding.btnPlay.setOnClickListener {
            onPlayClick(list[position].listSong)
        }
    }

    override fun getItemCount(): Int = list.size
}