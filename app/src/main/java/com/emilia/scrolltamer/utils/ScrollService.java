package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            Log.d("ScrollTamer", "Пробую спровоцировать родную инерцию...");
            flickScroll();
        }
    }

    private void flickScroll() {
        Path p = new Path();
        p.moveTo(500, 1000);
        p.lineTo(500, 700); // Короткий путь

        // ОЧЕНЬ быстро (80мс). Система должна воспринять это как резкий свайп
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(p, 0, 80);
        dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
    }

    @Override
    public void onInterrupt() {}
}
