
package com.pulsradio.app

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class RadioPlayer(context: Context) {

    private val player = ExoPlayer.Builder(context).build()

    fun play(url: String) {
        player.stop()
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun stop() {
        player.stop()
    }

    fun isPlaying(): Boolean {
        return player.isPlaying
    }

    fun release() {
        player.release()
    }
}
