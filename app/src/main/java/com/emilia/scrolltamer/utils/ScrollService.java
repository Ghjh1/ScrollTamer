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
    private static float currentY = 1000;
    private static float targetVelocity = 0;
    private static final Handler engineHandler = new Handler(Looper.getMainLooper());
    private static boolean isEngineRunning = false;                             
    @Override
    protected void onServiceConnected() {                                               super.onServiceConnected();
        instance = this;
        Log.d("ScrollTamer", "v66: Двигатель 'Silk' готов");
    }

    public static void scroll(float strength, float rawX, float rawY) {
        if (instance == null) return;

        // Накапливаем импульс (скорость)
        targetVelocity += (strength * -80);

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runEngine(rawX, rawY);
        }
    }

    private void runEngine(float x, float y) {
        if (Math.abs(targetVelocity) < 0.5f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        // Вычисляем шаг для текущего "кадра"
        float step = targetVelocity * 0.2f;
        targetVelocity -= step; // Затухание (трение)

        Path p = new Path();                                                            p.moveTo(x, y);
        p.lineTo(x, y + step);                                                  
        // Очень короткий жест (буквально на 1 кадр)
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 20);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                // Перезапуск через минимальное время
                engineHandler.postDelayed(() -> runEngine(x, y), 5);
            }
        }, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() { instance = null; super.onDestroy(); }
}
