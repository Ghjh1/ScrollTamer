package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private final float D_DEADZONE = 13.0f; // Твой порог отрыва
    private final float D_MIN_STEP = 1.0f;  // Минимальный полезный сдвиг
    private final float D_MAX = 250.0f;     // Максимальный полет
    private final int T_FIXED = 40;         // Граница флинга
    
    private static long lastEventTime = 0;
    private static float currentExtra = 0f; // Накопленное ускорение

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("FLOW v160 | Step: %.1f | T: 40", 13.0f + 1.0f + currentExtra);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        long now = System.currentTimeMillis();
        long interval = now - lastEventTime;
        float direction = (delta > 0) ? 1.0f : -1.0f;

        // Плавный апскейлинг:
        if (interval < 150) {
            // Если крутишь активно, добавляем к шагу "вес"
            // Чем меньше интервал, тем быстрее растет добавка
            float boost = (150f - interval) / 10f; 
            currentExtra += boost; 
        } else {
            // Остывание: если была пауза, сбрасываем ускорение
            currentExtra = 0;
        }
        
        // Ограничиваем сверху
        if (currentExtra > instance.D_MAX) currentExtra = instance.D_MAX;

        // Итоговая дистанция = Мертвая зона + Базовый шаг + Накопленный бонус
        float dynamicD = instance.D_DEADZONE + instance.D_MIN_STEP + currentExtra;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (dynamicD * direction));

        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, instance.T_FIXED);
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }

        lastEventTime = now;
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
