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
        Log.d("ScrollTamer", "v76: Я живой и вижу окна!");
    }

    // Этот метод вызывается, когда в системе что-то меняется (открыл чат, нажал кнопку)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            Log.d("ScrollTamer", "Вижу системный скролл в приложении: " + event.getPackageName());
        }
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;
        
        targetVelocity += (strength * 130); 

        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 1.0f) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        float step = targetVelocity * 0.2f;
        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + step);

        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 40);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                handler.postDelayed(() -> runStep(startX, startY), 10);
            }
            @Override
            public void onCancelled(GestureDescription gd) { isEngineRunning = false; }
        }, null);
    }

    @Override public void onInterrupt() {}
}
