package com.example.appc2c.products;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.appc2c.R;
import com.example.appc2c.models.Offer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.appc2c.dialogs.ReportDialog;

import java.util.ArrayList;

public class ProductDetailActivity extends AppCompatActivity {
    private ViewPager2 imageSlider;
    private TextView tvName, tvPrice, tvDesc, tvCategory, tvCondition;
    private Button btnMakeOffer, btnReport;
    private String productId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
        initViews();

        loadProductDetails();
        ArrayList<String> images = getIntent().getStringArrayListExtra("images");
        if (images == null || images.isEmpty()) {
            imageSlider.setVisibility(View.GONE);
            Log.e("ProductDetailActivity", "Image list is null or empty");
        } else {
            ImageSliderAdapter adapter = new ImageSliderAdapter(this, images);
            imageSlider.setAdapter(adapter);
        }

        btnMakeOffer.setVisibility(View.VISIBLE);

        btnMakeOffer.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailActivity.this);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_make_offer, null);
            builder.setView(dialogView);

            EditText offerPrice = dialogView.findViewById(R.id.offerPrice);
            EditText offerNote = dialogView.findViewById(R.id.offerNote);
            Button sendBtn = dialogView.findViewById(R.id.sendOfferBtn);

            AlertDialog dialog = builder.create();

            sendBtn.setOnClickListener(view -> {
                String priceStr = offerPrice.getText().toString().trim();
                String noteStr = offerNote.getText().toString().trim();

                if (priceStr.isEmpty()) {
                    offerPrice.setError("Vui lòng nhập giá đề nghị");
                    return;
                }

                int price;
                try {
                    price = Integer.parseInt(priceStr);
                } catch (NumberFormatException e) {
                    offerPrice.setError("Giá không hợp lệ");
                    return;
                }

                String buyerId = FirebaseAuth.getInstance().getUid();
                if (buyerId == null) {
                    Toast.makeText(ProductDetailActivity.this, "Không xác định người dùng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                String offerId = db.collection("offers").document().getId();
                String sellerId = getIntent().getStringExtra("sellerId");
                Offer offer = new Offer(
                        buyerId,
                        offerId,
                        noteStr,
                        productId,
                        price,
                        "pending",
                        sellerId
                );

                db.collection("offers").document(offerId)
                        .set(offer)
                        .addOnSuccessListener(aVoid -> {
                            dialog.dismiss();
                            btnMakeOffer.setVisibility(View.GONE);
                            Toast.makeText(ProductDetailActivity.this,
                                    "Đã gửi đề nghị thành công!",
                                    Toast.LENGTH_LONG).show();
                        })
                        .addOnFailureListener(e -> Toast.makeText(ProductDetailActivity.this,
                                "Lỗi khi gửi đề nghị: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            });

            dialog.show();
        });

        btnReport.setOnClickListener(v -> {
            ReportDialog dialog = ReportDialog.newInstance(productId, "product");
            dialog.show(getSupportFragmentManager(), "reportDialog");
        });
    }

    private void initViews() {
        imageSlider = findViewById(R.id.imageSlider);
        tvName = findViewById(R.id.txtDetailName);
        tvPrice = findViewById(R.id.txtDetailPrice);
        tvDesc = findViewById(R.id.txtDetailDesc);
        btnMakeOffer = findViewById(R.id.btnMakeOffer);
        btnReport = findViewById(R.id.btnReport);
        tvCategory = findViewById(R.id.txtDetailCategory);
        tvCondition = findViewById(R.id.txtDetailCondition);
    }

    @SuppressLint("DefaultLocale")
    private String formatCurrency(String priceStr) {
        try {
            long price = Long.parseLong(priceStr);
            return String.format("%,d", price).replace(",", ".") + " đ";
        } catch (NumberFormatException e) {
            return priceStr + " đ";
        }
    }

    @SuppressLint("SetTextI18n")
    private void loadProductDetails() {
        productId = getIntent().getStringExtra("productId");

        //  Nếu productId null thì đóng Activity
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy sản phẩm!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String name = getIntent().getStringExtra("name");
        String price = getIntent().getStringExtra("price");
        String desc = getIntent().getStringExtra("desc");
        String category = getIntent().getStringExtra("category");
        String condition = getIntent().getStringExtra("condition");

        if (name != null) tvName.setText(getString(R.string.product_name_placeholder, name));
        if (price != null) tvPrice.setText(formatCurrency(price));
        if (desc != null) tvDesc.setText(desc);
        if (category != null) tvCategory.setText(getString(R.string.product_category, category));
        if (condition != null)
            tvCondition.setText(getString(R.string.product_condition, condition));

        FirebaseFirestore.getInstance()
                .collection("products")
                .document(productId)
                .update("views", com.google.firebase.firestore.FieldValue.increment(1));
    }
}
