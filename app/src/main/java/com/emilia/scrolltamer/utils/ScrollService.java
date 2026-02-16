package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float x = 500;
    private static float y = 1000;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d("ScrollTamer", "v65: Двигатель запущен");
    }

    public static void scroll(float strength, float rawX, float rawY) {
        if (instance == null) return;
        
        x = rawX;
        y = rawY;

        // Увеличим множитель, чтобы гарантированно пробить "порог трения"
        float totalStep = strength * 60; 
        
        Log.d("ScrollTamer", "v65: Скролл в точке " + x + ":" + y + " сила " + totalStep);
        
        Path p = new Path();
        p.moveTo(x, y);
        p.lineTo(x, y + totalStep);

        // Растягиваем движение на 200мс для мягкости
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 200);
        instance.dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() { instance = null; super.onDestroy(); }
}
