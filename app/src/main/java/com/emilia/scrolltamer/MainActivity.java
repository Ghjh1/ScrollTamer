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

        EditText edD = findViewById(R.id.edit_dist);
        EditText edT = findViewById(R.id.edit_time);
        ListView lv = findViewById(R.id.test_list);

        ArrayList<String> items = new ArrayList<>();
        for (int i = 1; i <= 500; i++) items.add("Строка теста " + i);
        lv.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));

        TextWatcher tw = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                try {
                    float d = Float.parseFloat(edD.getText().toString());
                    int t = Integer.parseInt(edT.getText().toString());
                    ScrollService.setParams(d, t);
                } catch(Exception e) {}
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        };
        edD.addTextChangedListener(tw);
        edT.addTextChangedListener(tw);
    }
}
