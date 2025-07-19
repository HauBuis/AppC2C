package com.example.appc2c.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.example.appc2c.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerReports;
    private ReportAdapter reportAdapter;
    private final List<Report> reportList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        loadReports();
        findViewById(R.id.btnViewWarnings).setOnClickListener(v -> {
            startActivity(new Intent(this, WarningListActivity.class));
        });
        setupBottomNav();
    }

    private void initViews() {
        recyclerReports = findViewById(R.id.recyclerReports);
        recyclerReports.setLayoutManager(new LinearLayoutManager(this));
        reportAdapter = new ReportAdapter(this, reportList);
        recyclerReports.setAdapter(reportAdapter);

        reportAdapter.setOnActionListener(report -> {
            showAdminActionDialog(report);
        });
    }

    private void loadReports() {
        FirebaseFirestore.getInstance()
                .collection("reports")
                .get()
                .addOnSuccessListener(query -> {
                    reportList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Report report = doc.toObject(Report.class);
                        report.setId(doc.getId());
                        reportList.add(report);
                    }
                    reportAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải danh sách báo cáo", Toast.LENGTH_SHORT).show());

    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNav);
        bottomNav.setSelectedItemId(R.id.nav_admin_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_admin_home) {
                return true;
            } else if (id == R.id.nav_admin_manage) {
                startActivity(new Intent(this, ModerationActivity.class));
                return true;
            } else if (id == R.id.nav_admin_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }

            return false;
        });
    }

    private void showAdminActionDialog(@NonNull Report report) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hành động quản trị")
                .setItems(new String[]{"Cảnh báo", "Tạm ngưng", "Xóa nội dung"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            updateUserStatus(report.getTargetId(), "warned");
                            break;
                        case 1:
                            updateUserStatus(report.getTargetId(), "suspended");
                            break;
                        case 2:
                            deleteContent(report.getTargetId());
                            break;
                    }
                })
                .show();
    }

    private void updateUserStatus(String userId, String status) {
        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .update("status", status)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Đã cập nhật trạng thái người dùng", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void deleteContent(String contentId) {
        FirebaseFirestore.getInstance().collection("products")
                .document(contentId)
                .delete()
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Đã xóa nội dung", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi khi xóa nội dung: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
