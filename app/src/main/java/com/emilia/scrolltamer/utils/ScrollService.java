package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float targetVelocity = 0;
    private static boolean isEngineRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;

        // 1. Ограничиваем импульс, чтобы не было "выстрела"
        float newImpulse = strength * 120; 
        if (Math.abs(newImpulse) > 400) newImpulse = Math.signum(newImpulse) * 400;
        
        targetVelocity += newImpulse;

        if (!isEngineRunning) {
            isEngineRunning = true;
            // Используем реальные координаты X,Y от мыши, чтобы чат понимал, где скроллим
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 1.0f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        // 2. Делаем затухание чуть быстрее (0.2 вместо 0.1) для контроля в чатах
        float step = targetVelocity * 0.2f;
        
        // Ограничиваем максимальный физический шаг за один жест (не более 150 пикселей)
        if (Math.abs(step) > 150) step = Math.signum(step) * 150;
        
        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + step);

        // 3. Увеличиваем длительность жеста до 40мс. 
        // Это сделает движение "тяжелее" и понятнее для браузера.
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 40);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                // Небольшая пауза для стабилизации кадра
                handler.postDelayed(() -> runStep(startX, startY), 10);
            }
            @Override
            public void onCancelled(GestureDescription gd) { isEngineRunning = false; }
        }, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() { instance = null; super.onDestroy(); }
}
