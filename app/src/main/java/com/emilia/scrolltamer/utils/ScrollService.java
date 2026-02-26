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
    private static float lastX = 500, lastY = 1000; // Центр экрана примерно

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("LAB v133: D=%.0fpx | T=%dms", testDist, testTime);
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) { testDist += 1; return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) { testDist -= 1; return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) { testTime += 10; return true; }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) { testTime -= 10; return true; }
            if (keyCode == KeyEvent.KEYCODE_SPACE) { runTest(); return true; }
        }
        return super.onKeyEvent(event);
    }

    public static void scroll(float delta, float x, float y) {
        lastX = x; lastY = y; // Запоминаем координаты мыши для теста
        // В этой версии колесо ничего не делает, только стрелки и пробел
    }

    private static void runTest() {
        if (instance == null) return;
        Path path = new Path();
        path.moveTo(lastX, lastY);
        path.lineTo(lastX, lastY + testDist);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, Math.max(10, testTime));
        instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
