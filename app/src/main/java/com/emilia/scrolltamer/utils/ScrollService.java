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
        Log.d("ScrollTamer", "v82: Стабильный Шёлк готов");
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;
        
        // Снижаем агрессивность в 2 раза
        targetVelocity += (strength * 150); 
        
        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 1.0f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        // Лимитируем шаг, чтобы не "прыгать"
        float step = targetVelocity * 0.15f; 
        if (Math.abs(step) > 80) step = Math.signum(step) * 80;
        
        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + step);

        // 30мс - длительность самого жеста (плавность перемещения пальца)
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 30);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gd) {
                    // Пауза 16мс (соответствует 60Гц экрану)
                    handler.postDelayed(() -> runStep(startX, startY), 16);
                }
                @Override public void onCancelled(GestureDescription gd) { isEngineRunning = false; }
            }, null);
        } catch (Exception e) {
            isEngineRunning = false;
            Log.e("ScrollTamer", "Ошибка диспетчера жестов", e);
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
