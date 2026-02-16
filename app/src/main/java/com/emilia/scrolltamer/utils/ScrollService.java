package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;                         import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;                           import android.os.Handler;
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
        Log.d("ScrollTamer", "СЕРВИС: Связь установлена!");
    }

    public static void scroll(float strength, float x, float y) {
        Log.d("ScrollTamer", "СЕРВИС: Вызов получен, сила: " + strength);
        targetVelocity += (strength * -150); // Увеличим импульс для теста

        if (instance != null && !isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y);
        }
    }

    private void runStep(float x, float y) {                                            if (Math.abs(targetVelocity) < 1) {
            isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        float step = targetVelocity * 0.3f;
        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(x, y);                                                                 p.lineTo(x, y + step);
                                                                                        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 40);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override                                                                       public void onCompleted(GestureDescription gestureDescription) {
                handler.postDelayed(() -> runStep(x, y), 10);
            }
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                isEngineRunning = false;
            }
        }, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}                                          @Override public void onDestroy() { instance = null; super.onDestroy(); }
}
