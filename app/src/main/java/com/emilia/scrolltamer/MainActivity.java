package com.emilia.scrolltamer;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.ScrollView;
import android.widget.TextView;
import com.emilia.scrolltamer.utils.ScrollService;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScrollView scrollView = findViewById(R.id.main_scroll_view);
        TextView textView = findViewById(R.id.test_list_text);

        // Наполняем "Шёлковый путь" текстом для теста
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 500; i++) {
            content.append("Строка №").append(i).append(": Листай этот шёлк... 🍯\n");
        }
        textView.setText(content.toString());

        // Главный перехватчик
        scrollView.setOnGenericMotionListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_SCROLL) {
                float vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                // Отправляем сигнал в наш идеальный движок
                ScrollService.scroll(vScroll, event.getRawX(), event.getRawY());
                return true; // Полностью блокируем системный дерганый скролл
            }
            return false;
        });
    }
}
