package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float velocity = 0;
    private static boolean active = false;
    private static int boostCycles = 0; // Счетчик тактов "пробоя"
    private static long lockUntil = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("V: %.1f | LOCK: %d", velocity, Math.max(0, lockUntil - System.currentTimeMillis()));
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        if (now < lockUntil) return;

        if (velocity != 0 && Math.signum(delta) != Math.signum(velocity)) {
            int brakeDuration = (Math.abs(velocity) < 900) ? 75 : 130;
            velocity = 0; active = false;
            lockUntil = now + brakeDuration;
            instance.killQueue(x, y);
            return;
        } 
        
        // Сила v112
        velocity += delta * 55;
        if (Math.abs(velocity) > 3500) velocity = Math.signum(velocity) * 3500;

        if (!active && Math.abs(velocity) > 0.1f) {
            active = true;
            boostCycles = 2; // Заряжаем пробой на 2 такта
            instance.pulse(x, y);
        }
    }

    private void killQueue(float x, float y) {
        Path p = new Path();
        p.moveTo(x, y);
        p.lineTo(x, y + 1);
        dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p, 0, 10)).build(), null, null);
    }

    private void pulse(final float x, final float y) {
        if (!active || Math.abs(velocity) < 0.5f) {
            velocity = 0; active = false; return;
        }

        // Затухание v112
        float step = velocity * 0.12f;
        
        // ТОНКИЙ ПРОБОЙ
        if (boostCycles > 0) {
            // Добавляем всего 12 пикселей - это "золотое сечение" для Touch Slop
            step += (Math.signum(velocity) * 12);
            boostCycles--;
        }

        if (Math.abs(step) > 175) step = Math.signum(step) * 175;
        
        // Уменьшаем "штраф" к скорости, чтобы второй шаг не проваливался
        velocity -= (step * 0.7f); 

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + step);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 18);
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
            handler.postDelayed(() -> { if (active) pulse(x, y); }, 22);
        } catch (Exception e) { active = false; }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
