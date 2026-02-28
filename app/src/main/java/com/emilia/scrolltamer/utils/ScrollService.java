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
        return String.format("V: %.1f | STATUS: %s", velocity, (active ? "RUNNING" : "IDLE"));
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        if (now < lockUntil) return;

        // Резкий тормоз (v112)
        if (velocity != 0 && Math.signum(delta) != Math.signum(velocity)) {
            velocity = 0; active = false; lockUntil = now + 100;
            instance.killQueue(x, y); return;
        }

        // МАГИЯ СТАРТА: Если стоим, даем пинок сразу на 160 (это ~19px сдвига)
        if (!active) {
            velocity = Math.signum(delta) * 160f;
            active = true;
            instance.pulse(x, y);
        } else {
            // Если уже едем — просто добавляем веса (как в v112)
            velocity += delta * 65;
        }
        
        if (Math.abs(velocity) > 3500) velocity = Math.signum(velocity) * 3500;
    }

    private void killQueue(float x, float y) {
        Path p = new Path(); p.moveTo(x, y); p.lineTo(x, y + 1);
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 10);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
    }

    private void pulse(final float x, final float y) {
        // Если скорость упала ниже порога видимости — стоп
        if (!active || Math.abs(velocity) < 1.0f) {
            velocity = 0; active = false; return;
        }

        // Математика затухания v112 (0.12f)
        float step = velocity * 0.12f;
        
        // Ограничиваем максимальный рывок, чтобы не улетать
        if (Math.abs(step) > 180) step = Math.signum(step) * 180;
        
        velocity -= step;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + step);

        // T=38 — наш "Шёлковый" стандарт для плавности и тормозов
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 38);

        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
            // Частота обновления пульса (22мс)
            handler.postDelayed(() -> { if (active) pulse(x, y); }, 22);
        } catch (Exception e) { active = false; }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
