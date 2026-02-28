package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float velocity = 0;
    private static long lastEventTime = 0;
    private static long lockUntil = 0;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("V: %.1f | READY", velocity);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        if (now < lockUntil) return;

        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        // ЛОГИКА НАКОПЛЕНИЯ (Без циклов)
        if (interval < 150) {
            // Если крутим быстро — разгоняем шаг (от 18 до 45 пикселей)
            velocity += 2.0f;
            if (velocity > 27.0f) velocity = 27.0f;
        } else {
            // Если пауза — сброс на базу (Отмычка)
            velocity = 0;
        }

        float finalStep = 18.0f + velocity; // Минимум 18px для пробития Redmi

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (finalStep * direction));

        // Наш эталонный T=38 (микро-инерция без безумия)
        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, 38);
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
        
        // Маленький предохранитель, чтобы жесты не слиплись (10мс)
        lockUntil = now + 10; 
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
