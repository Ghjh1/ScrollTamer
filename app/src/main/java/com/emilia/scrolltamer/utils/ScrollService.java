package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class ScrollService extends AccessibilityService {
    private static final String TAG = "ScrollTamer";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "СЕРВИС ПОДКЛЮЧЕН И ГОТОВ К РАБОТЕ!");
        Toast.makeText(this, "Связь установлена!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Теперь мы будем видеть в Termux КАЖДОЕ событие
        String eventName = AccessibilityEvent.eventTypeToString(event.getEventType());
        Log.d(TAG, "Событие системы: " + eventName);

        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            Log.d(TAG, "Обнаружен клик! Пытаюсь скроллить...");
        }
    }

    @Override
    public void onInterrupt() {}
}
