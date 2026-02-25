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

        // ДИНАМИЧЕСКИЙ ТОРМОЗ (Логарифмический)
        if (Math.signum(strength) != Math.signum(targetVelocity) && Math.abs(targetVelocity) > 2) {
            targetVelocity *= 0.4f; // Гасим 60% скорости за каждый щелчок назад
            if (Math.abs(targetVelocity) < 15) targetVelocity = 0; // "Якорь" на малых скоростях
            return; 
        }
        
        // ТУРБО-РАЗГОН: Чем быстрее летим, тем легче ускоряться
        float turbo = 1.0f + (Math.abs(targetVelocity) / 450f);
        targetVelocity += (strength * 105 * turbo); 

        // Подняли планку максимума, раз ты хочешь летать быстрее
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

        // Затухание (0.16) - сделали чуть более инерционным для "полёта"
        lastStepValue = targetVelocity * 0.16f; 
        if (Math.abs(lastStepValue) > 160) lastStepValue = Math.signum(lastStepValue) * 160;

        targetVelocity -= lastStepValue;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + lastStepValue);

        // 18мс - ювелирная мягкость (чуть быстрее отклик, чем в v94)
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
