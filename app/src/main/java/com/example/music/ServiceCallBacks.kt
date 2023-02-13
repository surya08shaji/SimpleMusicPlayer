package com.example.music

interface ServiceCallBacks {
    fun pause()

    fun play()

    fun start()

    fun close()

    fun netWorkAvailable()

    fun noNetwork()
}