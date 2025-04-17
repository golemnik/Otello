package com.golem.myapplication.audio

import android.content.Context
import android.view.View
import android.widget.Button

fun Button.withSound(context: Context, action: (View) -> Unit) {
    // Получаем audioManager каждый раз заново при нажатии,
    // что гарантирует работу даже после пересоздания активности
    this.setOnClickListener { view ->
        GameAudioManager.getInstance(context)
            .playInterfaceSound(GameAudioManager.SoundType.BUTTON_CLICK)
        action(view)
    }
}
