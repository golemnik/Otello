package com.golem.myapplication.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import com.golem.myapplication.databinding.FragmentSettingsBinding
import androidx.core.content.edit
import com.golem.myapplication.audio.GameAudioManager
import com.golem.myapplication.audio.withSound

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val PREFS_NAME = "game_settings"
        private const val KEY_MUSIC_VOLUME = "music_volume"
        private const val KEY_INTERFACE_VOLUME = "interface_volume"
        private const val KEY_INTERFACE_SOUND = "interface_sound"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_DARK_THEME = "dark_theme"
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var audioManager: GameAudioManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        audioManager = GameAudioManager.getInstance(requireContext())

        loadSettings()
        setupListeners()

        // Кнопка возврата со звуком
        binding.buttonBack.withSound(requireContext()) {
            findNavController().navigateUp()
        }
    }

    private fun loadSettings() {
        // Загружаем сохраненные настройки
        binding.seekbarMusicVolume.progress = sharedPreferences.getInt(KEY_MUSIC_VOLUME, 50)
        binding.seekbarInterfaceVolume.progress = sharedPreferences.getInt(KEY_INTERFACE_VOLUME, 50)

        val isMusicEnabled = sharedPreferences.getBoolean(KEY_MUSIC_ENABLED, true)
        val isInterfaceSoundEnabled = sharedPreferences.getBoolean(KEY_INTERFACE_SOUND, true)

        binding.switchMusic.isChecked = isMusicEnabled
        binding.switchInterfaceSound.isChecked = isInterfaceSoundEnabled
        binding.switchTheme.isChecked = sharedPreferences.getBoolean(KEY_DARK_THEME, false)

        // Устанавливаем доступность слайдеров в зависимости от состояния переключателей
        setMusicVolumeEnabled(isMusicEnabled)
        setInterfaceVolumeEnabled(isInterfaceSoundEnabled)

        // Применяем тему если нужно
        updateTheme(binding.switchTheme.isChecked)
    }

    private fun setupListeners() {
        // Слушатель изменения громкости музыки
        binding.seekbarMusicVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    saveIntSetting(KEY_MUSIC_VOLUME, progress)
                    // Обновляем громкость музыки
                    audioManager.updateMusicVolume()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Проигрываем звук интерфейса при отпускании слайдера
                audioManager.playInterfaceSound(GameAudioManager.SoundType.BUTTON_CLICK)
            }
        })

        // Слушатель изменения громкости интерфейса
        binding.seekbarInterfaceVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    saveIntSetting(KEY_INTERFACE_VOLUME, progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Проигрываем звук с новой громкостью для демонстрации
                audioManager.playInterfaceSound(GameAudioManager.SoundType.BUTTON_CLICK)
            }
        })

        // Слушатель переключателя звука интерфейса
        binding.switchInterfaceSound.setOnCheckedChangeListener { _, isChecked ->
            // Воспроизводим звук переключения если включаем звуки
            if (isChecked) {
                audioManager.playInterfaceSound(GameAudioManager.SoundType.BUTTON_CLICK)
            }

            saveBooleanSetting(KEY_INTERFACE_SOUND, isChecked)
            setInterfaceVolumeEnabled(isChecked)
        }

        // Слушатель переключателя музыки
        binding.switchMusic.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanSetting(KEY_MUSIC_ENABLED, isChecked)
            setMusicVolumeEnabled(isChecked)

            // Воспроизводим звук переключения
            audioManager.playInterfaceSound(GameAudioManager.SoundType.BUTTON_CLICK)

            // Обновляем состояние музыки
            audioManager.updateMusicState()
        }

        // Слушатель переключателя темы
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            saveBooleanSetting(KEY_DARK_THEME, isChecked)
            audioManager.playInterfaceSound(GameAudioManager.SoundType.BUTTON_CLICK)
            updateTheme(isChecked)
        }
    }

    // Метод для включения/отключения слайдера громкости музыки
    private fun setMusicVolumeEnabled(enabled: Boolean) {
        binding.seekbarMusicVolume.isEnabled = enabled
        binding.textviewMusicVolume.alpha = if (enabled) 1.0f else 0.5f
        binding.seekbarMusicVolume.alpha = if (enabled) 1.0f else 0.5f
    }

    // Метод для включения/отключения слайдера громкости интерфейса
    private fun setInterfaceVolumeEnabled(enabled: Boolean) {
        binding.seekbarInterfaceVolume.isEnabled = enabled
        binding.textviewInterfaceVolume.alpha = if (enabled) 1.0f else 0.5f
        binding.seekbarInterfaceVolume.alpha = if (enabled) 1.0f else 0.5f
    }

    private fun saveIntSetting(key: String, value: Int) {
        sharedPreferences.edit() { putInt(key, value) }
    }

    private fun saveBooleanSetting(key: String, value: Boolean) {
        sharedPreferences.edit() { putBoolean(key, value) }
    }

    private fun updateTheme(isDarkTheme: Boolean) {
        // Сохраняем выбор темы
        saveBooleanSetting(KEY_DARK_THEME, isDarkTheme)

        // Устанавливаем режим темы
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // Звук кнопки будет потерян при пересоздании, но вместо попыток фиксить это,
        // просто предупреждаем пользователя
//        Toast.makeText(
//            requireContext(),
//            "Тема изменена. Для полного применения перезапустите приложение.",
//            Toast.LENGTH_SHORT
//        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

