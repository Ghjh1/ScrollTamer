package com.emilia.scrolltamer.utils;                                           
import android.accessibilityservice.AccessibilityService;                       import android.accessibilityservice.GestureDescription;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private final BroadcastReceiver scrollReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float strength = intent.getFloatExtra("direction", 0);
            float step = strength * 20;

            Path p = new Path();
            p.moveTo(500, 1000); // Используем фиксированную точку для стабильности
            p.lineTo(500, 1000 + step);

            try {
                GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 50);
                dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
            } catch (Exception e) {
                Log.e("ScrollTamer", "Ошибка жеста: " + e.getMessage());
            }
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        try {
            registerReceiver(scrollReceiver, new IntentFilter("com.emilia.scrolltamer.SCROLL_ACTION"));
            Log.d("ScrollTamer", "Сервис готов");
        } catch (Exception e) {
            Log.e("ScrollTamer", "Ошибка регистрации: " + e.getMessage());
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(scrollReceiver); } catch (Exception e) {}
    }
}
