package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private final float D_MIN = 14.0f; // Минимум (твой шелк)
    private final float D_MAX = 150.0f; // Максимум за один прыжок
    private final int T_FIXED = 40;    // Держим границу флинга железно
    
    private static long lastEventTime = 0;
    private static float currentVelocity = 1.0f;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("DYN-STEP v159 | D: %.1f | T: 40", 14.0f * currentVelocity);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        long now = System.currentTimeMillis();
        long interval = now - lastEventTime;
        float direction = (delta > 0) ? 1.0f : -1.0f;

        // Расчет акселерации: если между щелчками меньше 100мс - ускоряемся
        if (interval < 100) {
            currentVelocity += 0.5f; // Наращиваем мощь
        } else {
            currentVelocity = 1.0f;  // Остываем до базы
        }
        
        // Ограничиваем скорость, чтобы не улететь в космос
        if (currentVelocity > 10.0f) currentVelocity = 10.0f;

        float dynamicD = instance.D_MIN * currentVelocity;
        if (dynamicD > instance.D_MAX) dynamicD = instance.D_MAX;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (dynamicD * direction));

        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, instance.T_FIXED);
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }

        lastEventTime = now;
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
