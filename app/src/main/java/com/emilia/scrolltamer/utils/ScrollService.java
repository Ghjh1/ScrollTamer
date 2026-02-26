package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float testLimit = 14.0f;
    private static int testTime = 100;

    @Override
    protected void onServiceConnected() { instance = this; }

    // Метод для установки точных значений из диалога
    public static void setParams(float d, int t) {
        testLimit = d;
        if (t > 0) testTime = t;
    }

    public static String getDebugData() {
        return String.format("FIXED MODE | D: %.1f | T: %d ms", testLimit, testTime);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        // Любое движение колеса теперь просто вызывает жест
        // Без изменения testLimit
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + testLimit);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, testTime);
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
