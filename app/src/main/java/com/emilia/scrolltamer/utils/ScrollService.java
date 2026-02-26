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

    public static String getDebugData() {
        return String.format("LAB: Dist=%.0fpx | Time=%dms", testDist, testTime);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        // ЛОГИКА ТЕСТЕРА:
        if (delta < 0) { // Крутим вверх - меняем расстояние
            testDist += 1.0f;
            if (testDist > 100) testDist = 10;
        } else { // Крутим вниз - меняем время (делаем жест быстрее)
            testTime -= 10;
            if (testTime < 10) testTime = 200;
        }
        
        // Автоматический тест при каждом движении, чтобы сразу видеть результат
        runTest(x, y);
    }

    private static void runTest(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + testDist);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, testTime);
        instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
