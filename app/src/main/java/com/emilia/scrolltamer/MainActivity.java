package com.emilia.scrolltamer;

import android.app.Activity;
import android.os.Bundle;
import android.view.GenericMotionEvent;
import android.view.View;
import android.widget.TextView;
import android.util.Log;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View container = findViewById(R.id.test_scroll_view);
        container.setOnGenericMotionListener((v, event) -> {
            // Проверяем, что это событие прокрутки (колесико)
            if (event.getAction() == GenericMotionEvent.ACTION_SCROLL) {
                float vScroll = event.getAxisValue(GenericMotionEvent.AXIS_VSCROLL);
                if (vScroll != 0) {
                    Log.d("ScrollTamer", "ПОЛИГОН: Колесико крутится! Значение: " + vScroll);
                    // Здесь мы скоро свяжем это с сервисом
                }
            }
            return false;
        });

        TextView tv = findViewById(R.id.test_list_text);
        if (tv != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= 100; i++) {
                sb.append("Строка № ").append(i).append("\n");
            }
            tv.setText(sb.toString());
        }
    }
}
