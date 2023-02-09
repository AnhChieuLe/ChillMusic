package com.example.chillmusic.service

import android.app.Service
import android.content.Intent
import android.media.browse.MediaBrowser
import android.os.Bundle
import android.os.IBinder
import android.service.media.MediaBrowserService

class MusicService : MediaBrowserService() {
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowser.MediaItem>>) {
        TODO("Not yet implemented")
    }
}