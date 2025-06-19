package com.example.filkomapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class DashboardActivity extends AppCompatActivity {
    private TextView greetingText, nameText;
    private RecyclerView recyclerView;
    private ReportAdapter reportAdapter;
    private FirebaseHelper firebaseHelper;
    private View btnKirim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        greetingText = findViewById(R.id.greeting_text);
        nameText = findViewById(R.id.name_text);
        recyclerView = findViewById(R.id.recyclerViewNews);
        btnKirim = findViewById(R.id.btnKirim);
        firebaseHelper = new FirebaseHelper(this);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportAdapter = new ReportAdapter(this, new ReportAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Report report) {
                Intent intent = new Intent(DashboardActivity.this, ReportDetailActivity.class);
                intent.putExtra("reportId", report.getId());
                startActivity(intent);
            }

            @Override
            public void onLikeClick(Report report, boolean isLiked) {
                FirebaseUser user = firebaseHelper.getCurrentUser();
                if (user != null) {
                    firebaseHelper.toggleLikeReport(report.getId(), user.getUid(), new FirebaseHelper.LikeCallback() {
                        @Override
                        public void onSuccess(boolean isLiked) {

                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Toast.makeText(DashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        recyclerView.setAdapter(reportAdapter);


        FirebaseUser currentUser = firebaseHelper.getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();


            firebaseHelper.getUserData(uid, new FirebaseHelper.UserCallback() {
                @Override
                public void onUserLoaded(User user) {
                    greetingText.setText("Halo,");
                    nameText.setText(user.getName());
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(DashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });


            firebaseHelper.checkAdminStatus(uid, new FirebaseHelper.AdminCheckCallback() {
                @Override
                public void onAdminChecked(boolean isAdmin) {
                    if (isAdmin) {
                        btnKirim.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(DashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }


        firebaseHelper.getAllReports(new FirebaseHelper.ReportsCallback() {
            @Override
            public void onReportsLoaded(List<Report> reports) {
                reportAdapter.setReports(reports);
            }

            @Override
            public void onCancelled(String errorMessage) {
                Toast.makeText(DashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });


        findViewById(R.id.btnHome).setOnClickListener(v -> {
            firebaseHelper.getAllReports(new FirebaseHelper.ReportsCallback() {
                @Override
                public void onReportsLoaded(List<Report> reports) {
                    reportAdapter.setReports(reports);
                }

                @Override
                public void onCancelled(String errorMessage) {
                    Toast.makeText(DashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        findViewById(R.id.btnProfile).setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class)));

        findViewById(R.id.search_icon).setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, SearchActivity.class)));

        findViewById(R.id.notification_icon).setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, NotificationActivity.class)));

        btnKirim.setOnClickListener(v -> {
            FirebaseUser user = firebaseHelper.getCurrentUser();
            if (user != null) {
                firebaseHelper.checkAdminStatus(user.getUid(), new FirebaseHelper.AdminCheckCallback() {
                    @Override
                    public void onAdminChecked(boolean isAdmin) {
                        if (!isAdmin) {
                            startActivity(new Intent(DashboardActivity.this, UploadActivity.class));
                        } else {
                            Toast.makeText(DashboardActivity.this, "Admin tidak bisa mengirim laporan", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(DashboardActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
