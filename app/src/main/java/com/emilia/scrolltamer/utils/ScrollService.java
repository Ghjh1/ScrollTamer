package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Когда окно меняется (например, открыли браузер), пробуем сделать Silk Touch
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            performSilkScroll();
        }
    }

    private void performSilkScroll() {
        Path swipePath = new Path();
        // Начинаем снизу экрана (центр) и плавно ведем вверх
        swipePath.moveTo(500, 1500);
        swipePath.lineTo(500, 800);

        // 1000 мс (1 секунда) — это и даст ту самую "шелковую" плавность
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(swipePath, 0, 1000);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(stroke);

        dispatchGesture(builder.build(), null, null);
    }

    @Override
    public void onInterrupt() {}
}
