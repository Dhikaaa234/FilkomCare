package com.example.filkomapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_page);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseHelper firebaseHelper = new FirebaseHelper(MainActivity.this);


                if (firebaseHelper.getCurrentUser() != null) {

                    startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                } else {

                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                }

                finish();
            }
        }, 2000);
    }
}
