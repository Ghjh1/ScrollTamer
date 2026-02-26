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
    protected void onServiceConnected() { instance = this; }

    public static String getDebugData() {
        return String.format("V: %.1f | PIXEL_LOCK", velocity);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        
        // В ручном режиме (малые скорости) один щелчок = один чистый импульс
        float input = delta * 30; // Еще меньше база для контроля
        velocity = input; 

        if (!active && Math.abs(velocity) > 0.1f) {
            active = true;
            instance.pulse(x, y);
        }
    }

    private void pulse(final float x, final float y) {
        if (!active) return;

        float sign = Math.signum(velocity);
        float slopBypass = sign * 14.0f; // Увеличим "виртуальный" рывок для пробоя
        float realStep = sign * 1.0f;    // ТОТ САМЫЙ ОДИН ПИКСЕЛЬ

        Path path = new Path();
        // Взламываем систему: 
        // 1. Старт
        path.moveTo(x, y);
        // 2. Мгновенная точка пробоя (она не должна отрисоваться как движение)
        path.lineTo(x, y - slopBypass);
        // 3. Целевая точка: всего +1 пиксель от старта
        path.lineTo(x, y + realStep);

        // Ставим короткое время (15мс), чтобы Android "проглотил" это как один чих
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 15);
        
        try {
            dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
            velocity = 0;
            active = false; 
        } catch (Exception e) { active = false; }
    }

    private void killQueue(float x, float y) {
        Path p = new Path(); p.moveTo(x, y); p.lineTo(x, y + 1);
        dispatchGesture(new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(p, 0, 10)).build(), null, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
