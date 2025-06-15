package com.example.filkomapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ReportDetailActivity extends AppCompatActivity {

    private ImageView reportImage;
    private TextView titleText, locationText, descriptionText, statusText, likesText;
    private ImageButton statusButton1, statusButton2, statusButton3, statusButton4;
    private FirebaseHelper firebaseHelper;
    private String reportId;
    private boolean isAdmin = false;
    private FirebaseUser currentUser;
    private LinearLayout likeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan);

        // Inisialisasi view
        reportImage = findViewById(R.id.imageView);
        titleText = findViewById(R.id.title);
        locationText = findViewById(R.id.place);
        descriptionText = findViewById(R.id.description);
        statusText = findViewById(R.id.statusText);
        likesText = findViewById(R.id.likes);
        likeLayout = findViewById(R.id.like);

        statusButton1 = findViewById(R.id.statusButton1);
        statusButton2 = findViewById(R.id.statusButton2);
        statusButton3 = findViewById(R.id.statusButton3);
        statusButton4 = findViewById(R.id.statusButton4);

        firebaseHelper = new FirebaseHelper(this);
        currentUser = firebaseHelper.getAuth().getCurrentUser();

        reportId = getIntent().getStringExtra("reportId");
        if (reportId == null) {
            finish();
            return;
        }

        // Cek admin
        if (currentUser != null) {
            firebaseHelper.checkAdminStatus(currentUser.getUid(), new FirebaseHelper.AdminCheckCallback() {
                @Override
                public void onAdminChecked(boolean admin) {
                    isAdmin = admin;
                    setupStatusButtons();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(ReportDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Load data laporan
        firebaseHelper.getDatabase().child("reports").child(reportId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Report report = snapshot.getValue(Report.class);
                        if (report != null) {
                            titleText.setText(report.getTitle());
                            locationText.setText(report.getLocation());
                            descriptionText.setText(report.getDescription());
                            statusText.setText(report.getStatus());
                            likesText.setText(String.valueOf(report.getLikes()));

                            if (report.getImage() != null && !report.getImage().isEmpty()) {
                                Bitmap bitmap = FirebaseHelper.base64ToBitmap(report.getImage());
                                reportImage.setImageBitmap(bitmap);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ReportDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Tombol Like
        likeLayout.setOnClickListener(v -> {
            if (currentUser != null) {
                firebaseHelper.toggleLikeReport(reportId, currentUser.getUid(), new FirebaseHelper.LikeCallback() {
                    @Override
                    public void onSuccess(boolean isLiked) {
                        firebaseHelper.getDatabase().child("reports").child(reportId).child("likes")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Integer likes = snapshot.getValue(Integer.class);
                                        if (likes != null) {
                                            likesText.setText(String.valueOf(likes));
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Toast.makeText(ReportDetailActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(ReportDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Navigasi tombol
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            startActivity(new Intent(ReportDetailActivity.this, DashboardActivity.class));
            finish();
        });
        findViewById(R.id.btnProfile).setOnClickListener(v -> startActivity(new Intent(ReportDetailActivity.this, ProfileActivity.class)));
        findViewById(R.id.btnKirim).setOnClickListener(v -> {
            if (!isAdmin) {
                startActivity(new Intent(ReportDetailActivity.this, UploadActivity.class));
            } else {
                Toast.makeText(ReportDetailActivity.this, "Admin tidak bisa mengirim laporan", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupStatusButtons() {
        if (isAdmin) {
            statusButton1.setVisibility(View.VISIBLE);
            statusButton2.setVisibility(View.VISIBLE);
            statusButton3.setVisibility(View.VISIBLE);
            statusButton4.setVisibility(View.VISIBLE);

            statusButton1.setOnClickListener(v -> updateStatus("New"));
            statusButton2.setOnClickListener(v -> updateStatus("Processing"));
            statusButton3.setOnClickListener(v -> updateStatus("Fixed"));
            statusButton4.setOnClickListener(v -> updateStatus("Unfixable"));
        } else {
            statusButton1.setVisibility(View.GONE);
            statusButton2.setVisibility(View.GONE);
            statusButton3.setVisibility(View.GONE);
            statusButton4.setVisibility(View.GONE);
        }
    }

    private void updateStatus(String status) {
        firebaseHelper.updateReportStatus(reportId, status, new FirebaseHelper.StatusCallback() {
            @Override
            public void onSuccess() {
                statusText.setText(status);
                Toast.makeText(ReportDetailActivity.this, "Status diperbarui", Toast.LENGTH_SHORT).show();
                createStatusChangeNotification(status);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ReportDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createStatusChangeNotification(String newStatus) {
        String notificationId = firebaseHelper.getDatabase().child("notifications").push().getKey();
        String title = "Status Laporan Diubah";
        String message = "Laporan Anda \"" + titleText.getText().toString() + "\" statusnya diubah menjadi " + newStatus;
        String userId = currentUser != null ? currentUser.getUid() : "";

        Notification notification = new Notification(
                notificationId,
                title,
                message,
                reportId,
                userId,
                System.currentTimeMillis(),
                false
        );

        firebaseHelper.getDatabase().child("notifications").child(notificationId).setValue(notification);
    }
}
