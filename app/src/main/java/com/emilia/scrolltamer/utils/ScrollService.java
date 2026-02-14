package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class ScrollService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Реагируем на любой "чих" системы
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ||
            event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {

            // Если код работает, ты увидишь это сообщение
            Toast.makeText(this, "Сервис видит экран!", Toast.LENGTH_SHORT).show();
            performSimpleScroll();
        }
    }

    private void performSimpleScroll() {
        Path path = new Path();
        path.moveTo(500, 1000);
        path.lineTo(500, 500);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 500);
        dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
    }

    @Override
    public void onInterrupt() {}
}
