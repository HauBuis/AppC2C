package com.example.appc2c.profile;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.example.appc2c.products.Product;
import com.example.appc2c.products.ProductAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PurchasedActivity extends AppCompatActivity {

    private ProductAdapter productAdapter;
    private final List<Product> purchasedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchased);

        // Thiết lập toolbar và nút back
        MaterialToolbar toolbar = findViewById(R.id.toolbarPurchased);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Thiết lập RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recyclerPurchased);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(this, purchasedList);
        recyclerView.setAdapter(productAdapter);

        // Load sản phẩm đã mua
        loadPurchasedProducts();
    }

    private void loadPurchasedProducts() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("purchases")
                .whereEqualTo("buyerId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> purchasedProductIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String productId = doc.getString("productId");
                        if (productId != null) {
                            purchasedProductIds.add(productId);
                        }
                    }

                    if (purchasedProductIds.isEmpty()) {
                        Toast.makeText(this, "Chưa có sản phẩm đã mua", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseFirestore.getInstance()
                            .collection("products")
                            .get()
                            .addOnSuccessListener(productSnapshot -> {
                                purchasedList.clear();
                                for (QueryDocumentSnapshot productDoc : productSnapshot) {
                                    Product product = productDoc.toObject(Product.class);
                                    product.setId(productDoc.getId());

                                    if (purchasedProductIds.contains(product.getId())) {
                                        purchasedList.add(product);
                                    }
                                }
                                productAdapter.notifyDataSetChanged();
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi tải sản phẩm: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
