package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static final float D_BREAK = 16.0f;
    private static final float D_SILK = 1.0f; // Тестируем на самом минимуме
    private static final int WINDOW_LIMIT = 600; // Берем с запасом для замеров
    
    private static long lastEventTime = 0;
    private static long lastInterval = 0;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("INTERVAL: %d ms | Last: %s", 
            lastInterval, (lastInterval < WINDOW_LIMIT && lastInterval > 0) ? "SILK" : "BREAK");
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        long now = System.currentTimeMillis();
        if (lastEventTime > 0) {
            lastInterval = now - lastEventTime;
        }
        
        float direction = (delta > 0) ? 1.0f : -1.0f;
        
        // Если уложились в 600мс - пробуем шёлк, иначе - пробиваем заново
        float dynamicD = (lastInterval > 0 && lastInterval < WINDOW_LIMIT) ? D_SILK : D_BREAK;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (dynamicD * direction));

        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, 40);
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }

        lastEventTime = now;
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
