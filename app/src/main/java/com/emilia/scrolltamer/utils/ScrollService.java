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
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        instance = this;
    }

    public static String getDebugData() {
        return String.format("V: %.1f | %s", velocity, active ? "RUNNING" : "IDLE");
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;

        // Честный тормоз: смена направления = стоп
        if (velocity != 0 && Math.signum(delta) != Math.signum(velocity)) {
            velocity = 0;
        } else {
            velocity += delta * 115; // Возвращаем силу из v95
        }

        // Ограничиваем, чтобы не "выбивало" систему
        if (Math.abs(velocity) > 3200) velocity = Math.signum(velocity) * 3200;

        if (!active && Math.abs(velocity) > 0.5f) {
            active = true;
            instance.pulse(x, y);
        }
    }

    private void pulse(final float x, final float y) {
        long now = System.currentTimeMillis();
        
        // Глушим, если скорость упала
        if (Math.abs(velocity) < 1.0f) {
            velocity = 0;
            active = false;
            return;
        }

        // Защита от наслоения (минимум 22мс между началами жестов)
        if (now - lastPulseTime < 22) {
            handler.postDelayed(() -> pulse(x, y), 5);
            return;
        }

        float step = velocity * 0.16f; // Коэффициент из v95
        if (Math.abs(step) > 170) step = Math.signum(step) * 170;
        velocity -= step;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + step);

        lastPulseTime = now;
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 18);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
            // Следующий такт через короткую паузу
            handler.postDelayed(() -> pulse(x, y), 20);
        } catch (Exception e) {
            active = false;
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
