package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float testDist = 14.0f; 
    private static int testTime = 100;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static void setParams(float d, int t) {
        if (d >= 0) testDist = d;
        if (t > 0) testTime = t;
    }

    public static String getDebugData() {
        return String.format("D: %.1f px | T: %d ms", testDist, testTime);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        float direction = Math.signum(delta);
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (testDist * direction));

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(
            path, 0, Math.max(10, testTime));
        
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
