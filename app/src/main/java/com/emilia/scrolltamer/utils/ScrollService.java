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

            // Шаг в 30 пикселей — это должно "разбудить" скролл
            float step = (direction < 0) ? -30 : 30;
            executeStepScroll(x, y, step);
        }
    };

    @Override                                                                       protected void onServiceConnected() {
        super.onServiceConnected();                                                     registerReceiver(scrollReceiver, new IntentFilter("com.emilia.scrolltamer.SCROLL_ACTION"));
    }

    private void executeStepScroll(float x, float y, float step) {
        Path p = new Path();
        p.moveTo(x, y);
        p.lineTo(x, y + step);

        // 100мс — это достаточно быстро для импульса, но плавно для глаза
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 100);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
        Log.d("ScrollTamer", "Шаг: " + step + " пикселей");
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(scrollReceiver); } catch (Exception e) {}
    }
}
