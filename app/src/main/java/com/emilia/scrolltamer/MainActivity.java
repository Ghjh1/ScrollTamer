package com.emilia.scrolltamer;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import android.os.Handler;
import com.emilia.scrolltamer.utils.ScrollService;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private TextView debugInfo;
    private EditText editDist, editTime;
    private ListView testList;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        debugInfo = findViewById(R.id.debug_info);
        editDist = findViewById(R.id.edit_dist);
        editTime = findViewById(R.id.edit_time);
        testList = findViewById(R.id.test_list);

        // Наполняем список тестовыми данными
        ArrayList<String> items = new ArrayList<>();
        for (int i = 1; i <= 200; i++) items.add("Строка тестового списка №" + i);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        testList.setAdapter(adapter);

        // Слушатели ввода
        TextWatcher tw = new TextWatcher() {
            public void afterTextChanged(Editable s) { updateParams(); }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        editDist.addTextChangedListener(tw);
        editTime.addTextChangedListener(tw);

        startDebugUpdate();
    }

    private void updateParams() {
        try {
            float d = Float.parseFloat(editDist.getText().toString());
            int t = Integer.parseInt(editTime.getText().toString());
            ScrollService.setParams(d, t);
        } catch(Exception e) {}
    }

    private void startDebugUpdate() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                debugInfo.setText(ScrollService.getDebugData());
                handler.postDelayed(this, 500);
            }
        }, 500);
    }
}
