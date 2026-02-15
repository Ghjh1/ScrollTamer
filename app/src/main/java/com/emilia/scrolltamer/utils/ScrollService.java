package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static final String TAG = "ScrollTamer";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Оставляем для отладки кликов
    }

    // Это магическое место, где мы ловим кнопки и колесики
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();

        Log.d(TAG, "Кнопка нажата! Код: " + keyCode);

        // Если это прокрутка (некоторые мыши эмулируют кнопки громкости или стрелки)
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Log.d(TAG, "Поймал сигнал! Вместо громкости сейчас будет ШЕЛК...");
            // Тут мы вызовем наш плавный скролл
            return true; // "Съедаем" событие, чтобы громкость не менялась
        }

        return super.onKeyEvent(event);
    }

    @Override
    public void onInterrupt() {}
}
