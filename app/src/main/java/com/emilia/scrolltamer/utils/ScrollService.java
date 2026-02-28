package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float velocity = 0;
    private static long lastEventTime = 0;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("STEP: %.1f | T: 39ms", 18.0f + velocity);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        // Если крутим быстро (< 200ms) — чуть наращиваем шаг (акселерация 112-й)
        if (interval < 200) {
            velocity += 1.5f;
            if (velocity > 30f) velocity = 30f;
        } else {
            // Если пауза — сброс на чистый отрыв
            velocity = 0;
        }

        // БАЗА: 18px (отрыв) + накопленная скорость
        int finalStep = (int)(18 + velocity);

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (finalStep * direction));

        // Твой золотой стандарт T=39
        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, 39);
            
        try {
            // Шлем жест мгновенно и забываем о нем. 
            // Это освобождает поток для следующего щелчка.
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
