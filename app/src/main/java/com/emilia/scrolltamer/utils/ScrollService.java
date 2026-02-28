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
        float ratio = velocity / 46.0f; 
        // Теперь падение до 22 (39 - 17 = 22)
        int currentT = 39 - (int)(Math.pow(ratio, 2) * 17); 
        return String.format("D: %.0f | T: %dms | %s", 
            14.0f + velocity, Math.max(22, currentT), (ratio > 0.85 ? "WARP" : "SILK"));
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        if (interval < 220) {
            velocity += 4.0f; 
            if (velocity > 46.0f) velocity = 46.0f; 
        } else {
            velocity = 0; 
        }

        int finalStep = (int)(14 + velocity);
        
        // Математика T22
        float ratio = velocity / 46.0f;
        int finalT = 39 - (int)(Math.pow(ratio, 2) * 17); 
        if (finalT < 22) finalT = 22;

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
