package com.emilia.scrolltamer;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Handler;
import com.emilia.scrolltamer.utils.ScrollService;

public class MainActivity extends Activity {
    private TextView debugInfo;
    private EditText editDist, editTime;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        debugInfo = findViewById(R.id.debug_info);
        editDist = findViewById(R.id.edit_dist);
        editTime = findViewById(R.id.edit_time);

        // Устанавливаем начальные значения в поля ввода
        editDist.setText("14");
        editTime.setText("100");

        // Слушатель для дистанции
        editDist.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                try { ScrollService.setParams(Float.parseFloat(s.toString()), -1); } catch(Exception e){}
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        // Слушатель для времени
        editTime.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                try { ScrollService.setParams(-1, Integer.parseInt(s.toString())); } catch(Exception e){}
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        startDebugUpdate();
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
