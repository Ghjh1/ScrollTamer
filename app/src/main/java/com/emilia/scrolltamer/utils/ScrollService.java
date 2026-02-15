package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static final String TAG = "ScrollTamer";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Получаем имя пакета (приложения)
        CharSequence packageName = event.getPackageName();
        // Получаем тип события в читаемом виде
        String eventType = AccessibilityEvent.eventTypeToString(event.getEventType());
        // Пробуем достать текст (если он есть)
        String contentText = "";
        if (event.getText() != null && !event.getText().isEmpty()) {
            contentText = event.getText().toString();
        }

        // Выводим "карту" сигнала в лог
        Log.d(TAG, String.format("[%s] Приложение: %s | Текст: %s",
              eventType, packageName, contentText));
    }

    @Override
    public void onInterrupt() {}
}
