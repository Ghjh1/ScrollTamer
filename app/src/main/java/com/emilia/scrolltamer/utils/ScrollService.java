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
        int currentT = 39 - (int)(Math.pow(ratio, 1.2) * 19); // Падение до 20 (39-19)
        return String.format("D: %.0f | T: %dms | %s", 
            14.0f + velocity, Math.max(20, currentT), (ratio > 0.8 ? "WARP" : "SILK"));
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        if (interval < 220) {
            // Динамический инкремент: чем меньше скорость, тем мягче прибавка
            float increment = (velocity < 15) ? 3.0f : 6.0f;
            velocity += increment; 
            if (velocity > 34.0f) velocity = 34.0f; 
        } else {
            velocity = 0; 
        }

        int finalStep = (int)(14 + velocity);
        
        // Степень 1.2 вместо 1.5 — еще более раннее и плавное начало падения T
        float ratio = velocity / 34.0f;
        int finalT = 39 - (int)(Math.pow(ratio, 1.2) * 19); 
        if (finalT < 20) finalT = 20;

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
