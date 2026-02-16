package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static final String TAG = "ScrollTamer";                            
    private final BroadcastReceiver scrollReceiver = new BroadcastReceiver() {
        @Override                                                                       public void onReceive(Context context, Intent intent) {
            float strength = intent.getFloatExtra("direction", 0);
            float x = intent.getFloatExtra("x", 500);
            float y = intent.getFloatExtra("y", 1000);

            // Прямой микро-скролл без задержек
            // 15 пикселей — это "золотая середина", чтобы система заметила
            float step = strength * 15;                                         
            Log.d(TAG, "СЕРВИС: Делаю микро-скролл " + step + " px");

            Path p = new Path();
            p.moveTo(x, y);
            p.lineTo(x, y + step);

            GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 40);
            dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // Используем старый добрый фильтр
        IntentFilter filter = new IntentFilter("com.emilia.scrolltamer.SCROLL_ACTION");
        registerReceiver(scrollReceiver, filter);
        Log.d(TAG, "СЕРВИС: Подключен и готов к микро-шагам");
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(scrollReceiver); } catch (Exception e) {}
    }
}
