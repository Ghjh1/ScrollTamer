package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;

public class ScrollService extends AccessibilityService {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            startMultiStepScroll(1200, 15); // Дистанция 1200, 15 кадров замедления
        }
    }

    private void startMultiStepScroll(int totalDistance, int steps) {
        int currentY = 1500;
        int remainingDistance = totalDistance;

        for (int i = 0; i < steps; i++) {
            final int stepIndex = i;
            // Формула: чем дальше шаг, тем меньше дистанция (имитация трения)
            final int stepDistance = (totalDistance / steps) * (steps - stepIndex) / steps * 2;
            final int startY = currentY;
            final int endY = startY - stepDistance;
            currentY = endY;

            handler.postDelayed(() -> {
                sendMicroGesture(startY, endY, 60); // Каждый "кадр" длится 60мс
            }, i * 65); // Запускаем цепочку с микро-паузой
        }
    }

    private void sendMicroGesture(int startY, int endY, int duration) {
        Path p = new Path();
        p.moveTo(500, startY);
        p.lineTo(500, endY);
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, duration);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
    }

    @Override
    public void onInterrupt() {}
}
