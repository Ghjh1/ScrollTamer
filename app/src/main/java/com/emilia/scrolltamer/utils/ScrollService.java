package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;                         import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;                                          private static float targetVelocity = 0;
    private static boolean isEngineRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }
                                                                                    public static void scroll(float strength, float x, float y) {
        // Возвращаем мощный импульс. 250 - это уже серьезно.
        targetVelocity += (strength * 250);

        if (instance != null && !isEngineRunning) {
            isEngineRunning = true;
            // Бьем в центр экрана, чтобы никакие кнопки не мешали
            instance.runStep(500, 1000);                                                }
    }                                                                           
    private void runStep(final float startX, final float startY) {
        // Уменьшаем порог остановки до 0.1, чтобы список "доползал" до конца
        if (Math.abs(targetVelocity) < 0.1f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        // Коэффициент 0.1 дает ОЧЕНЬ долгое и плавное затухание                        float step = targetVelocity * 0.1f;
        targetVelocity -= step;                                                 
        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + step);

        // Ультра-быстрый жест (10мс) - это предел для Android
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 10);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {                                                               @Override
            public void onCompleted(GestureDescription gestureDescription) {                    // Пауза 1мс - практически мгновенный перезапуск
                handler.postDelayed(() -> runStep(startX, startY), 1);
            }
            @Override
            public void onCancelled(GestureDescription gd) {
                isEngineRunning = false;
            }
        }, null);
    }                                                                           
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() { instance = null; super.onDestroy(); }
}
