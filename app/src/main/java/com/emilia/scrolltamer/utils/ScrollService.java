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
        Log.d("ScrollTamer", "v88: Обуздание Мустанга");
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;

        // Улучшенный ТОРМОЗ: Активное противодействие при смене направления
        if (Math.signum(strength) != Math.signum(targetVelocity) && targetVelocity != 0) {
            targetVelocity *= -0.2f; // Небольшой реверс для мгновенной остановки
        }
        
        // Прогрессия с ограничителем (Max Velocity = 2500)
        float multiplier = 1.0f + (Math.min(Math.abs(targetVelocity), 1500f) / 600f);
        targetVelocity += (strength * 100 * multiplier); 

        // Жесткий лимит, чтобы не "вылететь с трассы"
        if (Math.abs(targetVelocity) > 2500) targetVelocity = Math.signum(targetVelocity) * 2500;

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

        float step = targetVelocity * 0.15f; 
        
        // Лимит шага для стабильности жеста
        if (Math.abs(step) > 150) step = Math.signum(step) * 150;

        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(startX, startY);
        // Безопасные координаты: следим, чтобы палец не ушел за пределы видимости
        float endY = startY + step;
        if (endY < 10) endY = 10;
        if (endY > 2300) endY = 2300; 

        p.lineTo(startX, endY);

        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 15);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gd) {
                    handler.postDelayed(() -> runStep(startX, startY), 10);
                }
                @Override public void onCancelled(GestureDescription gd) { 
                    targetVelocity = 0;
                    isEngineRunning = false; 
                }
            }, null);
        } catch (Exception e) {
            targetVelocity = 0;
            isEngineRunning = false;
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
