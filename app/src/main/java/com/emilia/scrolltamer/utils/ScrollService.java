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
        Log.d("ScrollTamer", "v84: Режим плотного зацепа");
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;

        // Резкая смена направления
        if (Math.signum(strength) != Math.signum(targetVelocity) && targetVelocity != 0) {
            targetVelocity = 0; 
        }
        
        targetVelocity += (strength * 200); 

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

        // Делаем шаг более весомым
        float step = targetVelocity * 0.3f; 
        if (Math.abs(step) > 120) step = Math.signum(step) * 120;
        
        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(startX, startY);
        // Небольшой "занос" для имитации вязкости
        p.lineTo(startX, startY + step);

        // Увеличиваем длительность жеста до 50мс - это и есть "удержание"
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 50);
        
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gd) {
                // Прямой запуск следующего шага без задержки
                runStep(startX, startY);
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
