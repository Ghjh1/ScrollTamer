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
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        instance = this;
    }

    public static String getDebugData() {
        return String.format("V: %.1f | A: %s", velocity, active ? "RUN" : "IDLE");
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        // ЛОГИКА СИЛЫ: Если знаки разные - это сопротивление (тормоз)
        // Если знаки одинаковые - это разгон
        if (velocity != 0 && Math.signum(delta) != Math.signum(velocity)) {
            velocity = 0; // Мгновенный стоп при смене направления
        } else {
            velocity += delta * 120; // Простой линейный подхват
        }

        if (Math.abs(velocity) > 3000) velocity = Math.signum(velocity) * 3000;

        if (!active && Math.abs(velocity) > 0) {
            active = true;
            instance.pulse(x, y);
        }
    }

    private void pulse(final float x, final float y) {
        if (Math.abs(velocity) < 1.0f) {
            velocity = 0;
            active = false;
            return;
        }

        // Вычисляем шаг
        float step = velocity * 0.15f;
        velocity -= step;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + step);

        // Самый быстрый и стабильный жест (20мс)
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 20);
        
        dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);

        // Мы не ждем колбэка! Мы просто бьем ритм каждые 25мс.
        handler.postDelayed(() -> pulse(x, y), 25);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
