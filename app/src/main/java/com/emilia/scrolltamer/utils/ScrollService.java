package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;
import android.widget.Toast;

public class ScrollService extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // Этот текст ДОЛЖЕН появиться сразу, как ты включишь сервис в настройках
        Toast.makeText(this, "СЕРВИС ПОДКЛЮЧЕН!", Toast.LENGTH_LONG).show();
        Log.d("ScrollTamer", "Service Connected!");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Ловим абсолютно любое событие
        Log.d("ScrollTamer", "Event received: " + event.getEventType());
    }

    @Override
    public void onInterrupt() {}
}
