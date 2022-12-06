package com.example.chillmusic.viewmodel

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.example.chillmusic.model.Song

object SongViewModel : BaseObservable(){
    @set:Bindable
    var song: Song? = null

    @set:Bindable
    var isPlaying = false

    @set:Bindable
    var backgroundColor = 0

    @set:Bindable
    var titleColor = 0

    @set:Bindable
    var artistColor = 0
}