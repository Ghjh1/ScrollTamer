package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;                                            import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static final String TAG = "ScrollTamer";
    private boolean isScrolling = false;

    private final BroadcastReceiver scrollReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isScrolling) return; // Не спамим жестами

            float direction = intent.getFloatExtra("direction", 0);
            float x = intent.getFloatExtra("x", 500);
            float y = intent.getFloatExtra("y", 1000);

            executeSilkScroll(x, y, direction);
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        try {
            IntentFilter filter = new IntentFilter("com.emilia.scrolltamer.SCROLL_ACTION");
            // Упрощенная регистрация для стабильности
            registerReceiver(scrollReceiver, filter);
            Log.d(TAG, "СЕРВИС: Запущен и ждет команд");
        } catch (Exception e) {                                                             Log.e(TAG, "Ошибка регистрации: " + e.getMessage());
        }
    }

    private void executeSilkScroll(float x, float y, float strength) {
        isScrolling = true;
        Path p = new Path();
        p.moveTo(x, y);

        // Инвертируем силу, чтобы крутя колесико "на себя",
        // мы тянули список "вверх" (как пальцем)
        float distance = strength * -200;                                               p.lineTo(x, y + distance);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(p, 0, 250));

        dispatchGesture(builder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                isScrolling = false;
            }
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                isScrolling = false;
            }
        }, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(scrollReceiver); } catch (Exception e) {}
    }                                                                           }
