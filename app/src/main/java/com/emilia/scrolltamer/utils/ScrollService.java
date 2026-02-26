package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.view.MotionEvent;
import android.view.InputDevice;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float testDist = 14.0f; 
    private static int testTime = 100;

    @Override
    protected void onServiceConnected() {
        instance = this;
        AccessibilityServiceInfo info = getServiceInfo();
        // Убеждаемся, что мы ловим события из всех приложений, включая свое
        info.flags |= AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS;
        info.flags |= AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        setServiceInfo(info);
    }

    public static void setParams(float d, int t) {
        if (d > 0) testDist = d;
        if (t > 0) testTime = t;
    }

    public static String getDebugData() {
        return String.format("ТЕСТ: %.1f px | %d ms", testDist, testTime);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        
        Path path = new Path();
        path.moveTo(x, y);
        float direction = Math.signum(delta);
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
