package com.example.appc2c.profile;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appc2c.R;
import com.example.appc2c.dialogs.ReportDialog;
import com.example.appc2c.login.LoginActivity;
import com.example.appc2c.products.MainActivity;
import com.example.appc2c.products.PostProductActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtName, txtEmail, txtPhone, txtAddress, txtBio, txtRating;
    private ImageView imgAvatar;
    private static final int PICK_IMAGE_AVATAR = 2001;
    private static final int REQUEST_EDIT_PROFILE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ các view
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtAddress = findViewById(R.id.txtAddress);
        txtBio = findViewById(R.id.txtBio);
        txtRating = findViewById(R.id.txtRating);
        imgAvatar = findViewById(R.id.imgAvatar);

        Button btnDeactivate = findViewById(R.id.btnDeactivate);
        Button btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnReportUser = findViewById(R.id.btnReportUser);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnPurchased = findViewById(R.id.btnPurchased);
        Button btnSold = findViewById(R.id.btnSold);

        loadRatingData();

        String viewedUserId = getIntent().getStringExtra("userId");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        if (viewedUserId != null && !viewedUserId.equals(currentUserId)) {
            btnEdit.setVisibility(Button.GONE);
            btnLogout.setVisibility(Button.GONE);
            btnDeactivate.setVisibility(Button.GONE);
            btnDeleteAccount.setVisibility(Button.GONE);
            btnReportUser.setVisibility(Button.VISIBLE);
            btnPurchased.setVisibility(Button.GONE);
            btnSold.setVisibility(Button.GONE);

            btnReportUser.setOnClickListener(v -> {
                ReportDialog dialog = ReportDialog.newInstance(viewedUserId, "user");
                dialog.show(getSupportFragmentManager(), "reportUserDialog");
            });
        }

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            intent.putExtra("name", txtName.getText().toString());
            intent.putExtra("email", txtEmail.getText().toString().replace("Email: ", ""));
            intent.putExtra("phone", txtPhone.getText().toString().replace("SĐT: ", ""));
            intent.putExtra("address", txtAddress.getText().toString().replace("Địa chỉ: ", ""));
            intent.putExtra("bio", txtBio.getText().toString().replace("Mô tả: ", ""));
            intent.putExtra("rating", txtRating.getText().toString().replace("Đánh giá: ", "").replace(" ★", ""));
            startActivityForResult(intent, REQUEST_EDIT_PROFILE);
        });

        imgAvatar.setOnClickListener(v -> openGallery());

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_account);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (id == R.id.nav_post) {
                startActivity(new Intent(this, PostProductActivity.class));
                return true;
            }
            return true;
        });

        // Nút vô hiệu hóa tài khoản
        btnDeactivate.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Vô hiệu hóa tài khoản")
                .setMessage("Bạn có chắc muốn vô hiệu hóa tài khoản không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .update("active", false)
                            .addOnSuccessListener(unused -> {
                                FirebaseAuth.getInstance().signOut();
                                Toast.makeText(this, "Đã vô hiệu hóa tài khoản", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Lỗi khi vô hiệu hóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show());

        // Nút xoá tài khoản
        btnDeleteAccount.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Xóa tài khoản vĩnh viễn")
                .setMessage("Hành động này không thể hoàn tác. Bạn chắc chắn?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        user.delete().addOnSuccessListener(unused -> {
                            FirebaseFirestore.getInstance().collection("users").document(uid).delete();
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(this, "Tài khoản đã bị xóa!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        }).addOnFailureListener(e ->
                                Toast.makeText(this, "Lỗi khi xoá: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Hủy", null)
                .show());

        // Xử lý 2 nút chuyển trang
        btnPurchased.setOnClickListener(v -> {
            startActivity(new Intent(this, PurchasedActivity.class));
        });

        btnSold.setOnClickListener(v -> {
            startActivity(new Intent(this, SoldActivity.class));
        });
        Button btnReceivedOffers = findViewById(R.id.btnReceivedOffers);

        // Mặc định ẩn nếu không phải người bán
        btnReceivedOffers.setVisibility(Button.GONE);

        // Kiểm tra nếu là người bán thì hiển thị
        FirebaseFirestore.getInstance().collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        String role = snapshot.getString("role");
                        if ("seller".equals(role)) {
                            btnReceivedOffers.setVisibility(Button.VISIBLE);
                            btnReceivedOffers.setOnClickListener(v -> {
                                startActivity(new Intent(this, com.example.appc2c.models.OfferListActivity.class));
                            });
                        }
                    }
                });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_AVATAR);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_EDIT_PROFILE && resultCode == RESULT_OK && data != null) {
            txtName.setText(data.getStringExtra("name"));
            txtEmail.setText("Email: " + data.getStringExtra("email"));
            txtPhone.setText("SĐT: " + data.getStringExtra("phone"));
            txtAddress.setText("Địa chỉ: " + data.getStringExtra("address"));
            txtBio.setText("Mô tả: " + data.getStringExtra("bio"));
            txtRating.setText("Đánh giá: " + data.getStringExtra("rating") + " ★");
        }

        if (requestCode == PICK_IMAGE_AVATAR && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri avatarUri = data.getData();
            Glide.with(this).load(avatarUri).into(imgAvatar);
        }
    }

    private void loadRatingData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        FirebaseFirestore.getInstance()
                .collection("ratings")
                .whereEqualTo("toUserId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalRating = 0;
                    float sum = 0f;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Long r = doc.getLong("rating");
                        if (r != null) {
                            sum += r;
                            totalRating++;
                        }
                    }

                    float avg = totalRating > 0 ? (sum / totalRating) : 0;
                    txtRating.setText(String.format("⭐ %.1f (%d đánh giá)", avg, totalRating));
                })
                .addOnFailureListener(e ->
                        txtRating.setText("⭐ 0.0 (0 đánh giá)"));
    }
}
