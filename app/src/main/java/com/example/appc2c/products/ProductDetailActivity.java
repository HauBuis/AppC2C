package com.example.appc2c.products;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.appc2c.R;
import com.example.appc2c.dialogs.ReportDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class ProductDetailActivity extends AppCompatActivity {
    private ViewPager2 imageSlider;
    private TextView tvName, tvPrice, tvDesc, tvCategory, tvCondition;
    private Button btnMakeOffer, btnReport, btnEdit, btnDelete;
    private ImageSliderAdapter imageSliderAdapter;
    private String productId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        initViews();

        // Đóng activity khi click topAppBar
        findViewById(R.id.topAppBar).setOnClickListener(v -> finish());

        // Lấy id sản phẩm từ Intent
        productId = getIntent().getStringExtra("productId");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProductDetails();
    }

    private void initViews() {
        imageSlider = findViewById(R.id.imageSlider);
        imageSliderAdapter = new ImageSliderAdapter(this, new ArrayList<>());
        imageSlider.setAdapter(imageSliderAdapter);

        tvName = findViewById(R.id.txtDetailName);
        tvPrice = findViewById(R.id.txtDetailPrice);
        tvDesc = findViewById(R.id.txtDetailDesc);
        tvCategory = findViewById(R.id.txtDetailCategory);
        tvCondition = findViewById(R.id.txtDetailCondition);

        btnMakeOffer = findViewById(R.id.btnMakeOffer);
        btnReport = findViewById(R.id.btnReport);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        btnMakeOffer.setFocusable(true);
        btnMakeOffer.setClickable(true);
        btnReport.setFocusable(true);
        btnReport.setClickable(true);
    }

    @SuppressLint("DefaultLocale")
    private String formatCurrency(String priceStr) {
        try {
            long price = Long.parseLong(priceStr.replace(",", ""));
            return String.format("%,d", price).replace(",", ".") + " đ";
        } catch (Exception e) {
            return priceStr + " đ";
        }
    }

    private void setLabelledText(TextView textView, String label, String content) {
        String fullText = label + content;
        SpannableString spannable = new SpannableString(fullText);
        spannable.setSpan(new StyleSpan(Typeface.BOLD), 0, label.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannable);
    }

    private void loadProductDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products").child(productId);
        String currentUserId = FirebaseAuth.getInstance().getUid();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(ProductDetailActivity.this, "Sản phẩm không tồn tại!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                String name = snapshot.child("name").getValue(String.class);
                String price = snapshot.child("price").getValue(String.class);
                String desc = snapshot.child("description").getValue(String.class);
                String category = snapshot.child("category").getValue(String.class);
                String condition = snapshot.child("condition").getValue(String.class);
                String sellerId = snapshot.child("sellerId").getValue(String.class);
                String imageUrl = snapshot.child("imageUrl").getValue(String.class);

                ArrayList<String> imageList = new ArrayList<>();

                if (snapshot.child("images").exists()) {
                    for (DataSnapshot imgSnap : snapshot.child("images").getChildren()) {
                        String img = imgSnap.getValue(String.class);
                        if (img != null && img.startsWith("http")) imageList.add(img);
                    }
                }

                if (imageList.isEmpty() && imageUrl != null && imageUrl.startsWith("http")) {
                    imageList.add(imageUrl);
                }

                if (imageList.isEmpty()) {
                    imageSlider.setVisibility(View.GONE);
                } else {
                    imageSlider.setVisibility(View.VISIBLE);
                    imageSliderAdapter.updateImages(imageList);
                }

                setLabelledText(tvName, "Tên sản phẩm: ", name != null ? name : "(Không tên)");
                setLabelledText(tvPrice, "Giá: ", price != null ? formatCurrency(price) : "(Không giá)");
                setLabelledText(tvCategory, "Danh mục: ", category != null ? category : "-");
                setLabelledText(tvCondition, "Tình trạng: ", condition != null ? condition : "-");
                tvDesc.setText(desc != null ? desc.replace("\\n", "\n") : "(Không mô tả)");

                // Nếu là chủ sản phẩm
                if (currentUserId != null && currentUserId.equals(sellerId)) {
                    btnMakeOffer.setVisibility(View.GONE);
                    btnReport.setVisibility(View.GONE);
                    btnEdit.setVisibility(View.VISIBLE);
                    btnDelete.setVisibility(View.VISIBLE);

                    btnEdit.setOnClickListener(v -> {
                        Intent intent = new Intent(ProductDetailActivity.this, EditProductActivity.class);
                        intent.putExtra("productId", productId);
                        startActivity(intent);
                    });

                    btnDelete.setOnClickListener(v -> confirmDelete(ref));
                } else {
                    btnMakeOffer.setVisibility(View.VISIBLE);
                    btnReport.setVisibility(View.VISIBLE);
                    btnEdit.setVisibility(View.GONE);
                    btnDelete.setVisibility(View.GONE);

                    btnMakeOffer.setOnClickListener(v -> {
                        MakeOfferDialog dialog = new MakeOfferDialog(ProductDetailActivity.this, productId);
                        dialog.show();
                    });

                    btnReport.setOnClickListener(v -> {
                        ReportDialog.newInstance(productId, "product")
                                .show(getSupportFragmentManager(), "report_dialog");
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductDetailActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDelete(DatabaseReference ref) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa sản phẩm này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    ref.removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đã xóa sản phẩm!", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
