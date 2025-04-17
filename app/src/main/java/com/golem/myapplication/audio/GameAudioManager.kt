package com.golem.myapplication.audio

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import com.golem.myapplication.R

class GameAudioManager private constructor(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "game_settings"
        private const val KEY_MUSIC_VOLUME = "music_volume"
        private const val KEY_INTERFACE_VOLUME = "interface_volume"
        private const val KEY_INTERFACE_SOUND = "interface_sound"
        private const val KEY_MUSIC_ENABLED = "music_enabled"

        @Volatile
        private var instance: GameAudioManager? = null

        fun getInstance(context: Context): GameAudioManager {
            return instance ?: synchronized(this) {
                instance ?: GameAudioManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // Настройки
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Музыкальный плеер
    private var mediaPlayer: MediaPlayer? = null

    // SoundPool для звуков интерфейса
    private val soundPool: SoundPool =
        SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build())
            .build()

    // Кеш звуков
    private val soundCache = mutableMapOf<SoundType, Int>()

    // Типы звуков
    enum class SoundType {
        BUTTON_CLICK, BACK_BUTTON
    }

    init {
        // Предзагрузка звуков
        soundCache[SoundType.BUTTON_CLICK] = soundPool.load(context, R.raw.button_click, 1)
        soundCache[SoundType.BACK_BUTTON] = soundPool.load(context, R.raw.back_button, 1)
    }

    // Воспроизведение звука интерфейса
    fun playInterfaceSound(soundType: SoundType) {
        if (!isSoundEnabled()) return
        soundCache[soundType]?.let { soundId ->
            val volume = getInterfaceVolume()
            soundPool.play(soundId, volume, volume, 1, 0, 1.0f)
        }
    }

    // Фоновая музыка
    fun startBackgroundMusic() {
        if (!isMusicEnabled()) return

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.background_music)
            mediaPlayer?.isLooping = true
            updateMusicVolume()
            mediaPlayer?.start()
        } else if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    fun pauseBackgroundMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    fun stopBackgroundMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }

    fun updateMusicVolume() {
        if (!isMusicEnabled()) return

        val volume = getMusicVolume()
        mediaPlayer?.setVolume(volume, volume)
    }

    fun updateMusicState() {
        if (isMusicEnabled()) {
            startBackgroundMusic()
        } else {
            pauseBackgroundMusic()
        }
    }

    fun reinitialize() {
        // Перезагружаем звуки интерфейса
        soundCache.clear()
        soundCache[SoundType.BUTTON_CLICK] = soundPool.load(context, R.raw.button_click, 1)
        soundCache[SoundType.BACK_BUTTON] = soundPool.load(context, R.raw.back_button, 1)

        // Обновляем состояние музыки
        updateMusicState()
    }


    private fun getInterfaceVolume(): Float {
        val volumePercent = prefs.getInt(KEY_INTERFACE_VOLUME, 50)
        return volumePercent / 100f
    }

    private fun getMusicVolume(): Float {
        val volumePercent = prefs.getInt(KEY_MUSIC_VOLUME, 50)
        return volumePercent / 100f
    }

    private fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_INTERFACE_SOUND, true)
    }

    private fun isMusicEnabled(): Boolean {
        return prefs.getBoolean(KEY_MUSIC_ENABLED, true)
    }

    fun release() {
        stopBackgroundMusic()
        soundPool.release()
    }
}
