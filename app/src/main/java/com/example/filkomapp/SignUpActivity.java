package com.example.filkomapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameField, emailField, passwordField, confirmPasswordField;
    private FirebaseHelper firebaseHelper;
    private ImageView togglePassword1, togglePassword2;
    private boolean isPasswordVisible1 = false;
    private boolean isPasswordVisible2 = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        nameField = findViewById(R.id.namefield);
        emailField = findViewById(R.id.gmailfield);
        passwordField = findViewById(R.id.passfield);
        confirmPasswordField = findViewById(R.id.passfield2);
        togglePassword1 = findViewById(R.id.imageView5);
        togglePassword2 = findViewById(R.id.imageView10);

        firebaseHelper = new FirebaseHelper(this);


        passwordField.setTransformationMethod(new PasswordTransformationMethod());
        confirmPasswordField.setTransformationMethod(new PasswordTransformationMethod());


        togglePassword1.setOnClickListener(v -> {
            if (isPasswordVisible1) {
                passwordField.setTransformationMethod(new PasswordTransformationMethod());
                togglePassword1.setImageResource(R.drawable.eye_icon);
                passwordField.setTransformationMethod(null);
                togglePassword1.setImageResource(R.drawable.eye_icon);
            }
            isPasswordVisible1 = !isPasswordVisible1;
            passwordField.setSelection(passwordField.getText().length());
        });


        togglePassword2.setOnClickListener(v -> {
            if (isPasswordVisible2) {
                confirmPasswordField.setTransformationMethod(new PasswordTransformationMethod());
                togglePassword2.setImageResource(R.drawable.eye_icon);
            } else {
                confirmPasswordField.setTransformationMethod(null);
                togglePassword2.setImageResource(R.drawable.eye_icon);
            }
            isPasswordVisible2 = !isPasswordVisible2;
            confirmPasswordField.setSelection(confirmPasswordField.getText().length());
        });


        findViewById(R.id.button).setOnClickListener(v -> {
            String name = nameField.getText().toString().trim();
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            String confirmPassword = confirmPasswordField.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(SignUpActivity.this, "Mohon isi semua field", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignUpActivity.this, "Password tidak cocok", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 8) {
                Toast.makeText(SignUpActivity.this, "Password minimal 8 karakter", Toast.LENGTH_SHORT).show();
                return;
            }

            firebaseHelper.registerUser(name, email, password, new FirebaseHelper.AuthCallback() {
                @Override
                public void onSuccess(FirebaseUser user) {
                    Toast.makeText(SignUpActivity.this, "Registrasi berhasil", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignUpActivity.this, DashboardActivity.class));
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(SignUpActivity.this, "Registrasi gagal: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });


        findViewById(R.id.imageButton).setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
        });
    }
}
