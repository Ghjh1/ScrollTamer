package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static final String TAG = "ScrollTamer";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "!!! МАЯК ПЕРЕЗАПУЩЕН: ВИЖУ ВСЁ !!!");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Получаем текстовое описание события
        String type = AccessibilityEvent.eventTypeToString(event.getEventType());
        Log.d(TAG, "СОБЫТИЕ: " + type);
    }

    @Override
    public void onInterrupt() {}
}
