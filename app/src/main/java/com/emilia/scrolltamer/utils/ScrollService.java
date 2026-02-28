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
        return String.format("D: %.0f | V: %.1f | HYBRID MODE", 14.0f + velocity, velocity);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        if (interval < 220) {
            // Плавный разгон как в 190-й, но с очень мягким первым шагом
            float inc = (velocity < 10) ? 4.0f : 9.0f; 
            velocity += inc; 
            if (velocity > 36.0f) velocity = 36.0f; 
        } else {
            velocity = 0; // Полный сброс в идеальные 14-39
        }

        int finalStep = (int)(14 + velocity);
        
        // РАСЧЕТ ТАЙМИНГА (ГИБРИД)
        int finalT;
        if (velocity == 0) {
            finalT = 39; // Тот самый эталонный старт вниз/вверх
        } else {
            float ratio = velocity / 36.0f;
            // Берем базу 190-й для плавности: Вниз 23, Вверх 25
            float targetT = (direction < 0) ? 25.0f : 23.0f;
            // Линейное падение от 39 до цели
            finalT = (int)(39 - (ratio * (39 - targetT)));
        }

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (finalStep * direction));

        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, finalT);
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
