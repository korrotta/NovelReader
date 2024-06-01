package com.softwaredesign.novelreader.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.softwaredesign.novelreader.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    // View binding for the SplashActivity
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using view binding
        binding = ActivitySplashBinding.inflate(getLayoutInflater());

        // Set the content view to the root of the binding
        setContentView(binding.getRoot());

        // after a delay of 2 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                // Create an intent to start MainActivity
                startActivity(new Intent(SplashActivity.this, MainActivity.class));

                // Finish the SplashActivity so the user can't navigate back to it
                finish();
            }
        }, 2000);
    }
}