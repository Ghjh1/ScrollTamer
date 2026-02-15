package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static final String TAG = "ScrollTamer";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "!!! МАЯК ПЕРЕЗАПУЩЕН: ЖДУ ЛЮБОЙ ШОРОХ !!!");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Логируем ТИП события цифрой и названием
        // Это покажет нам, ЧТО именно видит сервис
        int eventType = event.getEventType();
        Log.d(TAG, "Движение в системе! Тип: " + eventType);
    }

    @Override
    public void onInterrupt() {}
}
