package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float testLimit = 13.0f;
    private static int testTime = 40;
    private static int testDelay = 0;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static void setParams(float d, int t, int delay) {
        testLimit = d;
        if (t > 0) testTime = t;
        if (delay >= 0) testDelay = delay;
    }

    public static String getDebugData() {
        return String.format("D: %.1f | T: %d | Delay: %d", testLimit, testTime, testDelay);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + testLimit);

        // Используем testDelay перед началом движения
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, testDelay, testTime);
        
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
