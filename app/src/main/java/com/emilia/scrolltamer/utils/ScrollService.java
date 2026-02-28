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
        int currentT = 39 - (int)(velocity / 16); 
        return String.format("D: %.0f | T: %dms", 14.0f + velocity, Math.max(34, currentT));
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        if (interval < 180) {
            // Резко прибавляем мощь (5.0 вместо 3.5)
            velocity += 5.0f; 
            if (velocity > 81.0f) velocity = 81.0f; // Итого D до 95
        } else {
            velocity = 0; // Возврат к ювелирной базе 14px
        }

        int finalStep = (int)(14 + velocity);

        // Динамический расчет T: от 39 до 34
        int finalT = 39 - (int)(velocity / 16); 
        if (finalT < 34) finalT = 34;

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
