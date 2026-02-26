package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float testLimit = 10.0f; // Начинаем с 10

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("CALIBRATION MODE | CURRENT LIMIT: %.1f", testLimit);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        // Если крутим ВВЕРХ (delta < 0) - увеличиваем порог для поиска
        if (delta < 0) {
            testLimit += 2.0f;
            if (testLimit > 150) testLimit = 10; // Сброс если ушли далеко
        }

        // Если крутим ВНИЗ (delta > 0) - выполняем тестовый удар текущим лимитом
        if (delta > 0) {
            Path path = new Path();
            path.moveTo(x, y);
            path.lineTo(x, y + testLimit);

            // Время ставим побольше (100мс), чтобы убрать эффект "выстрела" и инерцию системы
            GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 100);
            
            try {
                instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
            } catch (Exception e) { }
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
