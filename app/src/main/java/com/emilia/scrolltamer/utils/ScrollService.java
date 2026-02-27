package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    
    private static final float D_BREAK = 16.0f; // Удар для открытия замка
    private static final float D_SILK = 2.0f;   // База после открытия
    private static final int T_FIXED = 40;     
    
    private static long lastEventTime = 0;
    private static float currentExtra = 0f;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        float currentStep = (System.currentTimeMillis() - lastEventTime < 300) ? 
                            (D_SILK + currentExtra) : D_BREAK;
        return String.format("LOCK-PICK v163 | Step: %.1f", currentStep);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        long now = System.currentTimeMillis();
        long interval = now - lastEventTime;
        float direction = (delta > 0) ? 1.0f : -1.0f;
        float dynamicD;

        if (interval < 300) { 
            // ЗАМОК ОТКРЫТ: переходим на микро-шаги + мягкий разгон
            float force = (300f - interval) / 10f; 
            currentExtra += (force * force) / 20f; // Мягкая квадратичная добавка
            
            dynamicD = D_SILK + currentExtra;
            if (dynamicD > 400.0f) dynamicD = 400.0f;
        } else {
            // ПЕРВЫЙ УДАР: пробиваем 13-пиксельную зону
            currentExtra = 0;
            dynamicD = D_BREAK;
        }

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (dynamicD * direction));

        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, T_FIXED);
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }

        lastEventTime = now;
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
