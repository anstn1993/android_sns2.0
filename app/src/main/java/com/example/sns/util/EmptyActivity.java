package com.example.sns.util;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.sns.R;

public class EmptyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);
        finish();
    }
}
