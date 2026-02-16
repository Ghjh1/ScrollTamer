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
        Log.d("ScrollTamer", "v81: Мастер Шёлка оживает...");
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;
        
        // Увеличиваем импульс для гарантированного сдвига
        targetVelocity += (strength * 300); 

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        // Снижаем порог до минимума
        if (Math.abs(targetVelocity) < 0.1f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        float step = targetVelocity * 0.15f; 
        targetVelocity -= step;

        Log.d("ScrollTamer", "Шаг: " + step + " по координатам: " + startX + "," + startY);

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + step);

        // 20мс - баланс между скоростью и пониманием системы
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 20);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                handler.postDelayed(() -> runStep(startX, startY), 1);
            }
            @Override public void onCancelled(GestureDescription gd) { 
                Log.d("ScrollTamer", "Жест отменен системой!");
                isEngineRunning = false; 
            }
        }, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
