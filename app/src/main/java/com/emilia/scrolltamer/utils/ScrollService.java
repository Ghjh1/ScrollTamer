package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float velocity = 0;
    private static long lastEventTime = 0;
    private static int gestureCounter = 0; // Счетчик для ШИМ

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("D: %.0f | V: %.1f | PWM ACTIVE", 14.0f + velocity, velocity);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        if (interval < 220) {
            // Еще более мощный подхват в конце (+11) для 4/4
            float inc = (velocity < 10) ? 4.0f : 11.0f; 
            velocity += inc; 
            if (velocity > 36.0f) velocity = 36.0f; 
        } else {
            velocity = 0;
            gestureCounter = 0;
        }

        int finalStep = (int)(14 + velocity);
        float ratio = velocity / 36.0f;
        
        // РАСЧЕТ ВИРТУАЛЬНОГО T (Float)
        float startT = 39.0f;
        float targetT = (direction < 0) ? 24.0f : 21.0f; // Ускорили 4/4 (было 23/25)
        float virtualT = startT - (ratio * (startT - targetT));

        // РЕАЛИЗАЦИЯ ШИМ (PWM)
        // Если virtualT = 38.4, то в 40% случаев будет 38, в 60% будет 39.
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
