package com.example.trackforsafe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class logo_activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setContentView(R.layout.activity_logo);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Intent intent = new Intent(logo_activity.this,LoginActivity.class);
            startActivity(intent);
            finish();
        },2000);
    }
}