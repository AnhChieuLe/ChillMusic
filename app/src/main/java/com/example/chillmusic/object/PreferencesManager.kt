package com.example.chillmusic.`object`

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.chillmusic.R

class PreferencesManager(val context: Context) {
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val autoScan get() = sharedPreferences.getBoolean("auto_scan", true)
    val loadImage get() = sharedPreferences.getBoolean("load_image", true)
    val timeSkip get() = sharedPreferences.getString("time_skip", "60000")?.toInt() ?: 60000
    var navigation: Int
        get() = sharedPreferences.getString("navigation", "0")?.toInt() ?: 0
        set(value) { sharedPreferences.edit().putString("navigation", value.toString()).apply()}
    var volume: Int
        get() = sharedPreferences.getInt("volume", 50)
        set(value) { sharedPreferences.edit().putInt("volume", value).apply() }
    val stopOnDestroy get() = sharedPreferences.getBoolean("stop_on_destroy", false)
    val numberOfSong get() = sharedPreferences.getString("number_of_song", "3")?.toInt() ?: 3
    var musixmatchKey
        get() = sharedPreferences.getString("musixmatch_key", context.getString(R.string.musixmatch_api_key))
        set(value) { sharedPreferences.edit().putString("musixmatch_key", value).apply() }
    var sort: Int
        get() = sharedPreferences.getString("sort", "0")?.toInt() ?: 0
        set(value) { sharedPreferences.edit().putString("sort", value.toString()).apply()}
    var sortType: Int
        get() = sharedPreferences.getString("sort_type", "0")?.toInt() ?: 0
        set(value) { sharedPreferences.edit().putString("sort_type", value.toString()).apply()}
    val bubble get() = sharedPreferences.getBoolean("bubble", false)
}