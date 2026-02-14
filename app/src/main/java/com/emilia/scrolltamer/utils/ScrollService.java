package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class ScrollService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Мы просто логируем тип события в Toast, чтобы понять, что телефон вообще нас "слышит"
        // Это может быть очень назойливо, но зато мы поймем, на что он реагирует
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
             // Делаем Silk Touch на ЛЮБОЕ изменение контента (например, мигание курсора)
             performSilkScroll();
        }
    }

    private void performSilkScroll() {
        Path swipePath = new Path();
        swipePath.moveTo(500, 1000);
        swipePath.lineTo(500, 400);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(swipePath, 0, 500);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(stroke);

        dispatchGesture(builder.build(), null, null);
    }

    @Override
    public void onInterrupt() {}
}
