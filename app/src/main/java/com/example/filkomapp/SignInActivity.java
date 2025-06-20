package com.example.filkomapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private FirebaseHelper firebaseHelper;
    private ImageView passwordToggle;
    private boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);

        emailField = findViewById(R.id.namefield);
        passwordField = findViewById(R.id.passfield);
        passwordToggle = findViewById(R.id.imageView5);
        firebaseHelper = new FirebaseHelper(this);


        passwordField.setTransformationMethod(new PasswordTransformationMethod());


        passwordToggle.setOnClickListener(v -> {
            if (isPasswordVisible) {
                passwordField.setTransformationMethod(new PasswordTransformationMethod());
                passwordToggle.setImageResource(R.drawable.eye_icon);
            } else {
                passwordField.setTransformationMethod(null);
                passwordToggle.setImageResource(R.drawable.eye_icon);
            }
            isPasswordVisible = !isPasswordVisible;
            passwordField.setSelection(passwordField.getText().length());
        });

        ImageButton loginButton = findViewById(R.id.button);
        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignInActivity.this, "Mohon isi semua field", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseHelper.signInUser(email, password, new FirebaseHelper.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    startActivity(new Intent(SignInActivity.this, DashboardActivity.class));
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(SignInActivity.this, "Login gagal: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        ImageButton switchToSignup = findViewById(R.id.imageView6);
        switchToSignup.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        });
    }
}
