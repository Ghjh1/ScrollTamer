package com.emilia.scrolltamer;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;

public class ScrollService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Здесь мы ловим прокрутку от системы
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
             // Можно добавить логику фильтрации, но пока просто даем скомпилироваться
        }
    }

    @Override
    public void onInterrupt() {}

    private void smoothScroll(boolean up) {
        Path path = new Path();
        path.moveTo(500, 500);
        path.lineTo(500, up ? 600 : 400);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 1, 100));
        dispatchGesture(builder.build(), null, null);
    }
}
