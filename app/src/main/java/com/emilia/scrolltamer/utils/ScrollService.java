package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;
import android.widget.Toast;

public class ScrollService extends AccessibilityService {
    private static final String TAG = "ScrollTamer";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "!!! МАЯК: СВЯЗЬ УСТАНОВЛЕНА !!!");
        Toast.makeText(this, "v35 на связи!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Логируем ВООБЩЕ ВСЁ, чтобы понять, видит ли сервис экран
        Log.d(TAG, "Тип события: " + event.getEventType());
    }

    @Override
    public void onInterrupt() {}
}
