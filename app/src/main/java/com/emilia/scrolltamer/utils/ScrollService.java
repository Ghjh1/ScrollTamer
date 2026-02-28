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
            // Быстрый подхват (+7), но мягкое начало (+3.5)
            float inc = (velocity < 12) ? 3.5f : 7.0f;
            velocity += inc; 
            if (velocity > 34.0f) velocity = 34.0f; 
        } else {
            velocity = 0; 
        }

        int finalStep = (int)(14 + velocity);
        float ratio = velocity / 34.0f;
        
        // БАЗА 186-й (39 -> 23)
        int finalT = 39 - (int)(Math.pow(ratio, 0.8) * 16); 
        
        // Мягкая поправка "ВВЕРХ": +1.5мс к вязкости
        if (direction < 0) finalT += 1.5f;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (finalStep * direction));

        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, Math.max(23, finalT));
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
