package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.BroadcastReceiver;                                       import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static final String TAG = "ScrollTamer";

    private final BroadcastReceiver scrollReceiver = new BroadcastReceiver() {          @Override
        public void onReceive(Context context, Intent intent) {                             float direction = intent.getFloatExtra("direction", 0);
            float x = intent.getFloatExtra("x", 500);
            float y = intent.getFloatExtra("y", 1000);

            Log.d(TAG, "СЕРВИС: Принял сигнал! Сила: " + direction + " в точке: " + x + "," + y);

            // Запускаем наш шелковый жест в месте нахождения курсора
            executeSilkScroll(x, y, direction);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // Подписываемся на "СМС" от нашего приложения
        IntentFilter filter = new IntentFilter("com.emilia.scrolltamer.SCROLL_ACTION");
        registerReceiver(scrollReceiver, filter, Context.RECEIVER_EXPORTED);
        Log.d(TAG, "СЕРВИС: Готов ловить колесико!");
    }                                                                           
    private void executeSilkScroll(float x, float y, float strength) {                  Path p = new Path();
        p.moveTo(x, y);
        // Дистанция зависит от силы вращения (умножаем на 150 для наглядности)
        float endY = y + (strength * 150);
        p.lineTo(x, endY);

        // Время жеста: 300мс для быстрого отклика
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 300);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), null, null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override
    public void onInterrupt() {}
    @Override
    public void onDestroy() {                                                           super.onDestroy();
        unregisterReceiver(scrollReceiver);
    }
}
