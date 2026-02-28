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
        int currentT = 39 - (int)(velocity / 15); // Динамический T
        return String.format("D: %.0f | T: %dms", 14.0f + velocity, Math.max(36, currentT));
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        // Если крутим активно
        if (interval < 180) {
            velocity += 3.5f; // Быстрее набираем мощь
            if (velocity > 51.0f) velocity = 51.0f; // Итого D до 65
        } else {
            velocity = 0; // Сброс на базу
        }

        // РАСЧЕТ ДИСТАНЦИИ (14..65)
        int finalStep = (int)(14 + velocity);

        // РАСЧЕТ ТАЙМИНГА (39..36)
        // Чем выше velocity, тем меньше T (больше флинг)
        int finalT = 39 - (int)(velocity / 15); 
        if (finalT < 36) finalT = 36;

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
