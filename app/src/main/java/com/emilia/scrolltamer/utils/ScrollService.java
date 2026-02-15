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
        // Ловим клик пользователя как сигнал к действию
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            Log.d(TAG, "Сигнал получен! Пробую пробить защиту жестом...");
            testClick();
        }
    }

    private void testClick() {
        Path path = new Path();
        // Нажмем в центр экрана (примерно 360, 800 для твоего Redmi)
        path.moveTo(360, 800);

        // Короткое нажатие (50 мс)
        GestureDescription.StrokeDescription click = new GestureDescription.StrokeDescription(path, 0, 50);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(click);

        dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "КАНАЛ СВЯЗИ ПОДТВЕРЖДЕН: Жест принят системой!");
            }
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(TAG, "БЛОКИРОВКА: Система отвергла жест. Проверь настройки безопасности MIUI.");
            }
        }, null);
    }

    @Override
    public void onInterrupt() {}
}
