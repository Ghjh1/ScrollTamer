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

        // ЖЕСТКИЙ ЯКОРЬ: Если крутим назад — мгновенная блокировка всего
        if (Math.signum(strength) != Math.signum(targetVelocity) && Math.abs(targetVelocity) > 1) {
            targetVelocity = 0;
            isEngineRunning = false;
            // Мы не запускаем новый жест, пока не остановим старый
            return; 
        }
        
        // Уменьшаем базовый импульс (было 130, стало 75) для короткого хода
        targetVelocity += (strength * 75); 

        // Ограничитель, чтобы "не улетать"
        if (Math.abs(targetVelocity) > 1800) targetVelocity = Math.signum(targetVelocity) * 1800;

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 0.3f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        // Делаем затухание более резким (0.25 вместо 0.18), чтобы список не "плыл" лишнего
        lastStepValue = targetVelocity * 0.25f; 
        
        // Лимитируем физический размер одного "шага" пальца
        if (Math.abs(lastStepValue) > 80) lastStepValue = Math.signum(lastStepValue) * 80;

        targetVelocity -= lastStepValue;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + lastStepValue);

        // Укорачиваем контакт до 8мс — это почти мгновенный тычок
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 8);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gd) {
                    // Пауза всего 2мс — максимально плотный поток
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
