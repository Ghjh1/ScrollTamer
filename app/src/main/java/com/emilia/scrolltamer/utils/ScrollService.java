package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.view.KeyEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float testDist = 14.0f; 
    private static int testTime = 100;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("LAB v134: Step=%.0fpx | Time=%dms", testDist, testTime);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            // Стрелки ТОЛЬКО меняют параметры
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) { testDist += 1; return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) { testDist -= 1; return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) { testTime += 10; return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) { testTime -= 10; return true; }
            if (keyCode == KeyEvent.KEYCODE_SPACE) { runGesture(500, 1000, 1); return true; }
        }
        return false; // Позволяем системе использовать стрелки, если не перехватили
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        // Колесо теперь делает скролл с нашими лабораторными цифрами
        runGesture(x, y, Math.signum(delta));
    }

    private static void runGesture(float x, float y, float direction) {
        if (instance == null) return;
        Path path = new Path();
        path.moveTo(x, y);
        // Используем testDist, умноженный на направление колеса
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
