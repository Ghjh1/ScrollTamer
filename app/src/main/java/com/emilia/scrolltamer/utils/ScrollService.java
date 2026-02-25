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
    private static float lastStepValue = 0;
    private static long lockUntil = 0;
    private static boolean isEngineRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    public static String getDebugData() {
        long timeLeft = Math.max(0, lockUntil - System.currentTimeMillis());
        return String.format("VELOCITY: %.2f\nSTEP: %.2f\nLOCK: %dms", 
                targetVelocity, lastStepValue, timeLeft);
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;

        long now = System.currentTimeMillis();
        
        // 1. Если шлюз закрыт — игнорируем инерцию пальца
        if (now < lockUntil) return;

        // 2. Логика ЯКОРЯ (на базе v95)
        // Если крутим против движения — мгновенный стоп и шлюз на 150мс
        if (Math.signum(strength) != Math.signum(targetVelocity) && Math.abs(targetVelocity) > 0.5f) {
            targetVelocity = 0;
            lastStepValue = 0;
            lockUntil = now + 150; // Твои 150мс для "выпуска пара"
            return;
        }
        
        // 3. ТУРБО-РАЗГОН (v95 чистый)
        float turbo = 1.0f + (Math.abs(targetVelocity) / 450f);
        targetVelocity += (strength * 105 * turbo); 

        if (Math.abs(targetVelocity) > 3500) targetVelocity = Math.signum(targetVelocity) * 3500;

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 0.3f) {
            isEngineRunning = false;
            targetVelocity = 0;
            lastStepValue = 0;
            return;
        }

        // Затухание v95
        lastStepValue = targetVelocity * 0.16f; 
        if (Math.abs(lastStepValue) > 160) lastStepValue = Math.signum(lastStepValue) * 160;

        targetVelocity -= lastStepValue;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + lastStepValue);

        // 18мс - мягкость v95
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 18);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gd) {
                    handler.postDelayed(() -> {
                        if (isEngineRunning) runStep(startX, startY);
                    }, 3); 
                }
                @Override public void onCancelled(GestureDescription gd) { 
                    handler.postDelayed(() -> runStep(startX, startY), 5);
                }
            }, null);
        } catch (Exception e) {
            isEngineRunning = false;
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
