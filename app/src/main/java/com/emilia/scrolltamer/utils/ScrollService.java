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
        // Рассчитываем T для дебага (39 -> 25)
        int currentT = 39 - (int)(velocity / 3.2f); 
        return String.format("D: %.0f | T: %dms", 14.0f + velocity, Math.max(25, currentT));
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        if (interval < 200) { // Чуть расширили окно подхвата
            // Агрессивный набор мощности: +10 за щелчок
            velocity += 10.0f; 
            if (velocity > 46.0f) velocity = 46.0f; // Итого D = 14 + 46 = 60
        } else {
            velocity = 0; // Возврат к ювелирной базе
        }

        int finalStep = (int)(14 + velocity);

        // Динамический T: от 39 до 25. 
        // Делим на 3.2, чтобы при velocity=46 мы четко выходили на T=25
        int finalT = 39 - (int)(velocity / 3.2f); 
        if (finalT < 25) finalT = 25;

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
