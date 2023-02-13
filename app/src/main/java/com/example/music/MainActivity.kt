package com.example.music

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.music.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    private var playIntent: Intent? = null
    private var musicBound = false
    private var stop = false
    private var playbackPaused: kotlin.Boolean = false
    private var firstTimePlay = false
    lateinit var mediaplayerService: MediaplayerService
//    var mediaplayerService = MediaplayerService()


    //    private val controller: MusicController? = null
    //    var musicService: MusicService? = null
    //    var player: MediaPlayer? = null
//    var currentProgram: String? = null

//    lateinit var serviceConnection: ServiceConnection
//
//    private var serviceCallBacks: ServiceCallBacks? = null
//
    private lateinit var runnable:Runnable
    private var handler: Handler = Handler()

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        mediaplayerService = MediaplayerService()

        binding.playBtn.setOnClickListener {
            if (!isMyMusicServiceRunning(MediaplayerService::class.java)) {
                //!isMyMusicServiceRunning(MediaplayerService::class.java)
                playIntent = Intent(applicationContext, MediaplayerService::class.java)
                bindService(playIntent, serviceConnection, BIND_AUTO_CREATE)
                startService(playIntent)
                Log.d(TAG, "onCreate: service created in oncreate ")

//                mediaplayerService!!.seekTo()
//                mediaplayerService.seek()
//                mediaplayerService.start()

                stop = false

            } else {
                playIntent = Intent(applicationContext, MediaplayerService::class.java)
                bindService(playIntent, serviceConnection, BIND_AUTO_CREATE)
                startService(playIntent)

//                mediaplayerService.playAudio()
            }

//            initializeSeekBar()
            binding.playBtn.visibility = View.GONE
            binding.pauseBtn.visibility = View.VISIBLE
        }
        binding.pauseBtn.setOnClickListener {
            if (mediaplayerService.isPlaying()) {
                mediaplayerService.pauseAudio()

                stop = true

                binding.pauseBtn.visibility = View.GONE
                binding.playBtn.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Audio has not played", Toast.LENGTH_SHORT).show()
            }
        }

        binding.positionBar.setOnSeekBarChangeListener(
            @SuppressLint("AppCompatCustomView")
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaplayerService.seekT(progress * 1000)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            }
        )
        binding.volumeBar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mediaplayerService.setVolume(0.5f,0.5f)
                        val volumeNum = progress / 100.0f
                        mediaplayerService.setVolume(volumeNum, volumeNum)

                        //mp.setVolume(0.5f,0.5f)
                        //                       val volumeNum = progress / 100.0f
                        //                       mp.setVolume(volumeNum,volumeNum)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    binding.soundMute.setOnClickListener {
                        seekBar!!.progress = 0
                    }
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    binding.soundFull.setOnClickListener {
                        seekBar!!.progress = 100
                    }
                }

            }
        )

//        val startIntent = Intent(this@MainActivity,MediaplayerService::class.java)
//        startIntent.action = mediaplayerService.ACTION_STARTFOREGROUND
//        startService(startIntent)

    }

    private fun initializeSeekBar() {
        playIntent = Intent(applicationContext, MediaplayerService::class.java)
        bindService(playIntent, serviceConnection, BIND_AUTO_CREATE)
        startService(playIntent)

        runnable = Runnable {

            binding.positionBar.progress = mediaplayerService.seekTo() / 1000

            val elapsedTime = createTimeLabel(mediaplayerService.seekTo() )
            binding.elapsedTimeLabel.text = elapsedTime

            val remainingTime = createTimeLabel(mediaplayerService.duration() - mediaplayerService.seekTo())
            binding.remainingTimeLabel.text = remainingTime

            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }

    private fun createTimeLabel(time: Int): String{
        var timeLabel: String
        val min = time / 1000 / 60
        val sec = time / 1000  % 60

        timeLabel = "$min:"
        if (sec < 10) timeLabel += "0"
        timeLabel += sec

        return timeLabel
    }

    private fun isMyMusicServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }



    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: MediaplayerService.LocalBinder = service as MediaplayerService.LocalBinder
            mediaplayerService = binder.getService()
            musicBound = true
            Log.d(TAG, "onServiceConnected: invoked")
//            mediaplayerService.playAudio()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            musicBound = false
        }
    }


    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart invoked")
        if (!isMyMusicServiceRunning(MediaplayerService::class.java)) {
            playIntent = Intent(applicationContext, MediaplayerService::class.java)
            bindService(playIntent, serviceConnection, BIND_AUTO_CREATE)
            startService(playIntent)
            Log.d(TAG, "onStart MainActivity: service started and binded again ")


//            val startIntent = Intent(this@MainActivity,MediaplayerService::class.java)
//            startIntent.action = mediaplayerService.ACTION_STARTFOREGROUND
//            startService(startIntent)

        }
        if (!musicBound && isMyMusicServiceRunning(MediaplayerService::class.java)) {
            playIntent = Intent(applicationContext, MediaplayerService::class.java)
            Log.d(TAG, "onStart: service binded again")
            bindService(playIntent, serviceConnection, BIND_AUTO_CREATE)

//            val startIntent = Intent(this@MainActivity,MediaplayerService::class.java)
//            startIntent.action = mediaplayerService.ACTION_STARTFOREGROUND
//            startService(startIntent)
        }
    }
}



////        binding.playBtn.visibility = View.VISIBLE
//
//       serviceConnection = object : ServiceConnection {
//            override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
//                val binder: MediaplayerService.LocalBinder =
//                    iBinder as MediaplayerService.LocalBinder
//                mediaplayerService = binder.getService()
////                mediaplayerService!!.setCallBacks()
//                mediaplayerService!!.setCallBacks(serviceCallBacks)
//            }
//
//            override fun onServiceDisconnected(componentName: ComponentName) {}
//        }
//
//        binding.positionBar.setOnSeekBarChangeListener(
//            object : SeekBar.OnSeekBarChangeListener {
//                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//                    if (fromUser) {
//                        seekTo(progress * 1000)
////                        player!!.seekTo(progress * 1000)
//                    }
//                }
//                override fun onStartTrackingTouch(p0: SeekBar?) {
//                }
//                override fun onStopTrackingTouch(p0: SeekBar?) {
//                }
//            }
//        )
//
//        binding.volumeBar.setOnSeekBarChangeListener(
//            object :SeekBar.OnSeekBarChangeListener{
//                override fun onProgressChanged(
//                    seekBar: SeekBar?,
//                    progress: Int,
//                    fromUser: Boolean
//                ) {
//                    if (fromUser){
//                        player!!.setVolume(0.5f,0.5f)
//                        val volumeNum = progress / 100.0f
//                        player!!.setVolume(volumeNum,volumeNum)
//
//                    }
//                }
//
//                override fun onStartTrackingTouch(seekBar: SeekBar?) {
//                    binding.soundMute.setOnClickListener {
//                        seekBar!!.progress = 0
//                    }
//                }
//
//                override fun onStopTrackingTouch(seekBar: SeekBar?) {
//                    binding.soundFull.setOnClickListener {
//                        seekBar!!.progress = 100
//                    }
//                }
//            }
//        )
//
//        if (binding.playBtn.visibility == View.VISIBLE) {
//
//            initializeSeekBar()
//
//            binding.playBtn.visibility = View.GONE
//            binding.pauseBtn.visibility = View.VISIBLE
//            mediaplayerService!!.playMedia()
//            Toast.makeText(applicationContext,"Now Playing",Toast.LENGTH_SHORT).show()
//
//        } else {
//
//            binding.pauseBtn.visibility = View.GONE
//            binding.playBtn.visibility = View.VISIBLE
//            mediaplayerService!!.pauseMedia()
//            Toast.makeText(applicationContext,"Player Paused",Toast.LENGTH_SHORT).show()
//        }
//    }
//
//
//    override fun onPause() {
//        super.onPause()
//    }
//
//    fun playAudio() {
//        val intent = Intent(this, MediaplayerService::class.java)
//        startService(intent)
//        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
//    }
//
//
////    fun onClick(view: View) {
////        if (binding.playBtn.visibility == View.VISIBLE) {
////
////            initializeSeekBar()
////
////            binding.playBtn.visibility = View.GONE
////            binding.pauseBtn.visibility = View.VISIBLE
////            mediaplayerService!!.playMedia()
////            Toast.makeText(applicationContext,"Now Playing",Toast.LENGTH_SHORT).show()
////
////        } else if (binding.pauseBtn.visibility == View.VISIBLE) {
////
////            binding.pauseBtn.visibility = View.GONE
////            binding.playBtn.visibility = View.VISIBLE
////            mediaplayerService!!.pauseMedia()
////            Toast.makeText(applicationContext,"Player Paused",Toast.LENGTH_SHORT).show()
////        }
////    }
//
//    fun getDuration(): Int {
//        return if (mediaplayerService!!.isPng()) mediaplayerService!!.getDur() else 0
//    }
//
//    fun getCurrentPosition(): Int {
//        return if (mediaplayerService!!.isPng()) mediaplayerService!!.getPosn() else 0
//    }
//
//    fun seekTo(pos: Int) {
//        mediaplayerService!!.seek(pos)
//    }
//
//    fun isPlaying(): Boolean {
//        return mediaplayerService!!.isPng()
//    }
//
//    private fun initializeSeekBar() {
//
//
//        runnable = Runnable {
//
//            val duration = getDuration()
//            val currentPosition = getCurrentPosition()
//
//            binding.positionBar.progress = currentPosition / 1000
//
////            binding.positionBar.progress = player!!.currentPosition / 1000
//
//
//            val elapsedTime = createTimeLabel(currentPosition)
////            val elapsedTime = createTimeLabel(player!!.currentPosition)
//            binding.elapsedTimeLabel.text = elapsedTime
//
//            val remainingTime = createTimeLabel(duration - currentPosition)
////            val remainingTime = createTimeLabel(player!!.duration - player!!.currentPosition)
//            binding.remainingTimeLabel.text = remainingTime
//
//            handler.postDelayed(runnable, 1000)
//        }
//        handler.postDelayed(runnable, 1000)
//    }
//
//    private fun createTimeLabel(time: Int): String {
//        var timeLabel: String
//        val min = time / 1000 / 60
//        val sec = time / 1000  % 60
//
//        timeLabel = "$min:"
//        if (sec < 10) timeLabel += "0"
//        timeLabel += sec
//
//        return timeLabel
//    }
//
//
//
//    fun pause() {
//        binding.pauseBtn.visibility = View.GONE
//        binding.playBtn.visibility = View.VISIBLE
//        Toast.makeText(applicationContext,"Player Paused",Toast.LENGTH_SHORT).show()
//    }
//
//    fun play() {
//        initializeSeekBar()
//
//        binding.pauseBtn.visibility = View.VISIBLE
//        binding.playBtn.visibility = View.GONE
//        Toast.makeText(applicationContext,"Now Playing",Toast.LENGTH_SHORT).show()