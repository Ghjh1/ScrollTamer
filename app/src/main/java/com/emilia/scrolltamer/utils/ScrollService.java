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
    private static long lastPulseTime = 0;
    private static long lockUntil = 0; // Наш шлюз для тормоза
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        instance = this;
    }

    public static String getDebugData() {
        long wait = Math.max(0, lockUntil - System.currentTimeMillis());
        return String.format("V: %.1f | LOCK: %dms", velocity, wait);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        long now = System.currentTimeMillis();
        if (now < lockUntil) return; // Игнорируем инерцию пальца во время шлюза

        // ХИРУРГИЧЕСКИЙ ТОРМОЗ
        if (velocity != 0 && Math.signum(delta) != Math.signum(velocity)) {
            velocity = 0;
            lockUntil = now + 150; // Включаем шлюз на 150мс
            return;
        } 
        
        velocity += delta * 115;

        if (Math.abs(velocity) > 3200) velocity = Math.signum(velocity) * 3200;

        if (!active && Math.abs(velocity) > 0.5f) {
            active = true;
            instance.pulse(x, y);
        }
    }

    private void pulse(final float x, final float y) {
        long now = System.currentTimeMillis();
        
        // Если скорость обнулена тормозом или сама упала - стоп
        if (Math.abs(velocity) < 1.0f || now < lockUntil) {
            velocity = 0;
            active = false;
            return;
        }

        if (now - lastPulseTime < 22) {
            handler.postDelayed(() -> pulse(x, y), 5);
            return;
        }

        float step = velocity * 0.16f;
        if (Math.abs(step) > 170) step = Math.signum(step) * 170;
        velocity -= step;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + step);

        lastPulseTime = now;
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 18);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
            handler.postDelayed(() -> pulse(x, y), 20);
        } catch (Exception e) {
            active = false;
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
