package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private WindowManager windowManager;
    private View interceptorView;
    private static float targetVelocity = 0;
    private static boolean isEngineRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        setupInterceptor();
        Log.d("ScrollTamer", "v78: Невидимое ухо установлено!");
    }

    private void setupInterceptor() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        // Создаем крошечную невидимую область для ловли событий
        interceptorView = new View(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            1, 1, // Размер 1x1 пиксель (невидимка)
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.LEFT;

        // Попытка поймать скролл через перехватчик (экспериментально)
        interceptorView.setOnGenericMotionListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_SCROLL) {
                float vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                Log.d("ScrollTamer", "ПОЙМАЛ СКРОЛЛ ВНЕ ПРИЛОЖЕНИЯ: " + vScroll);
                scroll(vScroll, event.getX(), event.getY());
                return true;
            }
            return false;
        });

        try { windowManager.addView(interceptorView, params); } catch (Exception e) { Log.e("ScrollTamer", "Ошибка Overlay", e); }
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;
        targetVelocity += (strength * 130); 
        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(540, 1000); 
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
            public void onCompleted(GestureDescription gd) { handler.postDelayed(() -> runStep(startX, startY), 10); }
            @Override public void onCancelled(GestureDescription gd) { isEngineRunning = false; }
        }, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override public void onDestroy() { 
        if (windowManager != null && interceptorView != null) windowManager.removeView(interceptorView);
        instance = null; 
        super.onDestroy(); 
    }
}
