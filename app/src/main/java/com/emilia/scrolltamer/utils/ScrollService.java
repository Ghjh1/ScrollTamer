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
        return String.format("D: %.0f | V: %.1f", 14.0f + velocity, velocity);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        if (interval < 220) {
            // ПЛОТНЫЙ РАЗГОН: Убираем микро-старт, сразу даем рабочий ход
            // +4 на старте, +9.5 в зоне 3/4 для мощности
            float inc = (velocity < 15) ? 4.5f : 9.5f;
            velocity += inc; 
            if (velocity > 36.0f) velocity = 36.0f; // D до 50 (золотая середина)
        } else {
            velocity = 0; 
        }

        int finalStep = (int)(14 + velocity);
        float ratio = velocity / 36.0f;
        
        // ВОЗВРАЩАЕМ ФЛИНГ НА СТАРТ:
        // Базовое T теперь падает ЛИНЕЙНО, чтобы не было дерганий
        // Вниз: 38.5 -> 23. Вверх: 40.5 -> 25.
        float baseT = (direction < 0) ? 40.5f : 38.5f;
        float targetT = (direction < 0) ? 25.0f : 23.0f;
        
        // Линейный спад T (ratio^0.9) дает предсказуемую густоту
        int finalT = (int)(baseT - (Math.pow(ratio, 0.9) * (baseT - targetT)));

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (finalStep * direction));

        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, Math.max((int)targetT, finalT));
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
