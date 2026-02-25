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

        // ТОРМОЗ (База v111)
        if (velocity != 0 && Math.signum(delta) != Math.signum(velocity)) {
            int brakeDuration = (Math.abs(velocity) < 900) ? 75 : 130;
            velocity = 0;
            active = false;
            lockUntil = now + brakeDuration;
            instance.killQueue(x, y);
            return;
        } 
        
        // СИЛА ЩЕЛЧКА: Подняли до 75 (баланс между точностью и стартом)
        velocity += delta * 75;
        if (Math.abs(velocity) > 3500) velocity = Math.signum(velocity) * 3500;

        // СТАРТ: Ультра-низкий порог 0.1, чтобы запускалось всегда
        if (!active && Math.abs(velocity) > 0.1f) {
            active = true;
            instance.pulse(x, y);
        }
    }

    private void killQueue(float x, float y) {
        Path p = new Path();
        p.moveTo(x, y);
        p.lineTo(x, y + 1);
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 10);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
    }

    private void pulse(final float x, final float y) {
        // ОСТАНОВКА: Порог 0.5, чтобы не дергалось в конце
        if (!active || Math.abs(velocity) < 0.5f) {
            velocity = 0;
            active = false;
            return;
        }

        // ЗАТУХАНИЕ: 0.10 - для максимально долгого и плавного "вылета"
        float step = velocity * 0.10f;
        if (Math.abs(step) > 175) step = Math.signum(step) * 175;
        velocity -= step;

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
