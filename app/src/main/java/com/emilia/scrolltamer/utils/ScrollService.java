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

        // РЕЖИМ ПОРТНОГО: Если крутим назад при движении вперед - полный СТОП
        if (Math.signum(strength) != Math.signum(targetVelocity) && Math.abs(targetVelocity) > 5) {
            targetVelocity = 0;
            lastStepValue = 0;
            return; // Первый обратный щелчок просто останавливает поток
        }
        
        targetVelocity += (strength * 130); 
        if (Math.abs(targetVelocity) > 2200) targetVelocity = Math.signum(targetVelocity) * 2200;

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 1.0f) {
            isEngineRunning = false;
            targetVelocity = 0;
            lastStepValue = 0;
            return;
        }

        lastStepValue = targetVelocity * 0.18f; 
        if (Math.abs(lastStepValue) > 130) lastStepValue = Math.signum(lastStepValue) * 130;

        targetVelocity -= lastStepValue;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + lastStepValue);

        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 10);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gd) {
                    handler.postDelayed(() -> runStep(startX, startY), 5);
                }
                @Override public void onCancelled(GestureDescription gd) { isEngineRunning = false; }
            }, null);
        } catch (Exception e) {
            isEngineRunning = false;
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
