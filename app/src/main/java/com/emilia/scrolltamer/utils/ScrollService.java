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
        return "DIAGNOSTIC MODE: RAW 70PX";
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        float sign = Math.signum(delta);
        // Проверяем твою догадку про 50-70 пикселей
        float testStep = sign * 70.0f; 

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + testStep);

        // 30мс - быстрый и уверенный жест
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 30);
        
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
