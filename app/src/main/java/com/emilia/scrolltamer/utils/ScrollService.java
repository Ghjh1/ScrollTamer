package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float dBase = 14.0f;
    private static int tStep = 40;
    
    private static long lastEventTime = 0;
    private static final Handler handler = new Handler();
    private static boolean isDragging = false;
    private static float currentY = 0;
    private static float startX = 0;
    private static float direction = 1.0f;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static void setParams(float d, int t, int delay) {
        dBase = d;
        tStep = t;
    }

    public static String getDebugData() {
        return String.format("PEDAL v155 | D: %.1f | T: %d | Drag: %b", dBase, tStep, isDragging);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        long now = System.currentTimeMillis();
        // Определяем направление (delta > 0 это вниз, delta < 0 это вверх)
        float currentDirection = (delta > 0) ? 1.0f : -1.0f;
        
        if (!isDragging || (now - lastEventTime > 400) || (currentDirection != direction)) {
            // НОВЫЙ ЗАЦЕП или смена направления
            startX = x;
            currentY = y;
            direction = currentDirection;
            isDragging = true;
            
            Path path = new Path();
            path.moveTo(startX, currentY);
            currentY += (dBase * direction); // Первый прыжок через мертвую зону
            path.lineTo(startX, currentY);
            
            dispatch(path, false);
        } else {
            // ПЕДАЛЬ (уже тащим)
            Path path = new Path();
            path.moveTo(startX, currentY);
            currentY += (2.0f * direction); // Микро-шаг
            path.lineTo(startX, currentY);
            
            dispatch(path, true);
        }

        lastEventTime = now;
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> isDragging = false, 500);
    }

    private static void dispatch(Path path, boolean cont) {
        // Увеличиваем время для "продолжающегося" жеста, чтобы он не обрывался слишком резко
        int duration = cont ? tStep * 2 : tStep;
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, duration, cont);
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) {}
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; isDragging = false; }
}
