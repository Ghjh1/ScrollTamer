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
    private static int brakeCounter = 0;
    private static boolean isEngineRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    public static String getDebugData() {
        return String.format("VELOCITY: %.2f\nSTEP: %.2f\nACTIVE: %s\nBRAKE: %d", 
                targetVelocity, lastStepValue, isEngineRunning ? "YES" : "NO", brakeCounter);
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;

        // УСИЛЕННЫЙ ТОРМОЗ (Ловим даже быстрые клики)
        if (Math.signum(strength) != Math.signum(targetVelocity) && Math.abs(targetVelocity) > 1) {
            brakeCounter++;
            if (brakeCounter >= 3) {
                targetVelocity = 0;
                brakeCounter = 0;
            } else {
                targetVelocity *= 0.4f; 
            }
            return; 
        }
        
        brakeCounter = 0;
        // Импульс для мягкого старта
        targetVelocity += (strength * 90); 

        if (Math.abs(targetVelocity) > 2100) targetVelocity = Math.signum(targetVelocity) * 2100;

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 0.4f) {
            isEngineRunning = false;
            targetVelocity = 0;
            lastStepValue = 0;
            return;
        }

        // Плавное затухание (0.20)
        lastStepValue = targetVelocity * 0.20f; 
        if (Math.abs(lastStepValue) > 95) lastStepValue = Math.signum(lastStepValue) * 95;

        targetVelocity -= lastStepValue;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + lastStepValue);

        // КИЛЛЕР-ФИЧА: 25мс - максимально мягкий контакт "подушечкой пальца"
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 25);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gd) {
                    // Пауза 2мс - почти непрерывный поток
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
