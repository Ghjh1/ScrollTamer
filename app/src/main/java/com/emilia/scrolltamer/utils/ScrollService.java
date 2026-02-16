package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;                           import android.os.Handler;
import android.os.Looper;                                                       import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static final String TAG = "ScrollTamer";
    private float pendingDistance = 0;
    private boolean isRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private final BroadcastReceiver scrollReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float strength = intent.getFloatExtra("direction", 0);
            pendingDistance += (strength * -100); // Увеличим порцию топлива
            Log.d(TAG, "Сигнал получен! В баке: " + pendingDistance);

            if (!isRunning) {
                Log.d(TAG, "Запускаю двигатель...");
                startScrolling();
            }
        }
    };

    private void startScrolling() {
        if (Math.abs(pendingDistance) < 5) {
            Log.d(TAG, "Топливо кончилось, стоп.");
            isRunning = false;                                                              pendingDistance = 0;
            return;                                                                     }

        isRunning = true;
        float step = (pendingDistance > 0) ? 40 : -40; // Фиксированный шаг для теста
        pendingDistance -= step;

        Path p = new Path();
        p.moveTo(500, 800);
        p.lineTo(500, 800 + step);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(p, 0, 50));

        dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                // Маленькая пауза между кадрами для Redmi
                handler.postDelayed(() -> startScrolling(), 5);
            }
        }, null);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        IntentFilter filter = new IntentFilter("com.emilia.scrolltamer.SCROLL_ACTION");
        registerReceiver(scrollReceiver, filter);
        Log.d(TAG, "СЕРВИС: Приемник зарегистрирован");
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}                                          @Override public void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(scrollReceiver); } catch (Exception e) {}
    }
}
