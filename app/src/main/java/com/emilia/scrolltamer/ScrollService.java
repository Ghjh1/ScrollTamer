package com.emilia.scrolltamer;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.MotionEvent;
public class ScrollService extends AccessibilityService {
    @Override public void onAccessibilityEvent(android.view.accessibility.AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override protected boolean onGenericMotionEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_SCROLL) {
            float axisValue = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
            if (Math.abs(axisValue) > 0) {
                smoothScroll(axisValue > 0);
                return true; 
            }
        }
        return super.onGenericMotionEvent(event);
    }
    private void smoothScroll(boolean up) {
        Path path = new Path();
        path.moveTo(500, 500);
        path.lineTo(500, up ? 550 : 450);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 1, 40));
        dispatchGesture(builder.build(), null, null);
    }
}