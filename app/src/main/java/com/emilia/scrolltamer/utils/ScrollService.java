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
        // Показываем реальный текущий шаг для калибровки
        return String.format("STEP: %.0f | T: 39ms", 14.0f + velocity);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        // Если крутим активно (< 180ms между щелчками)
        if (interval < 180) {
            // Увеличиваем шаг быстрее и до большего предела
            velocity += 2.5f; 
            if (velocity > 45.0f) velocity = 45.0f; 
        } else {
            // Полный сброс на ювелирную базу 14px
            velocity = 0;
        }

        // РАСЧЕТ: База 14 + накопленный разгон
        int finalStep = (int)(14 + velocity);

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (finalStep * direction));

        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, 39);
            
        try {
            // Прямая отправка без посредников и очередей
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
