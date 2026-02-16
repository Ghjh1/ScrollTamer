package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float targetVelocity = 0;
    private static boolean isEngineRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        Log.d("ScrollTamer", "СИСТЕМА: Сервис подключен и готов к магии!");
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) {
            Log.d("ScrollTamer", "?? СЕРВИС МОЛЧИТ: instance is null ??");
            return;
        }
        
        Log.d("ScrollTamer", "!! МАГИЯ В ДЕЛЕ: принимаю импульс !!");
        targetVelocity += (strength * 250); 

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(500, 1000); 
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 0.1f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        float step = targetVelocity * 0.1f;
        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + step);

        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 10);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                handler.postDelayed(() -> runStep(startX, startY), 1);
            }
            @Override
            public void onCancelled(GestureDescription gd) { isEngineRunning = false; }
        }, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
    @Override public void onDestroy() { instance = null; super.onDestroy(); }
}
