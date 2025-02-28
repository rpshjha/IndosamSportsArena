package com.indosam.sportsarena.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.indosam.sportsarena.R

object SoundUtils {

    private var soundPool: SoundPool? = null
    private var successSoundId: Int = -1
    private var errorSoundId: Int = -1

    fun initialize(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(audioAttributes)
            .build()

        successSoundId = soundPool?.load(context, R.raw.success_tone, 1) ?: -1
        errorSoundId = soundPool?.load(context, R.raw.error_tone, 1) ?: -1
    }

    fun playSuccess() {
        if (soundPool != null && successSoundId != -1) {
            soundPool?.play(successSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun playError() {
        if (soundPool != null && errorSoundId != -1) {
            soundPool?.play(errorSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        successSoundId = -1
        errorSoundId = -1
    }
}
