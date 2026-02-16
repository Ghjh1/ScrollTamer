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
        Log.d("ScrollTamer", "v89: Шлифовка Алмаза");
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;

        // ИДЕАЛЬНЫЙ ТОРМОЗ: Если сменили направление - сбрасываем всё в ноль перед новым толчком
        if (Math.signum(strength) != Math.signum(targetVelocity) && targetVelocity != 0) {
            targetVelocity = 0; 
        }
        
        // Добавляем импульс (увеличили вес одного клика до 120 для уверенности)
        targetVelocity += (strength * 120); 

        // Лимит скорости для безопасности
        if (Math.abs(targetVelocity) > 2200) targetVelocity = Math.signum(targetVelocity) * 2200;

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 0.5f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        // Плавное затухание (0.18)
        float step = targetVelocity * 0.18f; 
        if (Math.abs(step) > 130) step = Math.signum(step) * 130;

        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + step);

        // Ультра-короткий жест (10мс) для максимальной частоты опроса
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 10);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gd) {
                    // Минимальная пауза (5мс), чтобы не терять быстрые клики
                    handler.postDelayed(() -> runStep(startX, startY), 5);
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
