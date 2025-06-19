package com.example.filkomapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class ReportDetailActivity extends AppCompatActivity {

    private ImageView reportImage;
    private TextView titleText, locationText, descriptionText, statusText, likesText;
    private Spinner statusSpinner;
    private LinearLayout likeLayout, statusContainer;

    private FirebaseHelper firebaseHelper;
    private String reportId;
    private boolean isAdmin = false;
    private FirebaseUser currentUser;
    private boolean isSpinnerInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan);

        reportImage = findViewById(R.id.imageView);
        titleText = findViewById(R.id.title);
        locationText = findViewById(R.id.place);
        descriptionText = findViewById(R.id.description);
        statusText = findViewById(R.id.statusText);
        likesText = findViewById(R.id.likes);
        likeLayout = findViewById(R.id.like);
        statusSpinner = findViewById(R.id.statusSpinner);
        statusContainer = findViewById(R.id.statusContainer);

        firebaseHelper = new FirebaseHelper(this);
        currentUser = firebaseHelper.getAuth().getCurrentUser();
        reportId = getIntent().getStringExtra("reportId");

        if (reportId == null) {
            finish();
            return;
        }

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
                            setStatusBackground(report.getStatus());
                            likesText.setText(String.valueOf(report.getLikes()));

                            String[] statuses = getResources().getStringArray(R.array.status_options);
                            for (int i = 0; i < statuses.length; i++) {
                                if (statuses[i].equalsIgnoreCase(report.getStatus())) {
                                    statusSpinner.setSelection(i);
                                    break;
                                }
                            }

                            // Disable spinner jika status sudah fixed
                            if (report.getStatus().equalsIgnoreCase("fixed")) {
                                statusSpinner.setEnabled(false);
                            }

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

        if (currentUser != null) {
            firebaseHelper.checkAdminStatus(currentUser.getUid(), new FirebaseHelper.AdminCheckCallback() {
                @Override
                public void onAdminChecked(boolean admin) {
                    isAdmin = admin;
                    statusContainer.setVisibility(admin ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(ReportDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            statusContainer.setVisibility(View.GONE);
        }

        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true;
                    return;
                }

                String selectedStatus = adapterView.getItemAtPosition(position).toString();

                // Jika admin memilih "Fixed", konfirmasi dulu
                if (selectedStatus.equalsIgnoreCase("fixed")) {
                    new AlertDialog.Builder(ReportDetailActivity.this)
                            .setTitle("Konfirmasi Status")
                            .setMessage("Setelah status diubah menjadi 'Fixed', status tidak bisa diubah lagi. Lanjutkan?")
                            .setPositiveButton("Ya", (dialog, which) -> updateStatus(selectedStatus))
                            .setNegativeButton("Batal", (dialog, which) -> {
                                // Kembalikan ke pilihan sebelumnya (supaya tidak langsung berubah)
                                statusSpinner.setSelection(getSpinnerIndexByStatus(statusText.getText().toString()));
                            })
                            .setCancelable(false)
                            .show();
                } else {
                    updateStatus(selectedStatus);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

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

    private void updateStatus(String status) {
        firebaseHelper.updateReportStatus(reportId, status, new FirebaseHelper.StatusCallback() {
            @Override
            public void onSuccess() {
                statusText.setText(status);
                setStatusBackground(status);
                Toast.makeText(ReportDetailActivity.this, "Status diperbarui", Toast.LENGTH_SHORT).show();
                createStatusChangeNotification(status);

                if (status.equalsIgnoreCase("fixed")) {
                    statusSpinner.setEnabled(false);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ReportDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getSpinnerIndexByStatus(String status) {
        String[] statuses = getResources().getStringArray(R.array.status_options);
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equalsIgnoreCase(status)) {
                return i;
            }
        }
        return 0;
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

    private void setStatusBackground(String status) {
        if (status == null) return;

        switch (status.toLowerCase()) {
            case "new":
                statusText.setBackgroundResource(R.drawable.bg_status_new);
                break;
            case "processing":
                statusText.setBackgroundResource(R.drawable.bg_status_processing);
                break;
            case "fixed":
                statusText.setBackgroundResource(R.drawable.bg_status_fixed);
                break;
            case "unfixable":
                statusText.setBackgroundResource(R.drawable.bg_status_unfixable);
                break;
            default:
                statusText.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                break;
        }
    }
}
