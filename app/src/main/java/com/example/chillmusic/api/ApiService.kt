package com.example.chillmusic.api

import com.example.chillmusic.model.api.lyrics.LyricsResponse
import com.example.chillmusic.model.api.track.TrackResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    val apikey get() = "42e009aba3a6869507e21faccda666cb"
    @GET("matcher.lyrics.get?")
    fun getLyric(
        @Query("apikey") apikey: String = "42e009aba3a6869507e21faccda666cb",
        @Query("q_track") q_track: String,
        @Query("q_artist") q_artist: String
    ): Call<LyricsResponse>

    @GET("track.search?apikey=42e009aba3a6869507e21faccda666cb&q_track=nang%20tho&page_size=3&page=1&s_track_rating=desc")
    fun getTrack(): Call<TrackResponse>

    @GET("track.search?")
    fun getTrack(
        @Query("apikey") apikey: String = "42e009aba3a6869507e21faccda666cb",
        @Query("q_track") q_track: String,
        @Query("q_artist") q_artist: String = "",
        @Query("page_size") page_size: Int = 3,
        @Query("page") page: Int = 1,
        @Query("s_track_rating") rating:String = "desc"
    ): Call<TrackResponse>

    @GET("track.lyrics.get?")
    fun getLyric(
        @Query("apikey") apikey: String = "42e009aba3a6869507e21faccda666cb",
        @Query("track_id") track_id: String
    ): Call<LyricsResponse>
}