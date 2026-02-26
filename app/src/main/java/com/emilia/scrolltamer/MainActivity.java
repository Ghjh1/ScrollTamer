package com.emilia.scrolltamer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.Editable;
import android.text.TextWatcher;
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
        final EditText edD = findViewById(R.id.edit_d);
        final EditText edT = findViewById(R.id.edit_t);

        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 1000; i++) {
            content.append("Строка №").append(i).append(" — Измеряем Шёлк... 📏\n");
        }
        textView.setText(content.toString());

        TextWatcher tw = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                try {
                    float d = Float.parseFloat(edD.getText().toString());
                    int t = Integer.parseInt(edT.getText().toString());
                    ScrollService.setParams(d, t);
                } catch(Exception e) {}
            }
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {}
        };
        edD.addTextChangedListener(tw);
        edT.addTextChangedListener(tw);

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
}
