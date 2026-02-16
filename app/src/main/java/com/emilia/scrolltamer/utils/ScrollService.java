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
        Log.d("ScrollTamer", "v68: Двигатель запущен");
    }

    public static void scroll(float strength, float x, float y) {
        // Если крутишь "на себя" и список едет не туда — поменяй 120 на -120
        targetVelocity += (strength * 120); 

        if (instance != null && !isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y);
        }
    }

    private void runStep(final float x, final float y) {
        if (Math.abs(targetVelocity) < 0.5f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        // 0.2 — это коэффициент вязкости. Чем меньше, тем медленнее затухает
        float step = targetVelocity * 0.2f;
        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(x, y);
        p.lineTo(x, y + step);

        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 30);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                handler.postDelayed(() -> runStep(x, y), 5);
            }
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                isEngineRunning = false;
            }
        }, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() { instance = null; super.onDestroy(); }
}
