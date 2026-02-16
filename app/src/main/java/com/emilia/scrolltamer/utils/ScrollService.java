package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float targetVelocity = 0;
    private static boolean isEngineRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d("ScrollTamer", "v77: ГЛОБАЛЬНЫЙ КОНТРОЛЬ ЗАПУЩЕН");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Мы уже видим скролл других приложений здесь.
        // В будущем мы сможем "помогать" им скроллиться плавнее.
    }

    // Пробуем поймать кнопки мыши или специфические сигналы прокрутки
    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        
        // Логируем нажатия, чтобы понять, пролетает ли тут мышь
        Log.d("ScrollTamer", "Key Event: " + keyCode + " Action: " + action);
        
        return super.onKeyEvent(event);
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;
        
        targetVelocity += (strength * 130); 

        if (!isEngineRunning) {
            isEngineRunning = true;
            // Центрируем скролл для стабильности во внешних приложениях
            instance.runStep(540, 1200); 
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 1.0f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        float step = targetVelocity * 0.2f;
        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + step);

        // Стабильный 40мс жест для совместимости с внешними окнами
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 40);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                handler.postDelayed(() -> runStep(startX, startY), 10);
            }
            @Override
            public void onCancelled(GestureDescription gd) { isEngineRunning = false; }
        }, null);
    }

    @Override public void onInterrupt() { instance = null; }
}
