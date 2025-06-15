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

        // Simulasi proses loading selama 2 detik
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseHelper firebaseHelper = new FirebaseHelper(MainActivity.this);

                // ✅ Gunakan method getCurrentUser() (bukan akses langsung mAuth)
                if (firebaseHelper.getCurrentUser() != null) {
                    // Jika sudah login, masuk ke dashboard
                    startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                } else {
                    // Jika belum login, masuk ke sign-in
                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                }

                finish(); // Tutup activity loading
            }
        }, 2000); // delay 2 detik
    }
}
