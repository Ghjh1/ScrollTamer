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
        Log.d("ScrollTamer", "v85: Режим Сливочного Масла");
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;

        // Мгновенный стоп при смене направления
        if (Math.signum(strength) != Math.signum(targetVelocity) && targetVelocity != 0) {
            targetVelocity = 0; 
        }
        
        // Чуть более легкий импульс для старта
        targetVelocity += (strength * 230); 

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        // Уменьшили порог остановки, чтобы скролл не "залипал" в конце
        if (Math.abs(targetVelocity) < 0.5f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        // Оптимальный шаг для плавности
        float step = targetVelocity * 0.22f; 
        if (Math.abs(step) > 100) step = Math.signum(step) * 100;
        
        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + step);

        // 30 мс - золотая середина "зацепа"
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 30);
        
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gd) {
                // Краткий "вдох" для системы (5 мс)
                handler.postDelayed(() -> runStep(startX, startY), 5);
            }
            @Override public void onCancelled(GestureDescription gd) { 
                targetVelocity = 0; 
                isEngineRunning = false; 
            }
        }, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
