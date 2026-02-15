package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static final String TAG = "ScrollTamer";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "!!! МАЯК: СВЯЗЬ УСТАНОВЛЕНА !!!");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Ловим клик (TYPE_VIEW_CLICKED = 1)
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            Log.d(TAG, "Клик пойман! Пробую скроллить...");
            smoothScroll();
        }
    }

    private void smoothScroll() {
        Path path = new Path();
        // Настройки под экран 720x1600 (примерно как у Redmi 9C)
        path.moveTo(360, 1200); // Центр низа
        path.lineTo(360, 600);  // Тянем вверх

        // 600 миллисекунд — достаточно плавно для системы
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 600);
        dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, ">>> СКРОЛЛ ВЫПОЛНЕН УСПЕШНО <<<");
            }
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(TAG, "!!! СКРОЛЛ ОТМЕНЕН (Нужна галка в Mi-аккаунте) !!!");
            }
        }, null);
    }

    @Override
    public void onInterrupt() {}
}
