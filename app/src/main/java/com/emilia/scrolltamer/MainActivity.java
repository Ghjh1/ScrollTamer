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
        builder.setTitle("D, T и Задержка");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText inputD = new EditText(this);
        inputD.setHint("Дистанция (D): 13.0");
        inputD.setText("13.0");
        layout.addView(inputD);

        final EditText inputT = new EditText(this);
        inputT.setHint("Время (T): 40");
        inputT.setText("40");
        layout.addView(inputT);

        final EditText inputDelay = new EditText(this);
        inputDelay.setHint("Задержка (Delay): 0");
        inputDelay.setText("0");
        layout.addView(inputDelay);

        builder.setView(layout);
        builder.setPositiveButton("Пуск", (dialog, which) -> {
            try {
                float d = Float.parseFloat(inputD.getText().toString());
                int t = Integer.parseInt(inputT.getText().toString());
                int delay = Integer.parseInt(inputDelay.getText().toString());
                ScrollService.setParams(d, t, delay);
            } catch (Exception e) {}
        });

        builder.setCancelable(false);
        builder.show();
    }
}
