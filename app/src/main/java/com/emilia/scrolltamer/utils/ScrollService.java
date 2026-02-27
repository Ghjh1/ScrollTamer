package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private final float D_BASE = 14.0f; 
    private final int T_BASE = 40;
    
    private static long lastGestureEndTime = 0;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return "MODE: CONVEYOR v157 | D: 14.0 | T: 40";
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        long now = System.currentTimeMillis();
        float direction = (delta > 0) ? 1.0f : -1.0f;

        // "Конвейерная" логика:
        // Если предыдущий жест еще должен идти, мы планируем новый сразу за ним
        long startTime = Math.max(now, lastGestureEndTime);
        
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (instance.D_BASE * direction));

        // Вычисляем время старта относительно текущего момента
        long startDelay = startTime - now;
        
        // Ограничиваем очередь, чтобы не улететь в бесконечный скролл (макс 200мс вперед)
        if (startDelay > 200) return;

        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, startDelay, instance.T_BASE);
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
            lastGestureEndTime = startTime + instance.T_BASE;
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
