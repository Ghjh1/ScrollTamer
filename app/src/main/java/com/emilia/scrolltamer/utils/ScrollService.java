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
        return String.format("V: %.1f | PIXEL_MODE", velocity);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        if (now < lockUntil) return;

        // В ручном режиме (малые скорости) работаем через реверс
        float input = delta * 45; // Чуть снизили базу для контроля
        
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
        if (!active || Math.abs(velocity) < 0.4f) {
            velocity = 0; active = false; return;
        }

        float sign = Math.signum(velocity);
        // Наш заветный 1 пиксель чистого смещения
        float targetStep = sign * 1.5f; 
        
        // РЕВЕРСИВНЫЙ МАХ: 
        // 1. Откатываемся на 10px назад (пробой защиты)
        // 2. Возвращаемся и проезжаем на targetStep вперед
        float slopBypass = sign * 10.0f;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y - slopBypass); // Рывок назад
        path.lineTo(x, y + targetStep); // Мягкий выход в +1.5 пикселя

        // Общее время жеста 25мс, чтобы Redmi успевал отрисовать
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 25);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
            
            // Расход скорости: в ручном режиме гасим почти всё сразу для пошаговости
            velocity -= (velocity * 0.9f); 
            
            // Пауза между "качелями", чтобы не перегреть графический чип
            handler.postDelayed(() -> { if (active) pulse(x, y); }, 30);
        } catch (Exception e) { active = false; }
    }

    private void killQueue(float x, float y) {
        Path p = new Path(); p.moveTo(x, y); p.lineTo(x, y + 1);
        dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p, 0, 10)).build(), null, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
