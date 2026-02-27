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
    private static long lastEventTime = 0;
    private static int pincetStep = 0; // Счетчик шагов пинцета
    
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("V: %.1f | MODE: %s", velocity, (active ? "FLYHEEL" : "PINCET"));
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        if (now < lockUntil) return;

        long interval = now - lastEventTime;
        lastEventTime = now;

        // ПИНЦЕТ: если крутим медленно (> 150ms)
        if (interval > 150 && !active) {
            instance.pincetStroke(delta, x, y);
            return; 
        }

        // ДВИЖОК 112 (ТОРМОЗ)
        if (velocity != 0 && Math.signum(delta) != Math.signum(velocity)) {
            int brakeDuration = (Math.abs(velocity) < 900) ? 75 : 130;
            velocity = 0;
            active = false;
            lockUntil = now + brakeDuration;
            instance.killQueue(x, y);
            return;
        }

        velocity += delta * 55;
        if (Math.abs(velocity) > 3500) velocity = Math.signum(velocity) * 3500;

        if (!active && Math.abs(velocity) > 0.5f) {
            active = true;
            instance.pulse(x, y);
        }
    }

    private void pincetStroke(float delta, float x, float y) {
        float direction = Math.signum(delta);
        // Прогрессия пинцета: 16 -> 20 -> 24 -> 30
        int[] steps = {16, 20, 24, 30};
        int d = steps[pincetStep];
        pincetStep = (pincetStep + 1) % steps.length;

        Path p = new Path();
        p.moveTo(x, y);
        p.lineTo(x, y + (d * direction));
        
        // T=40 для пинцета - никакой инерции
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 40);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
        
        // Сброс шага пинцета, если долго не крутили
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> pincetStep = 0, 500);
    }

    private void killQueue(float x, float y) {
        Path p = new Path();
        p.moveTo(x, y);
        p.lineTo(x, y + 1);
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 10);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
    }

    private void pulse(final float x, final float y) {
        if (!active || Math.abs(velocity) < 0.8f) {
            velocity = 0;
            active = false;
            return;
        }

        float step = velocity * 0.12f;
        if (Math.abs(step) > 175) step = Math.signum(step) * 175;
        velocity -= step;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + step);

        // Магия 112 + T38 для тормозов
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 38);

        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
            handler.postDelayed(() -> { if (active) pulse(x, y); }, 22);
        } catch (Exception e) { active = false; }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
