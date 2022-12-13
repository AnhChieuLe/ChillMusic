package com.example.chillmusic.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chillmusic.R
import com.example.chillmusic.databinding.ItemAlbumBinding
import com.example.chillmusic.model.Album

class AlbumAdapter(
    private val context: Context,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {

    var list:List<Album> = mutableListOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    interface OnItemClickListener{
        fun onAlbumClick(position: Int)
        fun onButtonPlayClick(position: Int)
    }

    class ViewHolder(var binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root){
        fun setData(album: Album, context: Context){
            binding.imgAlbumArt.setImageResource(R.drawable.avatar2)
            binding.tvTitle.text = album.name
            binding.tvNumberOfSong.text = context.getString(R.string.numb_of_song, album.listSong.size.toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setData(list[position], context)

        holder.binding.rtlAlbum.setOnClickListener {
            listener.onAlbumClick(position)
            notifyItemChanged(position)
        }

        holder.binding.btnPlay.setOnClickListener {
            listener.onButtonPlayClick(position)
        }
    }

    override fun getItemCount(): Int = list.size
}