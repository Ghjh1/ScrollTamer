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
        return String.format("V: %.1f | %s", velocity, (active ? "FLYING" : "READY"));
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        if (now < lockUntil) return;

        // ТОРМОЗА 112-й: Смена направления — полная остановка
        if (velocity != 0 && Math.signum(delta) != Math.signum(velocity)) {
            velocity = 0; active = false; lockUntil = now + 110;
            instance.stopMovement(x, y); return;
        }

        // СТАРТ С ОТКРЫТИЕМ ЗАМКА (18px)
        if (!active) {
            // Чтобы при затухании 0.12 первый шаг был 18px, ставим V = 150
            velocity = Math.signum(delta) * 150f;
            active = true;
            instance.pulse(x, y);
        } else {
            // Если уже летим — добавляем "веса" щелчку
            velocity += delta * 65;
        }
        
        if (Math.abs(velocity) > 3500) velocity = Math.signum(velocity) * 3500;
    }

    private void stopMovement(float x, float y) {
        Path p = new Path(); p.moveTo(x, y); p.lineTo(x, y + 1);
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 10);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
    }

    private void pulse(final float x, final float y) {
        if (!active || Math.abs(velocity) < 1.0f) {
            velocity = 0; active = false; return;
        }

        // Математика затухания 112-й (0.12f)
        float step = velocity * 0.12f;
        
        // Ограничиваем рывок, чтобы не было "прыжков"
        if (Math.abs(step) > 170) step = Math.signum(step) * 170;
        
        velocity -= step;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (float)Math.floor(step)); // Только целые числа для Redmi

        // Твой золотой порог T=39 (Контролируемый флинг)
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 39);

        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
            // Интервал 22мс — проверенная классика 112-й
            handler.postDelayed(() -> { if (active) pulse(x, y); }, 22);
        } catch (Exception e) { active = false; }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
