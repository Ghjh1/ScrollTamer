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
        return String.format("V: %.1f | %s", velocity, active ? "FLOW" : "IDLE");
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        if (now < lockUntil) return;

        // Коррекция направлений (Redmi любит чуть больше силы вверх)
        float directionFactor = (delta < 0) ? 1.25f : 1.0f;
        float input = delta * 55 * directionFactor;

        // Тормоз
        if (velocity != 0 && Math.signum(delta) != Math.signum(velocity)) {
            velocity = 0; active = false;
            lockUntil = now + (Math.abs(velocity) < 900 ? 75 : 130);
            instance.killQueue(x, y);
            return;
        } 

        // Стабилизация: в ручном режиме просто держим входную скорость
        if (Math.abs(input) < 150) {
            velocity = input; 
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

        float sign = Math.signum(velocity);
        float step = velocity * 0.12f;

        // Прямой пробой без зигзага (бережем Redmi)
        // Мы просто делаем жест чуть длиннее самого смещения
        float visualStep = step;
        float internalPush = sign * 9; // Фиксированный пробой порога

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + visualStep + internalPush);

        // Длительность 10мс - это "выстрел", система не успеет распознать это как дрожание
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 12);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
            velocity -= (step * 0.9f);
            handler.postDelayed(() -> { if (active) pulse(x, y); }, 22);
        } catch (Exception e) { active = false; }
    }

    private void killQueue(float x, float y) {
        Path p = new Path(); p.moveTo(x, y); p.lineTo(x, y + 1);
        dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p, 0, 10)).build(), null, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
