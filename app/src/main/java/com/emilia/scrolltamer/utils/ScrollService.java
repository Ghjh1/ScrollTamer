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
        Log.d("ScrollTamer", "v83: Режим хирургического Шёлка");
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;

        // Если крутим в противоположную сторону - мгновенно гасим старую инерцию
        if (Math.signum(strength) != Math.signum(targetVelocity) && targetVelocity != 0) {
            targetVelocity = 0; 
        }
        
        targetVelocity += (strength * 180); 

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 2.0f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        // Вычисляем шаг
        float step = targetVelocity * 0.25f; // Увеличили отзывчивость
        if (Math.abs(step) > 100) step = Math.signum(step) * 100;
        
        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + step);

        // Укорачиваем сам жест до 20мс для мгновенного отклика
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 20);
        
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gd) {
                // Минимальная пауза для четкости
                handler.postDelayed(() -> runStep(startX, startY), 5);
            }
            @Override public void onCancelled(GestureDescription gd) { 
                targetVelocity *= 0.5f; // Гасим энергию при отмене, чтобы не "стреляло"
                isEngineRunning = false; 
            }
        }, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
