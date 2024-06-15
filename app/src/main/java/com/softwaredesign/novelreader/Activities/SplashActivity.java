package com.softwaredesign.novelreader.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.softwaredesign.novelreader.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using view binding
        // View binding for the SplashActivity
        com.softwaredesign.novelreader.databinding.ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());

        // Set the content view to the root of the binding
        setContentView(binding.getRoot());

        // after a delay of 2 seconds
        new Handler().postDelayed(() -> {

            // Create an intent to start MainActivity
            startActivity(new Intent(SplashActivity.this, MainActivity.class));

            // Finish the SplashActivity so the user can't navigate back to it
            finish();
        }, 2000);
    }
}