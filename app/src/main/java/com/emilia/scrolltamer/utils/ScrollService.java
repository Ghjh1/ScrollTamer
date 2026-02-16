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
        Log.d("ScrollTamer", "v87: Режим Гиперпрыжка");
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;

        // РЕЗКИЙ ТОРМОЗ: Если крутим назад, гасим скорость мгновенно
        if (Math.signum(strength) != Math.signum(targetVelocity) && targetVelocity != 0) {
            targetVelocity = 0; 
            // Даем микро-паузу для смены вектора
        }
        
        // ПРОГРЕССИЯ: Чем выше текущая скорость, тем сильнее добавляем
        float multiplier = 1.0f + (Math.abs(targetVelocity) / 500f);
        targetVelocity += (strength * 110 * multiplier); 

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

        // Вязкость затухания (0.12) - летим еще дольше и мягче
        float step = targetVelocity * 0.12f; 
        
        // Лимитируем шаг для "короткого хода" на старте
        if (Math.abs(targetVelocity) < 200 && Math.abs(step) > 40) {
            step = Math.signum(step) * 40;
        }

        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + step);

        // 12мс - баланс между четкостью v86 и мягкостью v85
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 12);
        
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gd) {
                handler.postDelayed(() -> runStep(startX, startY), 8);
            }
            @Override public void onCancelled(GestureDescription gd) { 
                isEngineRunning = false; 
            }
        }, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
