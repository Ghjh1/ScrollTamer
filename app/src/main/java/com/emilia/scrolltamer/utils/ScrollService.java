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
        float ratio = velocity / 34.0f; 
        // Степень 0.8 дает "выпуклый" график: T падает быстро в начале и замедляется в конце
        int currentT = 39 - (int)(Math.pow(ratio, 0.8) * 16); // 39 -> 23
        return String.format("D: %.0f | T: %dms | %s", 
            14.0f + velocity, Math.max(23, currentT), (ratio > 0.7 ? "FLOW" : "SILK"));
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        if (interval < 220) {
            // Мягкий вход (+3), затем рабочий ход (+6)
            float increment = (velocity < 12) ? 3.0f : 6.0f;
            velocity += increment; 
            if (velocity > 34.0f) velocity = 34.0f; 
        } else {
            velocity = 0; 
        }

        int finalStep = (int)(14 + velocity);
        
        // Математика T: Ранний подхват (степень 0.8) и финиш на 23мс
        float ratio = velocity / 34.0f;
        int finalT = 39 - (int)(Math.pow(ratio, 0.8) * 16); 
        if (finalT < 23) finalT = 23;

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
