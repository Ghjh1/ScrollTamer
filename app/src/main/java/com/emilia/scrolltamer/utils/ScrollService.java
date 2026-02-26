package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return "DIAGNOSTIC MODE: RAW 10PX";
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        float sign = Math.signum(delta);
        // Чистый, прямой шаг на 10 пикселей. 
        // Это наше "нулевое" измерение.
        float testStep = sign * 10.0f; 

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + testStep);

        // Длительность 50мс - это стандартный "уверенный" жест пальцем.
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 50);
        
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
