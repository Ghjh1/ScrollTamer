package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private final float D_BASE = 14.0f; // Твоя золотая дистанция
    private final int T_BASE = 40;      // Твое золотое время

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return "MODE: GOLDEN 156 | D: 14.0 | T: 40";
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        // Восстанавливаем направление: 
        // Если крутим колесо вниз (delta > 0), свайп должен идти ВВЕРХ (тянем список)
        // Но в твоем тесте ты просил, чтобы оно тянуло вниз. 
        // Сделаем так: delta > 0 тянет ВНИЗ (прибавляем к Y)
        float direction = (delta > 0) ? 1.0f : -1.0f;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (instance.D_BASE * direction));

        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, instance.T_BASE);
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
