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
        // Мы будем скроллить ТОЛЬКО когда ты кликаешь по экрану
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            Log.d(TAG, "Клик пойман! Запускаю шелковый скролл...");
            performSilkScroll();
        }
    }

    private void performSilkScroll() {
        // Координаты для Redmi Note 9C (центр экрана)
        Path scrollPath = new Path();
        scrollPath.moveTo(500, 1200); // Начало (снизу)
        scrollPath.lineTo(500, 400);  // Конец (вверху)

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(scrollPath, 0, 500);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(stroke);

        dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "Скролл успешно выполнен!");
            }
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(TAG, "Скролл отменен системой! (Проверь настройки безопасности)");
            }
        }, null);
    }

    @Override
    public void onInterrupt() {}
}
