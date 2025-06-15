package com.example.filkomapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;

import java.io.IOException;

public class UploadActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText titleEditText, locationEditText, descriptionEditText;
    private ImageView uploadImageView;
    private Bitmap selectedImageBitmap;
    private FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        titleEditText = findViewById(R.id.editJudul);
        locationEditText = findViewById(R.id.editRuangTempat);
        descriptionEditText = findViewById(R.id.editDeskripsi);
        uploadImageView = findViewById(R.id.uploadImageView);
        firebaseHelper = new FirebaseHelper(this);

        // Klik upload gambar
        uploadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // Tombol Kirim
        findViewById(R.id.btnKirim).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitReport();
            }
        });

        // Tombol Back
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Pilih Gambar"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                uploadImageView.setImageBitmap(selectedImageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void submitReport() {
        String title = titleEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        if (title.isEmpty() || location.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Harap isi semua data", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedImageBitmap == null) {
            Toast.makeText(this, "Silakan pilih gambar", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageBase64 = FirebaseHelper.bitmapToBase64(selectedImageBitmap);

        firebaseHelper.submitReport(title, location, description, imageBase64, new FirebaseHelper.ReportCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(UploadActivity.this, "Laporan berhasil dikirim", Toast.LENGTH_SHORT).show();
                createReportNotification(title);
                finish();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(UploadActivity.this, "Gagal kirim laporan: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createReportNotification(String reportTitle) {
        DatabaseReference notifRef = firebaseHelper.getDatabase().child("notifications").push();
        String notifId = notifRef.getKey();

        String title = "Laporan Berhasil Dikirim";
        String message = "Laporan Anda \"" + reportTitle + "\" telah berhasil dikirim";

        Notification notification = new Notification(notifId, title, message, "", firebaseHelper.getCurrentUser().getUid(), System.currentTimeMillis(), false);
        firebaseHelper.getDatabase().child("notifications").child(notifId).setValue(notification);
    }
}
