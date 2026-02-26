package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return "CLEAN SLATE MODE | RAW INPUT";
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        float sign = Math.signum(delta);
        
        // ТЕОРЕТИЧЕСКИЙ ПУТЬ ПРОБОЯ (Тот самый X)
        // Давай начнем с 12 пикселей. Если не сдвинется - будем поднимать.
        float bypass = sign * 12.0f; 
        float target = sign * 1.0f; // Наш заветный 1 пиксель

        Path path = new Path();
        path.moveTo(x, y);            // Точка А (Старт)
        path.lineTo(x, y - bypass);   // Точка Б (Программный замах - ВЗЛОМ)
        path.lineTo(x, y + target);   // Точка В (Финальная позиция - ЦЕЛЬ)

        // Один-единственный "выстрел". Без повторов. 
        // Время 20мс - чтобы система успела заметить вектор, но не успела отрисовать откат.
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 20);
        
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
