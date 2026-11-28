package com.pulsradio.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.media3.common.C
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import androidx.core.app.NotificationCompat
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.ui.PlayerNotificationManager

/** Foreground service: keeps ExoPlayer alive across rotation and screen-off. */
class RadioPlaybackService : Service() {

    inner class LocalBinder : Binder() {
        fun getService(): RadioPlaybackService = this@RadioPlaybackService
    }

    private val binder = LocalBinder()
    lateinit var player: ExoPlayer
        private set
    private var mediaSession: MediaSession? = null
    private var playerNotificationManager: PlayerNotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        player = ExoPlayer.Builder(this)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            // Pause radio when another app (music, video, navigation) takes audio focus.
            // Do not automatically resume after the other app releases focus.
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
        mediaSession = MediaSession.Builder(this, player).build()

        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        playerNotificationManager = PlayerNotificationManager.Builder(
            this, NOTIFICATION_ID, CHANNEL_ID
        )
            .setMediaDescriptionAdapter(object : PlayerNotificationManager.MediaDescriptionAdapter {
                override fun getCurrentContentTitle(player: Player): CharSequence {
                    return player.mediaMetadata.title ?: "пульс радио"
                }
                override fun createCurrentContentIntent(player: Player): PendingIntent = openApp
                override fun getCurrentContentText(player: Player): CharSequence = "Прямой эфир · пульс радио"
                override fun getCurrentLargeIcon(
                    player: Player,
                    callback: PlayerNotificationManager.BitmapCallback
                ): android.graphics.Bitmap? = null
            })
            .setNotificationListener(object : PlayerNotificationManager.NotificationListener {
                override fun onNotificationPosted(
                    notificationId: Int,
                    notification: Notification,
                    ongoing: Boolean
                ) {
                    startForeground(notificationId, notification)
                }
                override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
                    stopForeground(true)
                }
            })
            .build()
            .apply {
                setMediaSessionToken(mediaSession!!.platformToken)
                setUsePlayPauseActions(true)
                setUsePreviousAction(true)
                setUseNextAction(true)
                setUseStopAction(false)
                setPlayer(player)
            }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start promptly as a foreground service; PlayerNotificationManager replaces this
        // with the media-style notification as soon as playback metadata is available.
        startForeground(NOTIFICATION_ID, buildStartingNotification())
        playerNotificationManager?.invalidate()
        return START_STICKY
    }

    fun updatePlaybackNotification(station: String, playing: Boolean) {
        if (::player.isInitialized && player.mediaItemCount > 0) {
            playerNotificationManager?.invalidate()
        }
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!playing && player.mediaItemCount == 0) {
            manager.notify(NOTIFICATION_ID, buildStartingNotification())
        }
    }

    private fun buildStartingNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("пульс радио")
            .setContentText("Подготовка радио")
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Воспроизведение радио", NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        playerNotificationManager?.setPlayer(null)
        mediaSession?.release()
        if (::player.isInitialized) player.release()
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "puls_radio_playback"
        private const val NOTIFICATION_ID = 4102
    }
}
