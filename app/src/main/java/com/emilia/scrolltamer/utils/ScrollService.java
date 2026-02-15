package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static final String TAG = "ScrollTamer";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Мы фильтруем поток: реагируем ТОЛЬКО на клик
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            Log.d(TAG, "Клик пойман! Начинаю магию...");
            performSilkScroll();
        }
    }

    private void performSilkScroll() {
        Path path = new Path();
        // Настройки под твой экран: ведем снизу вверх по центру
        path.moveTo(500, 1300);
        path.lineTo(500, 300);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 500);
        dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "Магия сработала: Скролл завершен!");
            }
        }, null);
    }

    @Override
    public void onInterrupt() {}
}
