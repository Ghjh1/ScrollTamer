package com.emilia.scrolltamer.utils;                                           
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;                                          private static float targetVelocity = 0;
    private static boolean isEngineRunning = false;                                 private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }
                                                                                    public static void scroll(float strength, float x, float y) {
        targetVelocity += (strength * 180);                                             if (instance != null && !isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y);
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 0.3f) {
            isEngineRunning = false;                                                        targetVelocity = 0;
            return;
        }

        // Вот она, исправленная переменная step
        float step = targetVelocity * 0.15f;
        targetVelocity -= step;                                                 
        // Сдвигаем Y на 200 пикселей вниз, чтобы не попадать по кнопкам вверху
        float safeY = startY;
        if (startY < 400) safeY = startY + 300;

        Path p = new Path();
        p.moveTo(startX, safeY);
        p.lineTo(startX, safeY + step);                                         
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 15);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                handler.postDelayed(() -> runStep(startX, startY), 2);
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
