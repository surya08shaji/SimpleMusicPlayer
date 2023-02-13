package com.example.music

import android.app.*
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaSessionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.io.IOException

open class MediaplayerService : Service(), ServiceConnection {

    val ACTION_PLAY = "ACTION_PLAY"
    val ACTION_PAUSE = "ACTION_PAUSE"
    val ACTION_PREVIOUS = "ACTION_PREVIOUS"
    val ACTION_NEXT = "ACTION_NEXT"
    val ACTION_STOP = "ACTION_STOP"
    val ACTION_STARTFOREGROUND = "ACTION_STARTFOREGROUND"

    var isPlayerStarted = false
    private var mediaSessionManager: MediaSessionManager? = null
    private var mediaSession: MediaSessionCompat? = null
    private val NOTIFICATION_ID = 101
    private var transportControls: MediaControllerCompat.TransportControls? = null
    private val audioManager: AudioManager? = null
//    private lateinit var mediaPlayer: MediaPlayer
private var mediaPlayer = MediaPlayer()
    private var stop: Boolean = false
    private val notificationManager: NotificationManager? = null

    var showName = ""
    var fromuser = false

    private val iBinder: IBinder = LocalBinder()
    private var mServiceCallBacks: ServiceCallBacks? = null


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent!!.action){
            ACTION_STARTFOREGROUND -> {
                showNotification()
                Toast.makeText(this,"Service Started",Toast.LENGTH_SHORT).show()
            }
            ACTION_PLAY -> {
                Toast.makeText(this, "Clicked Play", Toast.LENGTH_SHORT).show()
            }
            ACTION_PAUSE -> {
                Toast.makeText(this, "Clicked Pause", Toast.LENGTH_SHORT).show()
            }
            ACTION_NEXT -> {
                Toast.makeText(this, "Clicked Next", Toast.LENGTH_SHORT).show()
            }
        }
        return START_STICKY
    }

    class LocalBinder : Binder() {
        fun getService(): MediaplayerService {
            try {
                Thread.sleep(5000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            return getService()
        }
    }

//    class LocalBinder : Binder() {
//    fun getService(): MediaplayerService {
//
//        try {
//            Thread.sleep(5000)
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//        }
//
//        return getService()
//    }
//}

    override fun onBind(intent: Intent?): IBinder {
        return iBinder
    }

//    fun onPrepared(mediaPlayer: MediaPlayer) {
//        mediaPlayer.start()
//        val intent = Intent(this, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        showNotification()
//    }

     private fun getPendingIntentPlay(): PendingIntent? {
        val pendingIntentPlay: PendingIntent
        val intentPlay = Intent(
            applicationContext,
            MediaplayerService::class.java
        ).setAction(ACTION_PLAY)
        pendingIntentPlay = PendingIntent.getActivity(
            applicationContext,
            0,
            intentPlay,
            PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntentPlay
    }
    private fun getPendingIntentPause(): PendingIntent? {
        val pendingIntentPause : PendingIntent
        val intentPause = Intent(
            applicationContext,
            MediaplayerService::class.java
        ).setAction(ACTION_PAUSE)
        pendingIntentPause = PendingIntent.getActivity(
            applicationContext, 0,
            intentPause,
            PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntentPause
    }

    private fun showNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val NOTIFICATION_CHANNEL_ID = "com.example.Music"
            val channelName = "My Notification Service"

            val drwPlay : Int
            drwPlay = R.drawable.play_button

            val drwPause : Int
            drwPause = R.drawable.pause_button

            val chan = NotificationChannel(NOTIFICATION_CHANNEL_ID,channelName,
            NotificationManager.IMPORTANCE_HIGH)
            notificationManager!!.createNotificationChannel(chan)
            val notificationBuilder = NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID)
            val notification:Notification = notificationBuilder.setOngoing(false)
                .setSmallIcon(R.drawable.musicss)
                .addAction(drwPlay,"Play",getPendingIntentPlay())
                .addAction(drwPause,"Pause",getPendingIntentPause())
                .build()
//            stopForeground(2,notification)
        }
    }

    fun playAudio() {

            val audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            mediaPlayer = MediaPlayer()

            mediaPlayer.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            try {
                mediaPlayer.setDataSource(audioUrl)
                mediaPlayer.prepare()
                mediaPlayer.start()

            } catch (e: IOException) {
                e.printStackTrace()
            }
    }

//    private fun initMediaSession() {
//
//        // Attach Callback to receive MediaSession updates
//        mediaSession!!.setCallback(object : MediaSessionCompat.Callback() {
//            // Implement callbacks
//            override fun onPlay() {
//                super.onPlay()
//                playMedia()
//            }
//
//            override fun onPause() {
//                super.onPause()
//                pauseMedia()
//            }
//
//            override fun onStop() {
//                super.onStop()
//                //Stop the service
//                //stopMedia();
//                stopSelf()
//            }
//
//            override fun onSeekTo(position: Long) {
//                super.onSeekTo(position)
//            }
//        })
//    }

    private fun handleIncomingActions(intent: Intent?) {

        when (intent!!.action) {
            ACTION_PLAY -> {
                mServiceCallBacks!!.play()
                Toast.makeText(this, "Clicked Play", Toast.LENGTH_SHORT).show()
            }
            ACTION_PAUSE -> {
                mServiceCallBacks!!.pause()
                Toast.makeText(this, "Clicked Pause", Toast.LENGTH_SHORT).show()
            }
        }
    }

//    override fun onDestroy() {
//        mediaPlayer = MediaPlayer()
//        super.onDestroy()
//        if (mediaPlayer != null) {
//            mediaPlayer.release()
//        }
//    }

    fun pauseMedia() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                isPlayerStarted = false
            }
            mServiceCallBacks?.pause()
//            buildNotification(false)
        }
    }

    fun playMedia() {
        if (mediaPlayer != null) {
            if (!mediaPlayer!!.isPlaying) {
                mediaPlayer!!.start()
                isPlayerStarted = true
            }
            mServiceCallBacks?.play()
//            buildNotification(true)
        }
    }
//    fun stopMedia() {
//        if (player == null) {
//            if (!player!!.isPlaying) {
//                player.stopPlayback()
//                isPlayerStarted = false
//            }
//            mServiceCallBacks?.pause()
//            buildNotification(false)
//            stopSelf()
//        }
//    }


//
//    private fun buildNotification(playStatus: Boolean) {
//        val notificationAction: Int
//        var play_pauseAction: PendingIntent? = null
//        var title = ""
//
//        if (playStatus) {
//            notificationAction = R.drawable.pause_button
//            title = "pause"
//            //create the pause action
//            play_pauseAction = playbackAction(1)
//        } else {
//            notificationAction = R.drawable.play_button
//            title = "play"
//            //create the play action
//            play_pauseAction = playbackAction(0)
//        }
//        val notificationCloseAction: Int = R.drawable.stop
//        val notificationCloseActionIntent: PendingIntent = playbackAction(4)!!
////        val largeIcon = BitmapFactory.decodeResource(
////            resources,
////            SyncStateContract.Constants.NOTIFICATION_BANNER_IMAGE
////        )
//
//        // Create a new Notification
//        val NOTIFICATION_CHANNEL_ID = "com.example.music"
//        val channelName = "Song"
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notificationChannel = NotificationChannel(
//                NOTIFICATION_CHANNEL_ID,
//                channelName,
//                NotificationManager.IMPORTANCE_NONE
//            )
//            notificationChannel.lightColor = Color.BLUE
//            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
//            val manager = (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
//            manager.createNotificationChannel(notificationChannel)
//        }
//        val resultIntent = Intent(this, MainActivity::class.java)
//        val openAppActivity = PendingIntent.getActivity(
//            this,
//            0,
//            resultIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )
//        val notificationBuilder: NotificationCompat.Builder = if (showName != "") {
//            //no inspection ResultOfMethodCallIgnored
////            val show = showName.replace(getString(R.string.label_now_playing_on_radio), "")
//            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//                .setShowWhen(false) // Set the Notification style
//                .setStyle(
//                    androidx.media.app.NotificationCompat.MediaStyle() // Attach our MediaSession token
//                        .setMediaSession(mediaSession!!.sessionToken) // Show our playback controls in the compact notification view.
//                        .setShowActionsInCompactView(0, 1)
//                ) // Set the Notification color
//                .setColor(Color.parseColor("#ee8c04")) // Set the large and small icons
////                .setLargeIcon(largeIcon)
//                .setSmallIcon(R.drawable.musicss) // Set Notification content information
////                .setContentText(show)
//                .setContentTitle(getString(R.string.label_now_playing)) // Add playback actions
//                .addAction(notificationAction, title, play_pauseAction)
//                .addAction(notificationCloseAction, "", notificationCloseActionIntent)
//        } else {
//            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
//                .setShowWhen(false) // Set the Notification style
//                .setStyle(
//                    androidx.media.app.NotificationCompat.MediaStyle() // Attach our MediaSession token
//                        .setMediaSession(mediaSession!!.sessionToken) // Show our playback controls in the compact notification view.
//                        .setShowActionsInCompactView(0, 1)
//                ) // Set the Notification color
//                .setColor(Color.parseColor("#ee8c04")) // Set the large and small icons
////                .setLargeIcon(largeIcon)
//                .setSmallIcon(R.drawable.musicss) // Set Notification content information
//                // Add playback actions
//                .addAction(notificationAction, title, play_pauseAction)
//                .addAction(notificationCloseAction, "", notificationCloseActionIntent)
//        }
//        notificationBuilder.setContentIntent(openAppActivity)
//        val notification = notificationBuilder.build()
//        startForeground(101, notification)
//    }

//    private fun playbackAction(actionNumber: Int): PendingIntent? {
//        val playbackAction = Intent(this, MediaplayerService::class.java)
//        when (actionNumber) {
//            0 -> {
//                // Play
//                playbackAction.action = ACTION_PLAY
//                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
//            }
//            1 -> {
//                // Pause
//                playbackAction.action = ACTION_PAUSE
//                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
//            }
//            2 -> {
//                // Next track
//                playbackAction.action = ACTION_NEXT
//                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
//            }
//            3 -> {
//                // Previous track
//                playbackAction.action = ACTION_PREVIOUS
//                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
//            }
//            4 -> {
//                // stop track
//                playbackAction.action = ACTION_STOP
//                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
//            }
//            else -> {
//            }
//        }
//        return null
//    }

    fun seekTo(): Int {
//        mediaPlayer = MediaPlayer()
        return mediaPlayer.currentPosition
    }
    fun duration(): Int{
//        mediaPlayer = MediaPlayer()
        return mediaPlayer.duration
    }

    fun start() {
//        mediaPlayer = MediaPlayer()
        mediaPlayer.start()
    }

    fun isPlaying(): Boolean {
        return true
    }

    fun pauseAudio() {
//        mediaPlayer = MediaPlayer()
       mediaPlayer.pause()
    }

    fun setVolume(fl: Float, fl1: Float) {
//        mediaPlayer = MediaPlayer()
        mediaPlayer.setVolume(fl,fl1)
    }

    fun seekT(i: Int){
//        mediaPlayer = MediaPlayer()
       mediaPlayer.seekTo(i)
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        TODO("Not yet implemented")
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        TODO("Not yet implemented")
    }

    fun seek() {
//        mediaPlayer = MediaPlayer()
        mediaPlayer.seekTo(mediaPlayer.currentPosition)
    }
}