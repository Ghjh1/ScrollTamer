package com.emilia.scrolltamer.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.os.Handler;
import android.os.Looper;

public class ScrollService extends AccessibilityService {
    private static ScrollService instance;
    private static float targetVelocity = 0;
    private static float lastStepValue = 0;
    private static int brakeCounter = 0;
    private static boolean isEngineRunning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
    }

    public static String getDebugData() {
        return String.format("VELOCITY: %.2f\nSTEP: %.2f\nACTIVE: %s\nBRAKE: %d", 
                targetVelocity, lastStepValue, isEngineRunning ? "YES" : "NO", brakeCounter);
    }

    public static void scroll(float strength, float x, float y) {
        if (instance == null) return;

        // 1. Обработка тормоза (теперь работает ВСЕГДА, так как targetVelocity обновляется мгновенно)
        if (Math.signum(strength) != Math.signum(targetVelocity) && Math.abs(targetVelocity) > 1) {
            brakeCounter++;
            if (brakeCounter >= 3) {
                targetVelocity = 0;
                brakeCounter = 0;
            } else {
                targetVelocity *= 0.3f; // Сделали тормоз чуть сильнее
            }
            return; 
        }
        
        brakeCounter = 0;
        
        // 2. Мгновенная запись импульса в "бак" (Ни один клик не пропадет!)
        targetVelocity += (strength * 95); 

        // 3. Запуск двигателя, если он стоял
        if (!isEngineRunning) {
            isEngineRunning = true;
            instance.runStep(x, y); 
        }
    }

    private void runStep(final float startX, final float startY) {
        // Если энергия кончилась — глушим мотор
        if (Math.abs(targetVelocity) < 0.3f) {
            isEngineRunning = false;
            targetVelocity = 0;
            lastStepValue = 0;
            return;
        }

        // Вычисляем размер текущего "куска" пути
        lastStepValue = targetVelocity * 0.18f; 
        if (Math.abs(lastStepValue) > 110) lastStepValue = Math.signum(lastStepValue) * 110;

        // Вычитаем потраченную энергию
        targetVelocity -= lastStepValue;

        Path p = new Path();
        p.moveTo(startX, startY);
        p.lineTo(startX, startY + lastStepValue);

        // 20мс - оптимальная мягкость
        GestureDescription.StrokeDescription sd = new GestureDescription.StrokeDescription(p, 0, 20);
        
        try {
            // Отправляем жест и СРАЗУ планируем следующий, опираясь на остаток targetVelocity
            dispatchGesture(new GestureDescription.Builder().addStroke(sd).build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gd) {
                    // Короткая пауза, чтобы Android успел "переварить" окончание потока
                    handler.postDelayed(() -> {
                        runStep(startX, startY);
                    }, 5); 
                }
                @Override public void onCancelled(GestureDescription gd) { 
                    // Если система отменила - не страшно, попробуем еще раз через миг
                    handler.postDelayed(() -> runStep(startX, startY), 10);
                }
            }, null);
        } catch (Exception e) {
            isEngineRunning = false;
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() { instance = null; }
}
