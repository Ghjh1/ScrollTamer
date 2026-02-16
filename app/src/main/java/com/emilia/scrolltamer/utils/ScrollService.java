package com.emilia.scrolltamer.utils;                                           
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;

public class ScrollService extends AccessibilityService {
    private float pendingDistance = 0;
    private boolean isWorking = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final BroadcastReceiver scrollReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            float strength = intent.getFloatExtra("direction", 0);
            // Каждый щелчок добавляет 60 пикселей в "бак" (направление инвертируем)
            pendingDistance += (strength * -60);                                
            if (!isWorking) startEngine();
        }
    };

    private void startEngine() {
        if (Math.abs(pendingDistance) < 5) {
            isWorking = false;
            return;
        }
        isWorking = true;

        // Берем кусочек дистанции для одного "кадра" (например, 10% от накопленного)
        float step = pendingDistance * 0.4f;
        pendingDistance -= step;

        Path p = new Path();
        p.moveTo(500, 800);
        p.lineTo(500, 800 + step);

        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 40);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                // После короткого шага сразу планируем следующий, создавая поток
                mainHandler.postDelayed(() -> startEngine(), 10);
            }
        }, null);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        registerReceiver(scrollReceiver, new IntentFilter("com.emilia.scrolltamer.SCROLL_ACTION"));
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}         @Override public void onInterrupt() {}
    @Override public void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(scrollReceiver); } catch (Exception e) {}
    }
}
