package com.example.filkomapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.graphics.Bitmap;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 100;

    private TextView nameText, statusText, fixedHistoryTitle;
    private EditText nimText, prodiText;
    private LinearLayout nimProdiLayout;
    private RecyclerView reportsRecyclerView, fixedReportsRecyclerView;
    private ReportAdapter reportAdapter, fixedReportAdapter;
    private FirebaseHelper firebaseHelper;
    private ImageView profileImage;
    private View btnKirim;
    private TextView historyTitle;
    private Uri imageUri;
    private boolean nimSaved = false;
    private boolean prodiSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        historyTitle = findViewById(R.id.historyTitle);
        nameText = findViewById(R.id.name);
        statusText = findViewById(R.id.status);
        nimProdiLayout = findViewById(R.id.infoNimProdi);
        nimText = findViewById(R.id.nim);
        prodiText = findViewById(R.id.prodi);
        reportsRecyclerView = findViewById(R.id.recyclerViewNews);
        profileImage = findViewById(R.id.profileIcon);
        btnKirim = findViewById(R.id.btnKirim);

        fixedHistoryTitle = findViewById(R.id.fixedHistoryTitle);
        fixedReportsRecyclerView = findViewById(R.id.fixedReportsRecyclerView);

        firebaseHelper = new FirebaseHelper(this);

        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fixedReportsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        reportAdapter = new ReportAdapter(this, new ReportAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Report report) {
                Intent intent = new Intent(ProfileActivity.this, ReportDetailActivity.class);
                intent.putExtra("reportId", report.getId());
                startActivity(intent);
            }

            @Override
            public void onLikeClick(Report report, boolean isLiked) {
                // No-op in profile screen
            }
        });
        reportsRecyclerView.setAdapter(reportAdapter);

        fixedReportAdapter = new ReportAdapter(this, new ReportAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Report report) {
                Intent intent = new Intent(ProfileActivity.this, ReportDetailActivity.class);
                intent.putExtra("reportId", report.getId());
                startActivity(intent);
            }

            @Override
            public void onLikeClick(Report report, boolean isLiked) {
                // No-op in profile screen
            }
        });
        fixedReportsRecyclerView.setAdapter(fixedReportAdapter);

        loadUserData();

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
        findViewById(R.id.btnProfile).setOnClickListener(v -> {});

        btnKirim.setOnClickListener(v -> {
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

        profileImage.setOnClickListener(v -> checkPermissionAndSelectImage());
    }

    private void loadUserData() {
        FirebaseUser currentUser = firebaseHelper.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();

        firebaseHelper.getUserData(uid, new FirebaseHelper.UserCallback() {
            @Override
            public void onUserLoaded(User user) {
                nameText.setText(user.getName());

                Glide.with(ProfileActivity.this)
                        .load(user.getProfileUrl())
                        .placeholder(R.drawable.profile)
                        .into(profileImage);

                firebaseHelper.checkAdminStatus(uid, new FirebaseHelper.AdminCheckCallback() {
                    @Override
                    public void onAdminChecked(boolean isAdmin) {
                        if (isAdmin) {
                            statusText.setText("Admin");
                            nimProdiLayout.setVisibility(View.GONE);
                            btnKirim.setVisibility(View.GONE);
                            fixedHistoryTitle.setVisibility(View.VISIBLE);
                            fixedReportsRecyclerView.setVisibility(View.VISIBLE);
                            reportsRecyclerView.setVisibility(View.GONE);
                            historyTitle.setVisibility(View.GONE);
                            firebaseHelper.getFixedReportsByAdmin(uid, new FirebaseHelper.ReportsCallback() {
                                @Override
                                public void onReportsLoaded(List<Report> reports) {
                                    fixedReportAdapter.setReports(reports);
                                }

                                @Override
                                public void onCancelled(String errorMessage) {
                                    Toast.makeText(ProfileActivity.this, "Gagal memuat riwayat fixed: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            statusText.setText("Mahasiswa");
                            nimProdiLayout.setVisibility(View.VISIBLE);
                            btnKirim.setVisibility(View.VISIBLE);
                            fixedHistoryTitle.setVisibility(View.GONE);
                            fixedReportsRecyclerView.setVisibility(View.GONE);
                            reportsRecyclerView.setVisibility(View.VISIBLE);

                            if (user.getNim() != null && !user.getNim().isEmpty()) {
                                nimText.setText(user.getNim());
                                nimText.setEnabled(false);
                                nimSaved = true;
                            }

                            if (user.getProgramStudi() != null && !user.getProgramStudi().isEmpty()) {
                                prodiText.setText(user.getProgramStudi());
                                prodiText.setEnabled(false);
                                prodiSaved = true;
                            }

                            nimText.setOnFocusChangeListener((v, hasFocus) -> {
                                if (!hasFocus && !nimSaved) {
                                    String nimBaru = nimText.getText().toString().trim();
                                    if (!nimBaru.isEmpty()) {
                                        user.setNim(nimBaru);
                                        firebaseHelper.saveUserData(user);
                                        nimText.setEnabled(false);
                                        nimSaved = true;
                                        Toast.makeText(ProfileActivity.this, "NIM disimpan", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            prodiText.setOnFocusChangeListener((v, hasFocus) -> {
                                if (!hasFocus && !prodiSaved) {
                                    String prodiBaru = prodiText.getText().toString().trim();
                                    if (!prodiBaru.isEmpty()) {
                                        user.setProgramStudi(prodiBaru);
                                        firebaseHelper.saveUserData(user);
                                        prodiText.setEnabled(false);
                                        prodiSaved = true;
                                        Toast.makeText(ProfileActivity.this, "Program Studi disimpan", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            firebaseHelper.getReportsByUser(uid, new FirebaseHelper.ReportsCallback() {
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
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(ProfileActivity.this, "Gagal cek status admin: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ProfileActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkPermissionAndSelectImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                selectProfilePicture();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                selectProfilePicture();
            }
        }
    }

    private void selectProfilePicture() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            uploadProfileImage(imageUri);
        }
    }

    private void uploadProfileImage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            FirebaseUser user = firebaseHelper.getCurrentUser();
            if (user != null) {
                firebaseHelper.saveProfileImageBase64(user.getUid(), base64Image);
                Toast.makeText(this, "Foto berhasil disimpan", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "User tidak ditemukan", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Gagal konversi gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectProfilePicture();
            } else {
                Toast.makeText(this, "Izin akses galeri ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }
}