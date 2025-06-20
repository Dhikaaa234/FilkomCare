package com.example.filkomapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
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

        //ke signup
        ImageButton imageView6 = findViewById(R.id.imageView6);
        imageView6.setOnClickListener(v -> {
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        });

        // Tombol login
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

        // Tampilkan/hide password
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

        // Forgot password
        TextView forgotPass = findViewById(R.id.forgotPass);
        forgotPass.setOnClickListener(v -> showForgotPasswordDialog());

        // Inisialisasi klik "Daftar"
        TextView textdaftar = findViewById(R.id.textdaftar);
        String fullText = getString(R.string.sign_account);
        SpannableString spannableString = new SpannableString(fullText);
        int startIndex = fullText.indexOf("Daftar");
        int endIndex = startIndex + "Daftar".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
            }
        };

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textdaftar.setText(spannableString);
        textdaftar.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Masukan Email untuk Reset Password");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Kirim", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(SignInActivity.this, "Email tidak boleh kosong", Toast.LENGTH_LONG).show();
            } else {
                sendResetEmail(email);
            }
        });
        builder.setNegativeButton("Batal", null);
        builder.show();
    }

    private void sendResetEmail(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignInActivity.this, "Silakan cek email Anda untuk reset password", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SignInActivity.this, "Gagal mengirim email reset password. Pastikan email sudah terdaftar.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
