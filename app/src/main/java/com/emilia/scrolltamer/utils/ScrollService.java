package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.Gravity;


public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float velocity = 0;
    private static long lastEventTime = 0;
    private static int gestureCounter = 0;
    private WindowManager windowManager;
    private View overlayView;

    @Override
    protected void onServiceConnected() {
        instance = this;
        setupGlobalOverlay();
    }

    private void setupGlobalOverlay() {
        try {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            overlayView = new View(this);
            
            int layoutType = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O 
                             ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
                             : WindowManager.LayoutParams.TYPE_PHONE;
    
            // Настраиваем "Стеклянный щит" на весь экран
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | // Пальцы проходят насквозь
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            );
            
            params.gravity = Gravity.TOP | Gravity.LEFT;
    
            overlayView.setOnGenericMotionListener((v, event) -> {
                // На этом слое мы ловим КОРНЕВОЕ событие прокрутки
                if (event.getAction() == MotionEvent.ACTION_SCROLL) {
                    float vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                    // Бьем точно в координаты курсора
                    scroll(vScroll, event.getRawX(), event.getRawY());
                    return true; 
                }
                return false;
            });
    
            windowManager.addView(overlayView, params);
        } catch (Exception e) {
            // Ошибка может быть, если сервис перезапустился, а окно еще висит
        }
    }


    public static String getDebugData() {
        return String.format("D: %.0f | V: %.1f | GLOBAL SNIPER", 14.0f + velocity, velocity);
    }

    public static void scroll(float delta, float x, float y) {
        if (instance == null) return;
        long now = System.currentTimeMillis();
        float direction = Math.signum(delta);
        long interval = now - lastEventTime;
        lastEventTime = now;

        if (interval < 220) {
            float inc = (interval < 80) ? 11.0f : 3.5f;
            velocity += inc; 
            if (velocity > 36.0f) velocity = 36.0f; 
        } else {
            velocity = 0;
            gestureCounter = 0;
        }

        int finalStep = (int)(14 + velocity);
        float ratio = velocity / 36.0f; 
        float startT = 39.0f;
        float targetT = (direction < 0) ? 24.0f : 21.0f; 
        float virtualT = startT - (ratio * (startT - targetT));

        int floorT = (int) Math.floor(virtualT);
        float fractionalPart = virtualT - floorT;
        gestureCounter++;
        int finalT = ( (gestureCounter % 10) < (fractionalPart * 10) ) ? floorT + 1 : floorT;

        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x, y + (finalStep * direction));

        GestureDescription.StrokeDescription stroke = 
            new GestureDescription.StrokeDescription(path, 0, finalT);
            
        try {
            instance.dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
        } catch (Exception e) { }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}

