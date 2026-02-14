package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class ScrollService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Ловим момент, когда ты кликаешь или взаимодействуешь с чем-то мышкой
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED ||
            event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            performScrollAtFocus();
        }
    }

    private void performScrollAtFocus() {
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo == null) return;

        // Пытаемся найти элемент, который можно скроллить, там где сейчас фокус
        // Для начала сделаем просто плавный жест в центре активного окна
        Rect rect = new Rect();
        nodeInfo.getBoundsInScreen(rect);

        int centerX = rect.centerX();
        int centerY = rect.centerY();

        Path swipePath = new Path();
        swipePath.moveTo(centerX, centerY + 300);
        swipePath.lineTo(centerX, centerY - 300);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(swipePath, 0, 700);
        dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);

        nodeInfo.recycle();
    }

    @Override
    public void onInterrupt() {}
}
