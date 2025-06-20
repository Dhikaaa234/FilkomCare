package com.example.filkomapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private RecyclerView notificationsRecyclerView;
    private NotificationAdapter notificationAdapter;
    private FirebaseHelper firebaseHelper;
    private View btnKirim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifikasi);

        notificationsRecyclerView = findViewById(R.id.recyclerViewNotifikasi);
        btnKirim = findViewById(R.id.btnKirim);
        firebaseHelper = new FirebaseHelper(this);

        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationAdapter = new NotificationAdapter(this, new NotificationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Notification notification) {
                if (notification.getReportId() != null && !notification.getReportId().isEmpty()) {
                    Intent intent = new Intent(NotificationActivity.this, ReportDetailActivity.class);
                    intent.putExtra("reportId", notification.getReportId());
                    startActivity(intent);
                }
            }

            @Override
            public void onDeleteClick(Notification notification) {
                firebaseHelper.getDatabase().child("notifications").child(notification.getId()).removeValue();
            }
        });
        notificationsRecyclerView.setAdapter(notificationAdapter);

        FirebaseUser currentUser = firebaseHelper.getCurrentUser();
        if (currentUser != null) {
            firebaseHelper.getDatabase().child("notifications")
                    .orderByChild("userId")
                    .equalTo(currentUser.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            List<Notification> notifications = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Notification notification = snapshot.getValue(Notification.class);
                                notification.setId(snapshot.getKey());
                                notifications.add(notification);
                            }
                            notificationAdapter.setNotifications(notifications);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(NotificationActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

            firebaseHelper.checkAdminStatus(currentUser.getUid(), new FirebaseHelper.AdminCheckCallback() {
                @Override
                public void onAdminChecked(boolean isAdmin) {
                    if (isAdmin) {
                        btnKirim.setVisibility(View.GONE);
                    } else {
                        btnKirim.setOnClickListener(v -> {
                            startActivity(new Intent(NotificationActivity.this, UploadActivity.class));
                        });
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(NotificationActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            startActivity(new Intent(NotificationActivity.this, DashboardActivity.class));
            finish();
        });

        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            startActivity(new Intent(NotificationActivity.this, ProfileActivity.class));
        });
    }
}
