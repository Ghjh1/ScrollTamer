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
    private static boolean isEngineRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    public static String getDebugData() {
        return String.format("VELOCITY: %.2f\nSTEP: %.2f\nACTIVE: %s", 
                targetVelocity, lastStepValue, isEngineRunning ? "YES" : "NO");
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;

        // ХИРУРГИЧЕСКИЙ ТОРМОЗ: Любое движение против вектора = Мгновенный 0
        if (Math.signum(strength) != Math.signum(targetVelocity) && Math.abs(targetVelocity) > 0.1f) {
            targetVelocity = 0;
            lastStepValue = 0;
            // Мы не выходим из метода, чтобы следующий клик в ту же сторону уже сработал, 
            // но этот конкретный клик только гасит инерцию.
            return; 
        }
        
        // ТУРБО-РАЗГОН (v95)
        float turbo = 1.0f + (Math.abs(targetVelocity) / 400f);
        targetVelocity += (strength * 110 * turbo); 

        // Лимит для безопасности
        if (Math.abs(targetVelocity) > 3800) targetVelocity = Math.signum(targetVelocity) * 3800;

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 0.2f) {
            isEngineRunning = false;
            targetVelocity = 0;
            lastStepValue = 0;
            return;
        }

        // Затухание 0.16
        lastStepValue = targetVelocity * 0.16f; 
        if (Math.abs(lastStepValue) > 170) lastStepValue = Math.signum(lastStepValue) * 170;

        targetVelocity -= lastStepValue;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + lastStepValue);

        // 18мс - баланс мягкости
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 18);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gd) {
                    handler.postDelayed(() -> {
                        if (isEngineRunning) runStep(startX, startY);
                    }, 2); 
                }
                @Override public void onCancelled(GestureDescription gd) { 
                    isEngineRunning = false;
                }
            }, null);
        } catch (Exception e) {
            isEngineRunning = false;
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
