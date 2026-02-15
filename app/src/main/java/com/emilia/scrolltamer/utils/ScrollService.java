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
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            String text = event.getText().toString();
            if (text.contains("МЕДЛЕННЫЙ")) {
                doScroll(2000, 400); // 2 секунды, плавно
            } else if (text.contains("ШЕЛК")) {
                doScroll(1000, 600); // 1 секунда, стандарт
            } else if (text.contains("ИМПУЛЬС")) {
                doScroll(300, 800);  // 0.3 секунды, быстро
            }
        }
    }

    private void doScroll(int duration, int distance) {
        Path p = new Path();
        int startY = 1200;
        p.moveTo(500, startY);
        p.lineTo(500, startY - distance);

        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, duration);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
        Log.d(TAG, "Скролл: " + duration + "мс, дистанция: " + distance);
    }

    @Override
    public void onInterrupt() {}
}
