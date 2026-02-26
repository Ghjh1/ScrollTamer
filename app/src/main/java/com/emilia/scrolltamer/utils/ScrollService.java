package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float testLimit = 14.0f; 

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("CALIBRATION MODE | CURRENT LIMIT: %.1f", testLimit);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        // Если крутим ВВЕРХ (delta < 0) - увеличиваем порог
        if (delta < 0) {
            testLimit += 1.0f;
            if (testLimit > 150) testLimit = 10;
        }

        // Если крутим ВНИЗ (delta > 0) - выполняем тестовый удар
        if (delta > 0) {
            Path path = new Path();
            path.moveTo(x, y);
            path.lineTo(x, y + testLimit);

            GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 100);
            
            try {
                instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
            } catch (Exception e) { }
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
