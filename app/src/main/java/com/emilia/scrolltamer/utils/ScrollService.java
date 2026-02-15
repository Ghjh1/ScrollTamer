package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class ScrollService extends AccessibilityService {
    private static final String TAG = "ScrollTamer";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Ловим клик, но даем системе 200мс "отдышаться"
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            Log.d(TAG, "Клик пойман. Готовлю сверх-длинный свайп...");

            // Небольшая задержка перед жестом
            new android.os.Handler().postDelayed(() -> {
                bigScroll();
            }, 200);
        }
    }

    private void bigScroll() {
        Path path = new Path();
        // Начнем почти от самого низа экрана и протянем до самого верха
        // Попробуем координаты, которые точно попадут в центр любого экрана
        path.moveTo(500, 1500);
        path.lineTo(500, 100);

        // Увеличим время до 1200мс, чтобы ты ГЛАЗАМИ видел движение
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 1200);

        dispatchGesture(new GestureDescription.Builder().addStroke(stroke).build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "ОТЧЕТ: Сверх-длинный жест выполнен успешно!");
            }
            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(TAG, "ОТЧЕТ: Жест прерван (возможно, ты коснулся экрана в этот момент)");
            }
        }, null);
    }

    @Override
    public void onInterrupt() {}
}
