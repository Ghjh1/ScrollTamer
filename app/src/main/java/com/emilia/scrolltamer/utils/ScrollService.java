package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class ScrollService extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "СЕРВИС ОЖИЛ!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Проверяем клик по экрану
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            Toast.makeText(this, "Вижу клик! Скроллю...", Toast.LENGTH_SHORT).show();
            performAction();
        }
    }

    private void performAction() {
        Path path = new Path();
        path.moveTo(540, 1500);
        path.lineTo(540, 500);

        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 500);
        dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), null, null);
    }

    @Override
    public void onInterrupt() {}
}
