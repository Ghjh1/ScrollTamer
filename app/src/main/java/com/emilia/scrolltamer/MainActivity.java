package com.emilia.scrolltamer;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.*;
import com.emilia.scrolltamer.utils.ScrollService;
import java.util.ArrayList;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText editDist = findViewById(R.id.edit_dist);
        EditText editTime = findViewById(R.id.edit_time);
        ListView testList = findViewById(R.id.test_list);

        // Тот самый список
        ArrayList<String> items = new ArrayList<>();
        for (int i = 1; i <= 300; i++) items.add("Строка №" + i + " [ТЕСТ СКОЛЬЖЕНИЯ]");
        testList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));

        TextWatcher tw = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                try {
                    float d = Float.parseFloat(editDist.getText().toString());
                    int t = Integer.parseInt(editTime.getText().toString());
                    ScrollService.setParams(d, t);
                } catch(Exception e) {}
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        editDist.addTextChangedListener(tw);
        editTime.addTextChangedListener(tw);
    }
}
