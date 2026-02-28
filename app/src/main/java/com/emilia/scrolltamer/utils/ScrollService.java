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
        return String.format("D: %.0f | V_STEP: %.1f", 14.0f + velocity, velocity);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        if (interval < 220) {
            // УСКОРЕННЫЙ ПОДХВАТ: +7 вместо +6 для динамики в середине
            // Но для движения ВВЕРХ (direction < 0) делаем чуть слабее (+5.8)
            float stepGain = (direction < 0) ? 5.8f : 7.0f;
            velocity += stepGain; 
            if (velocity > 34.0f) velocity = 34.0f; 
        } else {
            velocity = 0; 
        }

        int finalStep = (int)(14 + velocity);
        float ratio = velocity / 34.0f;
        
        // АСИММЕТРИЯ ТАЙМИНГА:
        // Вниз: 39 -> 23ms (быстрое масло)
        // Вверх: 40 -> 25ms (густой бархат, чтобы компенсировать резкий палец)
        int baseT = (direction < 0) ? 40 : 39;
        int targetT = (direction < 0) ? 25 : 23;
        int diff = baseT - targetT;

        int finalT = baseT - (int)(Math.pow(ratio, 0.8) * diff);

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
