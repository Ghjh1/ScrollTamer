package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float targetVelocity = 0;
    private static long lockUntil = 0;
    private static boolean isEngineRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    public static String getDebugData() {
        return String.format("VELOCITY: %.2f\nACTIVE: %s\nLOCK: %dms", 
                targetVelocity, isEngineRunning ? "YES" : "NO", Math.max(0, lockUntil - System.currentTimeMillis()));
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        if (now < lockUntil) return;

        // ПАРАЛЛЕЛЬНЫЙ ТОРМОЗ (АННИГИЛЯЦИЯ)
        // Если знаки разные — это удар по тормозам
        if (Math.signum(strength) != Math.signum(targetVelocity) && Math.abs(targetVelocity) > 1f) {
            targetVelocity = 0; 
            lockUntil = now + 150; // Шлюз для пальца
            // Мы не останавливаем engine, он сам "заглохнет" на следующем шаге, увидев 0
            return;
        }

        float turbo = 1.0f + (Math.abs(targetVelocity) / 450f);
        targetVelocity += (strength * 105 * turbo); 

        if (Math.abs(targetVelocity) > 3500) targetVelocity = Math.signum(targetVelocity) * 3500;

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y);
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 0.2f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        float step = targetVelocity * 0.16f;
        if (Math.abs(step) > 160) step = Math.signum(step) * 160;
        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + step);

        // Используем минимально возможную длительность, чтобы быстрее освобождать очередь
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 12);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gd) {
                    handler.postDelayed(() -> {
                        if (Math.abs(targetVelocity) > 0.1f) runStep(startX, startY);
                        else isEngineRunning = false;
                    }, 1); // Минимальная задержка для "плотности"
                }
                @Override public void onCancelled(GestureDescription gd) { isEngineRunning = false; }
            }, null);
        } catch (Exception e) { isEngineRunning = false; }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
