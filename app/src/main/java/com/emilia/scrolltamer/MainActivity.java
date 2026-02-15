package com.emilia.scrolltamer;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.util.Log;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Находим наш ScrollView, чтобы слушать мышь именно на нем
        View container = findViewById(R.id.test_scroll_view);
        if (container != null) {
            container.setOnGenericMotionListener(new View.OnGenericMotionListener() {
                @Override
                public boolean onGenericMotion(View v, MotionEvent event) {
                    // ACTION_SCROLL — это именно движение колесика
                    if (event.getAction() == MotionEvent.ACTION_SCROLL) {
                        float vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                        if (vScroll != 0) {
                            Log.d("ScrollTamer", "ПОЛИГОН: Колесико крутится! Сила: " + vScroll);
                            // Мы возвращаем true, чтобы система не делала стандартный прыжок
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        TextView tv = findViewById(R.id.test_list_text);
        if (tv != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= 100; i++) {
                sb.append("Строка № ").append(i).append("\n");                              }
            tv.setText(sb.toString());
        }
    }
}
