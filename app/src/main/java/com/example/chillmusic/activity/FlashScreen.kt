package com.example.chillmusic.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chillmusic.`object`.ListSongManager

class FlashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ListSongManager.requestPermission(actionGranted = {
            ListSongManager.setListAudio(applicationContext)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, {finish()})
    }
}