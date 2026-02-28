package com.emilia.scrolltamer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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

        // ПРОВЕРКА ПРАВ НА OVERLAY (для Redmi)
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        ScrollView scrollView = findViewById(R.id.main_scroll_view);
        TextView textView = findViewById(R.id.test_list_text);
        debugInfo = findViewById(R.id.debug_info);

        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 1000; i++) {
            content.append("Строка №").append(i).append(" — Испытание Глобальности... 🚀\n");
        }
        textView.setText(content.toString());

        scrollView.setOnGenericMotionListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_SCROLL) {
                float vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                ScrollService.scroll(vScroll, event.getRawX(), event.getRawY());
                return true; // Теперь ошибка "missing return value" исчезнет
            }
            return false;
        });

        updateHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                debugInfo.setText(ScrollService.getDebugData());
                updateHandler.postDelayed(this, 50);
            }
        }, 50);
    }
}

