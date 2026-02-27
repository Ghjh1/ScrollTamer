package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float dBase = 14.0f; // Порог отрыва
    private static int tStep = 40;      // Время шага
    
    private static Path currentPath;
    private static GestureDescription.Builder gestureBuilder;
    private static long lastEventTime = 0;
    private static final Handler handler = new Handler();
    private static boolean isDragging = false;
    private static float currentY = 0;
    private static float startX = 0;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static void setParams(float d, int t, int delay) {
        dBase = d;
        tStep = t;
    }

    public static String getDebugData() {
        return String.format("DRAG MODE | Base: %.1f | T: %d | Active: %b", dBase, tStep, isDragging);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        long now = System.currentTimeMillis();
        
        if (!isDragging || (now - lastEventTime > 300)) {
            // НОВЫЙ ЗАЦЕП (Первый щелчок)
            startX = x;
            currentY = y;
            currentPath = new Path();
            currentPath.moveTo(startX, currentY);
            
            // Сразу пробиваем мертвую зону
            currentY += dBase;
            currentPath.lineTo(startX, currentY);
            isDragging = true;
        } else {
            // ПОДДАЕМ ГАЗУ (Последующие щелчки)
            // Здесь мы добавляем мизерный шаг, т.к. система уже "проснулась"
            currentY += 1.0f; // Вот он, наш чистый шелк
            currentPath.lineTo(startX, currentY);
        }

        lastEventTime = now;

        // Формируем жест. 
        // Используем StrokeDescription с параметром willContinue = true
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(currentPath, 0, tStep, true);
        
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) {}

        // Сброс состояния через паузу
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> isDragging = false, 350);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; isDragging = false; }
}
