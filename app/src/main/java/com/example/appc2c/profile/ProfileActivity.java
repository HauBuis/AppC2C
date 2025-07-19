package com.example.appc2c.profile;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appc2c.R;
import com.example.appc2c.login.LoginActivity;
import com.example.appc2c.products.MainActivity;
import com.example.appc2c.products.MyProductsActivity;
import com.example.appc2c.products.PostProductActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtName, txtEmail, txtPhone, txtAddress, txtBio, txtRating;
    private ImageView imgAvatar;
    private String avatarUriString = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Ánh xạ
        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtAddress = findViewById(R.id.txtAddress);
        txtBio = findViewById(R.id.txtBio);
        txtRating = findViewById(R.id.txtRating);
        imgAvatar = findViewById(R.id.imgAvatar);

        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnDeactivate = findViewById(R.id.btnDeactivate);
        Button btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        Button btnSold = findViewById(R.id.btnSold);
        Button btnPurchased = findViewById(R.id.btnPurchased);

        // Mở danh sách sản phẩm của tôi
        btnSold.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MyProductsActivity.class);
            startActivity(intent);
        });

        // Mở danh sách sản phẩm đã mua
        btnPurchased.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, PurchasedActivity.class);
            startActivity(intent);
        });

        // Chức năng chỉnh sửa và đăng xuất
        btnEdit.setOnClickListener(v -> openEditProfile());
        btnLogout.setOnClickListener(v -> logoutUser());

        // Bottom Nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_account);
        bottomNav.setOnItemSelectedListener(item -> handleNavigation(item.getItemId()));

        // Tùy chọn tài khoản
        btnDeactivate.setOnClickListener(v -> showDeactivateDialog());
        btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            txtName.setText(documentSnapshot.getString("name"));
                            txtEmail.setText("Email: " + documentSnapshot.getString("e-mail"));
                            txtPhone.setText("SĐT: " + documentSnapshot.getString("phone"));
                            txtAddress.setText("Địa chỉ: " + documentSnapshot.getString("address"));
                            txtBio.setText("Mô tả: " + documentSnapshot.getString("bio"));
                            txtRating.setText("Đánh giá: " + documentSnapshot.getString("rating"));
                            avatarUriString = documentSnapshot.getString("avatar");
                            if (avatarUriString != null && !avatarUriString.isEmpty()) {
                                Glide.with(this).load(Uri.parse(avatarUriString)).into(imgAvatar);
                            } else {
                                imgAvatar.setImageResource(R.drawable.ic_account);
                            }
                        }
                    });
        }
    }

    private void openEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra("name", txtName.getText().toString());
        intent.putExtra("email", txtEmail.getText().toString().replace("Email: ", ""));
        intent.putExtra("phone", txtPhone.getText().toString().replace("SĐT: ", ""));
        intent.putExtra("address", txtAddress.getText().toString().replace("Địa chỉ: ", ""));
        intent.putExtra("bio", txtBio.getText().toString().replace("Mô tả: ", ""));
        intent.putExtra("avatarUri", avatarUriString);
        startActivity(intent);
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private boolean handleNavigation(int id) {
        if (id == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        } else if (id == R.id.nav_post) {
            startActivity(new Intent(this, PostProductActivity.class));
            return true;
        } else if (id == R.id.nav_account) {
            return true;
        }
        return false;
    }

    private void showDeactivateDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Vô hiệu hóa tài khoản")
                .setMessage("Bạn có chắc muốn vô hiệu hóa tài khoản không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> deactivateAccount())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa tài khoản vĩnh viễn")
                .setMessage("Hành động này không thể hoàn tác. Bạn chắc chắn?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteAccount())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deactivateAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .update("active", false)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đã vô hiệu hóa tài khoản", Toast.LENGTH_SHORT).show();
                        logoutUser();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void deleteAccount() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.delete().addOnSuccessListener(unused -> {
                Toast.makeText(this, "Tài khoản đã bị xóa!", Toast.LENGTH_SHORT).show();
                logoutUser();
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Lỗi khi xoá: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
