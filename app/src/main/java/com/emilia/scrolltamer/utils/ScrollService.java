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
        return String.format("V: %.1f | DUAL_KICK", velocity);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        if (now < lockUntil) return;

        float input = delta * 50;
        if (Math.abs(input) < 150) { velocity = input; } 
        else { velocity += input; }

        if (!active && Math.abs(velocity) > 0.1f) {
            active = true;
            instance.pulse(x, y);
        }
    }

    private void pulse(final float x, final float y) {
        if (!active || Math.abs(velocity) < 0.4f) {
            velocity = 0; active = false; return;
        }

        float sign = Math.signum(velocity);
        float bypass = sign * 12.0f; // Увеличили пробой для верности
        float targetStep = sign * 1.5f;

        // ЖЕСТ 1: Взлом (Назад)
        Path p1 = new Path();
        p1.moveTo(x, y);
        p1.lineTo(x, y - bypass);
        GestureDescription.StrokeDescription stroke1 = new GestureDescription.StrokeDescription(p1, 0, 10);

        // ЖЕСТ 2: Движение (Вперед + Микро-шаг)
        Path p2 = new Path();
        p2.moveTo(x, y - bypass);
        p2.lineTo(x, y + targetStep);
        GestureDescription.StrokeDescription stroke2 = new GestureDescription.StrokeDescription(p2, 11, 15); // Стартует сразу после первого

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(stroke1);
        builder.addStroke(stroke2);
        
        try {
            dispatchGesture(builder.build(), null, null);
            velocity -= (velocity * 0.9f);
            handler.postDelayed(() -> { if (active) pulse(x, y); }, 35);
        } catch (Exception e) { active = false; }
    }

    private void killQueue(float x, float y) {
        Path p = new Path(); p.moveTo(x, y); p.lineTo(x, y + 1);
        dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p, 0, 10)).build(), null, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
