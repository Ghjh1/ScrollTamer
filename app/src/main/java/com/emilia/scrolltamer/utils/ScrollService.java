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
    private static long lockUntil = 0;
    private static int pincetStep = 0;
    
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("V: %.1f | %s", velocity, (active ? "FLYHEEL" : "PINCET"));
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        if (now < lockUntil) return;

        // Резкий тормоз при смене направления
        if (velocity != 0 && Math.signum(delta) != Math.signum(velocity)) {
            velocity = 0;
            active = false;
            lockUntil = now + 100;
            instance.killQueue(x, y);
            return;
        }

        // Энергия копится ВСЕГДА
        velocity += delta * 60; // Чуть добавили веса щелчку (55 -> 60)
        if (Math.abs(velocity) > 3500) velocity = Math.signum(velocity) * 3500;

        if (Math.abs(velocity) < 180) { // Порог чуть выше для Пинцета
            if (!active) {
                instance.pincetStroke(delta, x, y);
            }
        } else {
            // Переход в маховик
            if (!active) {
                active = true;
                instance.pulse(x, y);
            }
        }
    }

    private void pincetStroke(float delta, float x, float y) {
        float direction = Math.signum(delta);
        int[] steps = {16, 22, 28}; // Упростили и усилили шаг
        int d = steps[pincetStep % steps.length];
        pincetStep++;

        Path p = new Path();
        p.moveTo(x, y);
        p.lineTo(x, y + (d * direction));
        
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 40);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
        
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> {
            pincetStep = 0;
            // Если маховик не запущен, потихоньку гасим скорость
            if (!active) velocity *= 0.5f; 
        }, 250);
    }

    private void killQueue(float x, float y) {
        Path p = new Path();
        p.moveTo(x, y);
        p.lineTo(x, y + 1);
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 10);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
    }

    private void pulse(final float x, final float y) {
        if (!active || Math.abs(velocity) < 1.0f) {
            velocity = 0;
            active = false;
            pincetStep = 0;
            return;
        }

        float step = velocity * 0.12f;
        if (Math.abs(step) > 175) step = Math.signum(step) * 175;
        velocity -= step;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + step);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 38);

        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
            handler.postDelayed(() -> { if (active) pulse(x, y); }, 22);
        } catch (Exception e) { active = false; }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
