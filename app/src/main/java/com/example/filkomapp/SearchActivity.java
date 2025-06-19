package com.example.filkomapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText searchEditText;
    private ImageView clearSearchIcon;
    private RecyclerView searchResultsRecyclerView;
    private ReportAdapter reportAdapter;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchEditText = findViewById(R.id.editJudul);
        clearSearchIcon = findViewById(R.id.deleteIcon);
        searchResultsRecyclerView = findViewById(R.id.recyclerViewNews);
        firebaseHelper = new FirebaseHelper(this);


        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reportAdapter = new ReportAdapter(this, new ReportAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Report report) {
                Intent intent = new Intent(SearchActivity.this, ReportDetailActivity.class);
                intent.putExtra("reportId", report.getId());
                startActivity(intent);
            }

            @Override
            public void onLikeClick(Report report, boolean isLiked) {
                firebaseHelper.toggleLikeReport(report.getId(), firebaseHelper.getCurrentUser().getUid(), new FirebaseHelper.LikeCallback() {
                    @Override
                    public void onSuccess(boolean isLiked) {

                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(SearchActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        searchResultsRecyclerView.setAdapter(reportAdapter);


        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    clearSearchIcon.setVisibility(View.VISIBLE);
                    firebaseHelper.searchReports(s.toString(), new FirebaseHelper.ReportsCallback() {
                        @Override
                        public void onReportsLoaded(List<Report> reports) {
                            reportAdapter.setReports(reports);
                        }

                        @Override
                        public void onCancelled(String errorMessage) {
                            Toast.makeText(SearchActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    clearSearchIcon.setVisibility(View.GONE);
                    reportAdapter.setReports(new ArrayList<>());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        clearSearchIcon.setOnClickListener(v -> searchEditText.setText(""));


        findViewById(R.id.btnBack).setOnClickListener(v -> finish());


        findViewById(R.id.btnHome).setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, DashboardActivity.class));
            finish();
        });

        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            startActivity(new Intent(SearchActivity.this, ProfileActivity.class));
        });

        findViewById(R.id.btnKirim).setOnClickListener(v -> {
            FirebaseUser currentUser = firebaseHelper.getCurrentUser();
            if (currentUser != null) {
                firebaseHelper.checkAdminStatus(currentUser.getUid(), new FirebaseHelper.AdminCheckCallback() {
                    @Override
                    public void onAdminChecked(boolean isAdmin) {
                        if (!isAdmin) {
                            startActivity(new Intent(SearchActivity.this, UploadActivity.class));
                        } else {
                            Toast.makeText(SearchActivity.this, "Admin tidak bisa mengirim laporan", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(SearchActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
