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
        // Если кликнули по кнопке с текстом "НАЖМИ МЕНЯ"
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            if (event.getText().toString().contains("НАЖМИ МЕНЯ")) {
                Log.d(TAG, "ПОЛИГОН: Кнопка нажата. Пускаю скролл через 300мс...");
                new android.os.Handler().postDelayed(this::testScroll, 300);
            }
        }
    }

    private void testScroll() {
        Path p = new Path();
        p.moveTo(500, 1000);
        p.lineTo(500, 200);
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 1000);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
    }

    @Override
    public void onInterrupt() {}
}
