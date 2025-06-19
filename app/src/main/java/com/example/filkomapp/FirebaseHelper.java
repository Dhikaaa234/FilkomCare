package com.example.filkomapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.io.ByteArrayOutputStream;
import java.util.*;

public class FirebaseHelper {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private Context context;

    public FirebaseHelper(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public DatabaseReference getDatabase() {
        return mDatabase;
    }

    public FirebaseAuth getAuth() {
        return mAuth;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static Bitmap base64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void signInUser(String email, String password, final AuthCallback callback) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(getCurrentUser());
                    } else {
                        callback.onFailure(task.getException().getMessage());
                    }
                });
    }

    public void registerUser(String name, String email, String password, final AuthCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = getCurrentUser();
                        if (user != null) {
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("email", email);
                            userMap.put("isAdmin", false);
                            mDatabase.child("users").child(user.getUid()).setValue(userMap)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            callback.onSuccess(user);
                                        } else {
                                            callback.onFailure(task1.getException().getMessage());
                                        }
                                    });
                        }
                    } else {
                        callback.onFailure(task.getException().getMessage());
                    }
                });
    }

    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    public void submitReport(String title, String location, String description, String imageBase64, final ReportCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            String reportId = mDatabase.child("reports").push().getKey();
            Map<String, Object> reportMap = new HashMap<>();
            reportMap.put("title", title);
            reportMap.put("location", location);
            reportMap.put("description", description);
            reportMap.put("image", imageBase64);
            reportMap.put("userId", user.getUid());
            reportMap.put("userName", user.getDisplayName());
            reportMap.put("timestamp", System.currentTimeMillis());
            reportMap.put("status", "New");
            reportMap.put("likes", 0);
            reportMap.put("likedBy", new HashMap<String, Boolean>());

            mDatabase.child("reports").child(reportId).setValue(reportMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure(task.getException().getMessage());
                        }
                    });
        }
    }

    public interface ReportCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void getAllReports(final ReportsCallback callback) {
        mDatabase.child("reports").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Report> reports = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Report report = snap.getValue(Report.class);
                    if (report != null) {
                        report.setId(snap.getKey());
                        reports.add(report);
                    }
                }
                callback.onReportsLoaded(reports);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCancelled(error.getMessage());
            }
        });
    }

    public interface ReportsCallback {
        void onReportsLoaded(List<Report> reports);
        void onCancelled(String errorMessage);
    }

    public void toggleLikeReport(String reportId, String userId, final LikeCallback callback) {
        DatabaseReference reportRef = mDatabase.child("reports").child(reportId);
        reportRef.child("likedBy").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    reportRef.child("likedBy").child(userId).removeValue();
                    reportRef.child("likes").setValue(ServerValue.increment(-1))
                            .addOnCompleteListener(task -> callback.onSuccess(false));
                } else {
                    reportRef.child("likedBy").child(userId).setValue(true);
                    reportRef.child("likes").setValue(ServerValue.increment(1))
                            .addOnCompleteListener(task -> callback.onSuccess(true));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    public interface LikeCallback {
        void onSuccess(boolean isLiked);
        void onFailure(String errorMessage);
    }

    public void updateReportStatus(String reportId, String status, final StatusCallback callback) {
        DatabaseReference reportRef = mDatabase.child("reports").child(reportId);

        reportRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", status);

                if ("fixed".equalsIgnoreCase(status)) {
                    String currentUid = getCurrentUserId();
                    if (snapshot.child("fixedBy").getValue() == null) {
                        updates.put("fixedBy", currentUid);
                    }
                }

                reportRef.updateChildren(updates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                callback.onSuccess();
                            } else {
                                callback.onFailure(task.getException().getMessage());
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    public interface StatusCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void searchReports(String query, final ReportsCallback callback) {
        mDatabase.child("reports").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Report> results = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Report report = snap.getValue(Report.class);
                    if (report != null && (
                            report.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                                    report.getLocation().toLowerCase().contains(query.toLowerCase()) ||
                                    report.getDescription().toLowerCase().contains(query.toLowerCase()))) {
                        report.setId(snap.getKey());
                        results.add(report);
                    }
                }
                callback.onReportsLoaded(results);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCancelled(error.getMessage());
            }
        });
    }

    public void getUserData(String userId, final UserCallback callback) {
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    user.setId(snapshot.getKey());
                    callback.onUserLoaded(user);
                } else {
                    callback.onFailure("User data not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onFailure(error.getMessage());
            }
        });
    }

    public interface UserCallback {
        void onUserLoaded(User user);
        void onFailure(String errorMessage);
    }

    public void getReportsByUser(String userId, final ReportsCallback callback) {
        mDatabase.child("reports").orderByChild("userId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Report> reports = new ArrayList<>();
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            Report report = snap.getValue(Report.class);
                            if (report != null) {
                                report.setId(snap.getKey());
                                reports.add(report);
                            }
                        }
                        callback.onReportsLoaded(reports);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onCancelled(error.getMessage());
                    }
                });
    }

    public void checkAdminStatus(String userId, final AdminCheckCallback callback) {
        mDatabase.child("users").child(userId).child("isAdmin")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Boolean isAdmin = snapshot.getValue(Boolean.class);
                        callback.onAdminChecked(isAdmin != null && isAdmin);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    public interface AdminCheckCallback {
        void onAdminChecked(boolean isAdmin);
        void onFailure(String errorMessage);
    }

    public void saveUserData(User user) {
        if (user != null && user.getId() != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", user.getName());
            updates.put("email", user.getEmail());
            updates.put("isAdmin", user.isAdmin());
            updates.put("nim", user.getNim());
            updates.put("programStudi", user.getProgramStudi());

            mDatabase.child("users").child(user.getId()).updateChildren(updates);
        }
    }

    public void saveProfileImageUrl(String uid, String url) {
        if (uid == null || url == null) {
            Toast.makeText(context, "UID atau URL kosong", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabase.child("users").child(uid).child("profileUrl").setValue(url)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "URL foto profil berhasil disimpan", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Gagal simpan URL foto: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void saveProfileImageBase64(String uid, String base64Image) {
        if (uid == null || base64Image == null || base64Image.isEmpty()) {
            Toast.makeText(context, "UID atau gambar kosong", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabase.child("users").child(uid).child("profileBase64").setValue(base64Image)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Foto profil berhasil disimpan", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Gagal simpan foto profil: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void getFixedReportsByAdmin(String uid, ReportsCallback callback) {
        mDatabase.child("reports")
                .orderByChild("fixedBy")
                .equalTo(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Report> result = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Report report = child.getValue(Report.class);
                            if (report != null && "fixed".equalsIgnoreCase(report.getStatus())) {
                                result.add(report);
                            }
                        }
                        callback.onReportsLoaded(result);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onCancelled(error.getMessage());
                    }
                });
    }
}
