package com.example.chillmusic.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chillmusic.R
import com.example.chillmusic.databinding.ItemSongBinding
import com.example.chillmusic.model.Song

class ListSongsAdapter(
    private var context: Context,
    private var listener: OnItemClickListener,
    private var listSong: List<Song>
) : RecyclerView.Adapter<ListSongsAdapter.ViewHolder>(){

    interface OnItemClickListener{
        fun onSongClick(position: Int)
        fun onLongClick(position: Int)
        fun onButtonAddClick(position: Int)
        fun onButtonFavoriteClick(position: Int)
    }

    class ViewHolder(var binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root){
        fun setData(song: Song, context: Context){
            with(binding){
                tvTitle.text = song.title

                val single = if(song.artist == "") "Không rõ" else song.artist
                tvArtist.text = context.getString(R.string.other_info_song, single, song.strDuration)

                if(song.image != null)
                    imgAlbumArt.setImageBitmap(song.image)
                else
                    imgAlbumArt.setImageResource(R.drawable.music_thumb1)
            }
        }

        fun setEvent(position: Int, listener: OnItemClickListener){
            with(binding){
                lnSong.setOnClickListener {
                    listener.onSongClick(position)
                }

                imgAdd.setOnClickListener {
                    listener.onButtonAddClick(position)
                }

                imgFavorite.setOnClickListener {
                    listener.onButtonFavoriteClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setEvent(position, listener)
        holder.setData(listSong[position], context)
    }

    override fun getItemCount(): Int {
        return listSong.size
    }
}