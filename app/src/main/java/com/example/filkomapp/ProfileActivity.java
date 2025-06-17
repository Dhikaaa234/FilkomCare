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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 100;

    private TextView nameText, statusText;
    private EditText nimText, prodiText;
    private LinearLayout nimProdiLayout;
    private RecyclerView reportsRecyclerView;
    private ReportAdapter reportAdapter;
    private FirebaseHelper firebaseHelper;
    private ImageView profileImage;

    private Uri imageUri;
    private boolean nimSaved = false;
    private boolean prodiSaved = false;

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
        profileImage = findViewById(R.id.profileIcon);

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
                // Optional
            }
        });
        reportsRecyclerView.setAdapter(reportAdapter);

        // Load user data & reports
        loadUserData();

        // Logout button
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            firebaseHelper.getAuth().signOut();
            startActivity(new Intent(ProfileActivity.this, SignInActivity.class));
            finishAffinity();
        });

        // Navigation buttons
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, DashboardActivity.class));
            finish();
        });
        findViewById(R.id.btnProfile).setOnClickListener(v -> {});

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

        // Click profile image -> select picture
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
                            nimProdiLayout.setVisibility(LinearLayout.GONE);
                        } else {
                            statusText.setText("Mahasiswa");
                            nimProdiLayout.setVisibility(LinearLayout.VISIBLE);

                            if (user.getNim() != null && !user.getNim().isEmpty()) {
                                nimText.setText(user.getNim());
                                nimText.setEnabled(false);
                                nimText.setHint("NIM berhasil disimpan");
                                nimSaved = true;
                            } else {
                                nimText.setHint("Masukkan NIM");
                            }

                            if (user.getProgramStudi() != null && !user.getProgramStudi().isEmpty()) {
                                prodiText.setText(user.getProgramStudi());
                                prodiText.setEnabled(false);
                                prodiText.setHint("Prodi berhasil disimpan");
                                prodiSaved = true;
                            } else {
                                prodiText.setHint("Masukkan Prodi");
                            }

                            nimText.setOnFocusChangeListener((v, hasFocus) -> {
                                if (!hasFocus && !nimSaved) {
                                    String nimBaru = nimText.getText().toString().trim();
                                    if (!nimBaru.isEmpty()) {
                                        user.setNim(nimBaru);
                                        firebaseHelper.saveUserData(user);
                                        nimText.setEnabled(false);
                                        nimText.setHint("NIM berhasil disimpan");
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
                                        prodiText.setHint("Prodi berhasil disimpan");
                                        prodiSaved = true;
                                        Toast.makeText(ProfileActivity.this, "Program Studi disimpan", Toast.LENGTH_SHORT).show();
                                    }
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

    // Cek permission lalu mulai pilih foto
    private void checkPermissionAndSelectImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ gunakan READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                selectProfilePicture();
            }
        } else {
            // Android sebelum 13 gunakan READ_EXTERNAL_STORAGE
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
        FirebaseUser user = firebaseHelper.getCurrentUser();
        if (user == null || uri == null) return;

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_images/" + user.getUid() + ".jpg");
        storageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(downloadUri -> {
                            firebaseHelper.saveProfileImageUrl(user.getUid(), downloadUri.toString());
                            Toast.makeText(ProfileActivity.this, "Foto berhasil diunggah", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Gagal upload foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Tangani hasil permintaan permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
