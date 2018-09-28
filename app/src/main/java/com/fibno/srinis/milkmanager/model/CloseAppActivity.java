package com.fibno.srinis.milkmanager.model;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.fibno.srinis.milkmanager.R;

public class CloseAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_close_app);
        finishAffinity();
    }
}
