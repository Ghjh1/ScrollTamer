package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;                       import android.accessibilityservice.GestureDescription;
import android.content.BroadcastReceiver;                                       import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private final BroadcastReceiver scrollReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float direction = intent.getFloatExtra("direction", 0);
            float x = intent.getFloatExtra("x", 500);
            float y = intent.getFloatExtra("y", 1000);

            // Направление колеса: если отрицательное (на себя), двигаем на 1 пиксель вверх
            float pixelStep = (direction < 0) ? -1 : 1;
            executePixelScroll(x, y, pixelStep);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        registerReceiver(scrollReceiver, new IntentFilter("com.emilia.scrolltamer.SCROLL_ACTION"));
    }                                                                           
    private void executePixelScroll(float x, float y, float step) {
        Path p = new Path();
        p.moveTo(x, y);
        p.lineTo(x, y + step); // Сдвиг ровно на 1 пиксель

        // Минимальное время, чтобы система успела зафиксировать жест (50-100мс)
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 80);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
        Log.d("ScrollTamer", "Микро-шаг: " + step + " пиксель");
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(scrollReceiver); } catch (Exception e) {}          }
}
