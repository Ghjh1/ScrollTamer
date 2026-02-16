package com.emilia.scrolltamer;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import com.emilia.scrolltamer.utils.ScrollService;

public class MainActivity extends Activity {
    @Override                                                                       protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                                             setContentView(R.layout.activity_main);

        View container = findViewById(R.id.test_scroll_view);
        if (container != null) {
            container.setOnGenericMotionListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_SCROLL) {
                    float vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                    if (vScroll != 0) {
                        // ПРЯМОЙ ВЫЗОВ СЕРВИСА
                        ScrollService.scroll(vScroll);
                        return true;
                    }
                }
                return false;
            });
        }

        TextView tv = findViewById(R.id.test_list_text);
        if (tv != null) {
            StringBuilder sb = new StringBuilder();                                         for (int i = 1; i <= 100; i++) sb.append("Строка № ").append(i).append("\n");
            tv.setText(sb.toString());
        }
    }
}
