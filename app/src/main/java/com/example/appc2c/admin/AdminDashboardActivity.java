package com.example.appc2c.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.example.appc2c.profile.ProfileActivity;
import com.example.appc2c.admin.ModerationActivity;
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
}
