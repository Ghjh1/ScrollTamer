package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;                       import android.accessibilityservice.GestureDescription;
import android.graphics.Path;                                                   import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float targetVelocity = 0;
    private static boolean isEngineRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {                                               super.onServiceConnected();
        instance = this;                                                            }

    public static void scroll(float strength, float x, float y) {
        // Увеличим импульс, но уменьшим коэффициент шага в цикле для гладкости
        targetVelocity += (strength * 180);

        if (instance != null && !isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y);
        }
    }
                                                                                    private void runStep(final float x, final float y) {
        if (Math.abs(targetVelocity) < 0.3f) {                                              isEngineRunning = false;
            targetVelocity = 0;
            return;
        }

        // Берем меньшую порцию (0.15 вместо 0.2), чтобы растянуть движение             float step = targetVelocity * 0.15f;
        targetVelocity -= step;

        Path p = new Path();
        p.moveTo(x, y);
        p.lineTo(x, y + step);

        // Ультра-короткий жест (15мс) — это почти один кадр дисплея
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 15);
        dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {                    // Минимально возможная пауза
                handler.postDelayed(() -> runStep(x, y), 2);
            }
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                isEngineRunning = false;                                                    }
        }, null);                                                                   }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() { instance = null; super.onDestroy(); }
}
