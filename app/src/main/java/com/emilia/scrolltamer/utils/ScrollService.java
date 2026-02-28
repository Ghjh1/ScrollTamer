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
        return String.format("D: %.0f | V: %.1f | T39 FIXED", 14.0f + velocity, velocity);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        if (interval < 220) {
            // Плотный подхват в середине (+8), но аккуратный вход (+4)
            float inc = (velocity < 12) ? 4.0f : 8.5f;
            velocity += inc; 
            if (velocity > 36.0f) velocity = 36.0f; 
        } else {
            velocity = 0; 
        }

        int finalStep = (int)(14 + velocity);
        float ratio = velocity / 36.0f;
        
        // ЗАКОН T=39: Оба направления стартуют с 39мс
        int startT = 39;
        // Финиш: Вниз 23мс, Вверх 25мс (для мягкости)
        int endT = (direction < 0) ? 25 : 23;
        
        // КРИВАЯ: Используем степень 1.3, чтобы T подольше держалось около 39 
        // на малых скоростях, сохраняя ту самую "точность флинга"
        int finalT = startT - (int)(Math.pow(ratio, 1.3) * (startT - endT));

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (finalStep * direction));

        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, Math.max(endT, finalT));
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
