package com.emilia.scrolltamer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.ScrollView;
import android.widget.TextView;
import com.emilia.scrolltamer.utils.ScrollService;

public class MainActivity extends Activity {
    private TextView debugInfo;
    private final Handler updateHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScrollView scrollView = findViewById(R.id.main_scroll_view);
        TextView textView = findViewById(R.id.test_list_text);
        debugInfo = findViewById(R.id.debug_info);

        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 1000; i++) {
            content.append("Строка №").append(i).append(" — Измеряем Шёлк... 📏\n");
        }
        textView.setText(content.toString());

        scrollView.setOnGenericMotionListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_SCROLL) {
                float vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                ScrollService.scroll(vScroll, event.getRawX(), event.getRawY());
                return true;
            }
            return false;
        });

        // Обновляем приборы 20 раз в секунду
        updateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                debugInfo.setText(ScrollService.getDebugData());
                updateHandler.postDelayed(this, 50);
            }
        }, 50);
    }
}
