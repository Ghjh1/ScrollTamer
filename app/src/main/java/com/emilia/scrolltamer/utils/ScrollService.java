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

        // ТОРМОЗНОЙ ПУТЬ (3 щелчка)
        if (Math.signum(strength) != Math.signum(targetVelocity) && Math.abs(targetVelocity) > 2) {
            brakeCounter++;
            if (brakeCounter == 1) targetVelocity *= 0.6f; // Гасим 40%
            else if (brakeCounter == 2) targetVelocity *= 0.3f; // Гасим еще
            else {
                targetVelocity = 0; // Полный стоп на 3-й клик
                brakeCounter = 0;
            }
            return;
        }
        
        // Сброс счетчика тормоза, если крутим в ту же сторону
        brakeCounter = 0;
        
        // Импульс v91 (короткий ход)
        targetVelocity += (strength * 80); 

        if (Math.abs(targetVelocity) > 2000) targetVelocity = Math.signum(targetVelocity) * 2000;

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        // Жесткий порог отсечки (0.5), чтобы приборы не "зависали"
        if (Math.abs(targetVelocity) < 0.5f) {
            isEngineRunning = false;
            targetVelocity = 0;
            lastStepValue = 0;
            return;
        }

        lastStepValue = targetVelocity * 0.22f; 
        if (Math.abs(lastStepValue) > 90) lastStepValue = Math.signum(lastStepValue) * 90;

        targetVelocity -= lastStepValue;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + lastStepValue);

        // 15мс - возвращаем мягкость v90 (было 8мс)
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 15);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gd) {
                    handler.postDelayed(() -> {
                        if (isEngineRunning) runStep(startX, startY);
                    }, 4); // Чуть увеличили паузу для стабильности
                }
                @Override public void onCancelled(GestureDescription gd) { 
                    isEngineRunning = false; 
                    targetVelocity = 0;
                    lastStepValue = 0;
                }
            }, null);
        } catch (Exception e) {
            isEngineRunning = false;
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
