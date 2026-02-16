package com.emilia.scrolltamer.utils;
                                                                                import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;                         import android.graphics.Path;                                                   import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float targetVelocity = 0;                                        private static boolean isEngineRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());        
    @Override                                                                       protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    public static void scroll(float strength, float x, float y) {
        // Ограничиваем максимальный импульс, чтобы не было "катапульты"
        float impulse = strength * 150;
        if (Math.abs(impulse) > 500) impulse = Math.signum(impulse) * 500;

        targetVelocity += impulse;

        if (instance != null && !isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y);
        }
    }

    private void runStep(final float startX, final float startY) {
        if (Math.abs(targetVelocity) < 0.5f) {
            isEngineRunning = false;
            targetVelocity = 0;                                                             return;
        }                                                                       
        float step = targetVelocity * 0.18f;
        targetVelocity -= step;

        // Безопасная зона: не кликаем по верхним 200 пикселям (где кнопки)
        float safeY = (startY < 250) ? startY + 300 : startY;

        Path p = new Path();
        p.moveTo(startX, safeY);
        p.lineTo(startX, safeY + step);

        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 20);                                                                   dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                handler.postDelayed(() -> runStep(startX, safeY), 5);
            }
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                isEngineRunning = false;                                                        targetVelocity = 0;
            }                                                                           }, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}         @Override public void onInterrupt() { isEngineRunning = false; targetVelocity = 0; }
    @Override public void onDestroy() { instance = null; super.onDestroy(); }
}
