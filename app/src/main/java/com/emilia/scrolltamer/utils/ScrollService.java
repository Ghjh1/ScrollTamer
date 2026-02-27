package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private final float D_BASE = 14.0f; 
    private final float D_MAX = 600.0f; // Увеличили предел для полетов
    private final int T_FIXED = 40;     
    
    private static long lastEventTime = 0;
    private static float currentExtra = 0f;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("TURBO v161 | Step: %.1f | T: 40", 14.0f + currentExtra);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        long now = System.currentTimeMillis();
        long interval = now - lastEventTime;
        float direction = (delta > 0) ? 1.0f : -1.0f;

        if (interval < 250) {
            // АГРЕССИВНЫЙ БУСТ
            // Теперь даже при среднем интервале (100мс) прибавка будет ощутимой
            float force = (250f - interval) / 5f; 
            currentExtra += (force * force) / 10f; // Квадратичное ускорение!
        } else {
            currentExtra = 0;
        }
        
        if (currentExtra > D_MAX) currentExtra = D_MAX;

        float dynamicD = D_BASE + currentExtra;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (dynamicD * direction));

        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, T_FIXED);
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }

        lastEventTime = now;
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
