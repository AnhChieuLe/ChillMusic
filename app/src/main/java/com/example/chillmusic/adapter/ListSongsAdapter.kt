package com.example.chillmusic.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chillmusic.R
import com.example.chillmusic.databinding.ItemSongBinding
import com.example.chillmusic.model.MusicStyle
import com.example.chillmusic.model.Song

class ListSongsAdapter(private var context: Context): RecyclerView.Adapter<ListSongsAdapter.ViewHolder>(), Filterable {
    var listSong = mutableListOf<Song>()
    var listSongOld = mutableListOf<Song>()
    var onItemClick: (Int) -> Unit = {}
    var actionMore: (Song) -> Unit = {}

    var currentPosition = -1

    @SuppressLint("NotifyDataSetChanged")
    fun setStyle(style: MusicStyle?, position: Int = -1) {
        currentPosition = position
        style?.let {
            listSong.forEach { it.style = style }
        }
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: ItemSongBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(song: Song, context: Context) {
            binding.tvTitle.text = song.title

            val single = if (song.artist == "") context.getString(R.string.unknown) else song.artist
            val album = if (song.album == "") context.getString(R.string.unknown) else song.album
            binding.tvArtist.text =
                context.getString(R.string.other_info_song, single, song.strDuration)
            binding.tvAlbum.text = context.getString(R.string.album, album)

            if (song.smallImage != null)
                binding.imgAlbumArt.setImageBitmap(song.smallImage)
            else
                binding.imgAlbumArt.setImageResource(R.drawable.music_thumb1)

//            Glide.with(context)
//                .load(song.smallImage)
//                .error(R.drawable.avatar2)
//                .override(50, 50)
//                .into(binding.imgAlbumArt)
        }

        fun setStyle(style: MusicStyle) {
            val color = style.titleColor
            binding.tvTitle.setTextColor(color)
            binding.tvArtist.setTextColor(color)
            binding.tvAlbum.setTextColor(color)
            DrawableCompat.setTint(binding.imgMore.drawable.mutate(), color)
        }

        fun setCurrentStyle(style: MusicStyle) {
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
        with(holder.binding) {
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
            if (position != currentPosition)
                holder.setStyle(it)
            else
                holder.setCurrentStyle(it)
        }
    }

    override fun getItemCount() = listSong.size
    override fun getFilter(): Filter {
        return object : Filter(){
            override fun performFiltering(p0: CharSequence?): FilterResults {
                val strSearch = p0.toString()
                listSong = if(strSearch.isEmpty()){
                    listSongOld
                }else{
                    listSongOld.filter { it.title.contains(strSearch, ignoreCase = true) } as MutableList<Song>
                }

                val filterResults = FilterResults()
                filterResults.values = listSong
                return filterResults
            }

            override fun publishResults(p0: CharSequence?, p1: FilterResults?) {
                listSong = p1?.values as MutableList<Song>
                notifyDataSetChanged()
            }
        }
    }
}