package com.example.filkomapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private TextView nameText, statusText, nimText, prodiText;
    private LinearLayout nimProdiLayout;
    private RecyclerView reportsRecyclerView;
    private ReportAdapter reportAdapter;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameText = findViewById(R.id.name);
        statusText = findViewById(R.id.status);
        nimProdiLayout = findViewById(R.id.infoNimProdi);
        nimText = findViewById(R.id.nim);
        prodiText = findViewById(R.id.prodi);
        reportsRecyclerView = findViewById(R.id.recyclerViewNews);
        firebaseHelper = new FirebaseHelper(this);

        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportAdapter = new ReportAdapter(this, new ReportAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Report report) {
                Intent intent = new Intent(ProfileActivity.this, ReportDetailActivity.class);
                intent.putExtra("reportId", report.getId());
                startActivity(intent);
            }

            @Override
            public void onLikeClick(Report report, boolean isLiked) {
                // Optional: handle like
            }
        });
        reportsRecyclerView.setAdapter(reportAdapter);

        FirebaseUser currentUser = firebaseHelper.getCurrentUser(); // ✅ gunakan getter
        if (currentUser != null) {
            firebaseHelper.getUserData(currentUser.getUid(), new FirebaseHelper.UserCallback() {
                @Override
                public void onUserLoaded(User user) {
                    nameText.setText(user.getName());

                    if (user.isAdmin()) {
                        statusText.setText("Admin");
                        nimProdiLayout.setVisibility(View.GONE);
                    } else {
                        statusText.setText("Mahasiswa");
                        nimProdiLayout.setVisibility(View.VISIBLE);
                        nimText.setText(user.getNim());
                        prodiText.setText(user.getProgramStudi());
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });

            firebaseHelper.getReportsByUser(currentUser.getUid(), new FirebaseHelper.ReportsCallback() {
                @Override
                public void onReportsLoaded(List<Report> reports) {
                    reportAdapter.setReports(reports);
                }

                @Override
                public void onCancelled(String errorMessage) {
                    Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            firebaseHelper.getAuth().signOut();
            startActivity(new Intent(ProfileActivity.this, SignInActivity.class));
            finishAffinity();
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, DashboardActivity.class));
            finish();
        });

        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            // do nothing
        });

        findViewById(R.id.btnKirim).setOnClickListener(v -> {
            FirebaseUser user = firebaseHelper.getCurrentUser();
            if (user != null) {
                firebaseHelper.checkAdminStatus(user.getUid(), new FirebaseHelper.AdminCheckCallback() {
                    @Override
                    public void onAdminChecked(boolean isAdmin) {
                        if (!isAdmin) {
                            startActivity(new Intent(ProfileActivity.this, UploadActivity.class));
                        } else {
                            Toast.makeText(ProfileActivity.this, "Admin tidak bisa mengirim laporan", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
