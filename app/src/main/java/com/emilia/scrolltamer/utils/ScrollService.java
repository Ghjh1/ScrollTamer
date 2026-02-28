package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float velocity = 0;
    private static long lastEventTime = 0;
    private static int gestureCounter = 0;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("D: %.0f | V: %.1f | FINAL CALIBRATION", 14.0f + velocity, velocity);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        if (interval < 220) {
            float inc = (velocity < 10) ? 4.0f : 11.0f; 
            velocity += inc; 
            if (velocity > 35.0f) velocity = 35.0f; // Тот самый фикс на 35
        } else {
            velocity = 0;
            gestureCounter = 0;
        }

        int finalStep = (int)(14 + velocity);
        float ratio = velocity / 35.0f; // Пересчет под 35
        
        float startT = 39.0f;
        float targetT = (direction < 0) ? 24.0f : 21.0f; 
        float virtualT = startT - (ratio * (startT - targetT));

        int floorT = (int) Math.floor(virtualT);
        float fractionalPart = virtualT - floorT;
        
        gestureCounter++;
        int finalT = ( (gestureCounter % 10) < (fractionalPart * 10) ) ? floorT + 1 : floorT;

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
