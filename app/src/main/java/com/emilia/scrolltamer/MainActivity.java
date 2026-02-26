package com.emilia.scrolltamer;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.LinearLayout;
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

        // Всплывающее окно ПРИ ЗАПУСКЕ
        showSettingsDialog();

        scrollView.setOnGenericMotionListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_SCROLL) {
                float vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                ScrollService.scroll(vScroll, event.getRawX(), event.getRawY());
                return true;
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

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Настройки калибровки");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText inputD = new EditText(this);
        inputD.setHint("Дистанция (D) сейчас: 14.0");
        inputD.setText("14.0");
        layout.addView(inputD);

        final EditText inputT = new EditText(this);
        inputT.setHint("Время (T) сейчас: 100");
        inputT.setText("100");
        layout.addView(inputT);

        builder.setView(layout);
        builder.setPositiveButton("Пуск", (dialog, which) -> {
            try {
                float d = Float.parseFloat(inputD.getText().toString());
                int t = Integer.parseInt(inputT.getText().toString());
                ScrollService.setParams(d, t);
            } catch (Exception e) {}
        });

        builder.setCancelable(false);
        builder.show();
    }
}
