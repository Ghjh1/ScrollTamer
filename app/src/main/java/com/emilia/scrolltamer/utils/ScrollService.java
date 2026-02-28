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
            // ТРЕХСТУПЕНЧАТЫЙ РАЗГОН для идеальной мягкости
            float inc;
            if (velocity < 8) inc = 2.5f;      // Микро-старт (убираем ступеньки)
            else if (velocity < 20) inc = 5.0f; // Плотная середина
            else inc = 8.5f;                   // Турбо-подхват
            
            velocity += inc; 
            if (velocity > 34.0f) velocity = 34.0f; 
        } else {
            velocity = 0; 
        }

        int finalStep = (int)(14 + velocity);
        float ratio = velocity / 34.0f;
        
        // РАННИЙ ФЛИНГ: Степень 0.6 вместо 0.8. 
        // Время (T) будет падать ОЧЕНЬ быстро в начале.
        // Вниз: 39 -> 23. Вверх: 41 -> 25.
        int baseT = (direction < 0) ? 41 : 39;
        int targetT = (direction < 0) ? 25 : 23;
        int diff = baseT - targetT;

        // Формула с ранним подхватом (pow 0.6)
        int finalT = baseT - (int)(Math.pow(ratio, 0.6) * diff);

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (finalStep * direction));

        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, Math.max(targetT, finalT));
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
