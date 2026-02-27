package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static final float D_START = 15.0f; // Первый рывок
    private static final float D_CONTINUE = 3.0f; // Микро-шаги внутри потока
    
    private static long lastEventTime = 0;
    private static float currentY = 500f; // Виртуальная координата пальца

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return "MODE: CONTINUOUS DRAG v167";
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        long now = System.currentTimeMillis();
        float direction = (delta > 0) ? 1.0f : -1.0f;
        
        Path path = new Path();
        path.moveTo(x, currentY);
        
        // Если крутим часто, считаем, что палец не отрывали
        if (now - lastEventTime < 300) {
            currentY += (D_CONTINUE * direction);
        } else {
            currentY = y; // Сброс позиции
            path.moveTo(x, currentY);
            currentY += (D_START * direction);
        }
        
        path.lineTo(x, currentY);

        // willContinue = true — это КЛЮЧ. Говорит системе не завершать жест.
        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, 60, true);
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }

        lastEventTime = now;
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
