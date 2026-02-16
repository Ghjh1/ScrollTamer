package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d("ScrollTamer", "СЕРВИС: Подключен напрямую");
    }

    // Статический метод, который можно вызвать из любой части приложения
    public static void scroll(float strength) {
        if (instance == null) {
            Log.e("ScrollTamer", "СЕРВИС: Еще не запущен в Спец. возможностях!");
            return;
        }
        
        float step = strength * 20; 
        Log.d("ScrollTamer", "СЕРВИС: Получен прямой вызов! Шаг: " + step);
        
        Path p = new Path();
        p.moveTo(500, 1000);
        p.lineTo(500, 1000 + step);

        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 50);
        instance.dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() { instance = null; super.onDestroy(); }
}
