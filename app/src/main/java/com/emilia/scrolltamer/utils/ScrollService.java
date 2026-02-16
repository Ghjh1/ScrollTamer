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
        Log.d("ScrollTamer", "v86: Режим Короткого Хода");
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;

        if (Math.signum(strength) != Math.signum(targetVelocity) && targetVelocity != 0) {
            targetVelocity = 0; 
        }
        
        // Уменьшаем силу импульса (было 230, стало 140) для короткого хода
        targetVelocity += (strength * 140); 

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 0.5f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        // Затухание 0.15 (вместо 0.22) — энергия живет дольше для связки кликов
        float step = targetVelocity * 0.15f; 
        
        // Лимитируем шаг сверху (не более 60 пикселей за раз)
        if (Math.abs(step) > 60) step = Math.signum(step) * 60;
        
        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + step);

        // 10 мс — очень быстрый и четкий жест (убираем дрожание)
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 10);
        
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gd) {
                // Маленькая пауза 10 мс для стабильности
                handler.postDelayed(() -> runStep(startX, startY), 10);
            }
            @Override public void onCancelled(GestureDescription gd) { 
                isEngineRunning = false; 
            }
        }, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
