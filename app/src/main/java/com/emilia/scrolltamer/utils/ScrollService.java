package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static final float D_BREAK = 16.0f;
    private static final float D_SILK = 3.0f; // Увеличили, чтобы Android не игнорил
    private static final int WINDOW_LIMIT = 400; // Немного сузили для точности
    
    private static long lastEventTime = 0;
    private static long currentInterval = 0;
    private static String lastMode = "NONE";

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("INT: %d ms | MODE: %s", currentInterval, lastMode);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        long now = System.currentTimeMillis();
        currentInterval = (lastEventTime > 0) ? (now - lastEventTime) : 0;
        
        float direction = (delta > 0) ? 1.0f : -1.0f;
        float dynamicD;

        // Если пауза меньше 400мс - это серия (SILK)
        if (lastEventTime > 0 && currentInterval < WINDOW_LIMIT) {
            dynamicD = D_SILK;
            lastMode = "SILK (3px)";
        } else {
            dynamicD = D_BREAK;
            lastMode = "BREAK (16px)";
        }

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
