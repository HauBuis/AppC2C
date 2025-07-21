package com.example.appc2c.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appc2c.R;
import com.example.appc2c.models.Warning;
import com.example.appc2c.products.Product;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminProductDetailActivity extends AppCompatActivity {

    private TextView txtProductName, txtProductDesc, txtProductPrice, txtCategoryCondition;
    private Button btnDeleteProduct, btnSuspendProduct, btnWarnSeller;

    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_product_detail);

        initViews();
        loadProductFromIntent();
        setupButtonActions();
    }

    private void initViews() {
        txtProductName = findViewById(R.id.txtProductName);
        txtProductDesc = findViewById(R.id.txtProductDesc);
        txtProductPrice = findViewById(R.id.txtProductPrice);
        txtCategoryCondition = findViewById(R.id.txtCategoryCondition);
        btnDeleteProduct = findViewById(R.id.btnDeleteProduct);
        btnSuspendProduct = findViewById(R.id.btnSuspendProduct);
        btnWarnSeller = findViewById(R.id.btnWarnSeller);
    }

    @SuppressLint("SetTextI18n")
    private void loadProductFromIntent() {
        product = (Product) getIntent().getSerializableExtra("product");

        if (product != null) {
            txtProductName.setText(product.getName());
            txtProductDesc.setText(product.getDescription());
            txtProductPrice.setText(product.getPrice() + " đ");
            txtCategoryCondition.setText(product.getCategory() + " - " + product.getCondition());
        } else {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupButtonActions() {
        btnDeleteProduct.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa sản phẩm này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteProduct())
                .setNegativeButton("Hủy", null)
                .show());

        btnSuspendProduct.setOnClickListener(v -> suspendProduct());

        btnWarnSeller.setOnClickListener(v -> warnSeller());
    }

    private void deleteProduct() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products").child(product.getId());
        ref.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void suspendProduct() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products").child(product.getId());
        ref.child("active").setValue(false)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã tạm ngưng sản phẩm", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tạm ngưng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void warnSeller() {
        if (product == null || product.getSellerId() == null) {
            Toast.makeText(this, "Không xác định được người bán", Toast.LENGTH_SHORT).show();
            return;
        }

        String sellerId = product.getSellerId();
        DatabaseReference warningsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(sellerId)
                .child("warnings");

        String warningId = warningsRef.push().getKey();
        if (warningId == null) return;

        // Dùng constructor với timeMillis cho Realtime Database
        Warning warning = new Warning(
                warningId,
                "Sản phẩm '" + product.getName() + "' bị cảnh báo bởi quản trị viên.",
                System.currentTimeMillis() // Lưu vào trường timeMillis
        );

        warningsRef.child(warningId).setValue(warning)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã gửi cảnh báo đến người bán", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi gửi cảnh báo: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
