package com.indosam.sportsarena.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.indosam.sportsarena.R

object SoundUtils {

    private var soundPool: SoundPool? = null
    private var soundId: Int = -1

    fun initialize(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        // Load the sound
        soundId = soundPool?.load(context, R.raw.success_tone, 1) ?: -1
    }

    fun playSound() {
        if (soundId != -1) {
            soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
    }


}