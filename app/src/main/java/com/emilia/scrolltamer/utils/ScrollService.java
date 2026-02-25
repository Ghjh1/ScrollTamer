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

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        if (now < lockUntil) return;

        // Компенсация "вверх": если крутим вверх, чуть усиливаем импульс
        float directionFactor = (delta < 0) ? 1.15f : 1.0f;
        float input = delta * 55 * directionFactor;

        if (velocity != 0 && Math.signum(delta) != Math.signum(velocity)) {
            velocity = 0; active = false;
            lockUntil = now + (Math.abs(velocity) < 900 ? 75 : 130);
            instance.killQueue(x, y);
            return;
        } 

        if (Math.abs(velocity + input) < 250) {
            velocity = input; // Ручной режим "Прямые руки"
        } else {
            velocity += input;
        }

        if (!active && Math.abs(velocity) > 0.1f) {
            active = true;
            instance.pulse(x, y);
        }
    }

    private void pulse(final float x, final float y) {
        if (!active || Math.abs(velocity) < 0.5f) {
            velocity = 0; active = false; return;
        }

        float step = velocity * 0.12f;
        float sign = Math.signum(velocity);

        Path path = new Path();
        // ПЛАН "ПРЯМЫЕ РУКИ": Программный микро-зигзаг для взлома Slop
        // Начинаем чуть-чуть в обратную сторону (-2px), затем основной ход
        path.moveTo(x, y - (sign * 2)); 
        path.lineTo(x, y);
        path.lineTo(x, y + step + (sign * 6)); // Основной шаг + микро-пробой

        // Удлиняем время контакта, чтобы система "залипла" на движении
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 25);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
            velocity -= (step * 0.9f);
            handler.postDelayed(() -> { if (active) pulse(x, y); }, 25);
        } catch (Exception e) { active = false; }
    }

    private void killQueue(float x, float y) {
        Path p = new Path(); p.moveTo(x, y); p.lineTo(x, y + 1);
        dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p, 0, 10)).build(), null, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
