package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;

public class ScrollService extends AccessibilityService {                           private static ScrollService instance;
    private static float targetVelocity = 0;
    private static boolean isEngineRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
                                                                                    @Override
    protected void onServiceConnected() {                                               super.onServiceConnected();
        instance = this;
    }

    public static void scroll(float strength, float x, float y) {                       targetVelocity += (strength * 180);
        if (instance != null && !isEngineRunning) {                                         isEngineRunning = true;
            // Игнорируем x и y от мыши, бьем строго в центр экрана
            instance.runStep(500, 1000);
        }
    }

    private void runStep(final float centerX, final float centerY) {
        if (Math.abs(targetVelocity) < 0.5f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }                                                                       
        float step = targetVelocity * 0.15f;
        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(centerX, centerY);
        p.lineTo(centerX, centerY + step);                                                                                                                              GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 20);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                handler.postDelayed(() -> runStep(centerX, centerY), 5);
            }
            @Override public void onCancelled(GestureDescription gd) { isEngineRunning = false; }
        }, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() { instance = null; super.onDestroy(); }
}
