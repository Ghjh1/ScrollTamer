package com.emilia.scrolltamer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tv = findViewById(R.id.test_list_text);
        if (tv != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= 100; i++) {
                sb.append("Строка № ").append(i).append("\n");
            }
            tv.setText(sb.toString());
        }
    }
}
