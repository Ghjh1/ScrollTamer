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
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        debugInfo = findViewById(R.id.debug_info);
        final EditText edD = findViewById(R.id.edit_dist);
        final EditText edT = findViewById(R.id.edit_time);

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
