package com.example.appc2c.products;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyProductsActivity extends AppCompatActivity {

    private ProductAdapter productAdapter;
    private final List<Product> productList = new ArrayList<>();
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_products);

        RecyclerView recyclerProducts = findViewById(R.id.recyclerProducts);
        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerProducts.setNestedScrollingEnabled(false);

        productAdapter = new ProductAdapter(this, productList);
        recyclerProducts.setAdapter(productAdapter);

        productsRef = FirebaseDatabase.getInstance().getReference("products");
        loadUserProducts();

        // Xử lý xóa sản phẩm từ adapter
        productAdapter.setOnItemActionListener((product, position) -> {
            deleteProduct(product.getId(), position);
        });
    }

    private void loadUserProducts() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(this, "Bạn chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        productsRef.orderByChild("sellerId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productList.clear();
                        for (DataSnapshot productSnap : snapshot.getChildren()) {
                            Product product = productSnap.getValue(Product.class);
                            if (product != null) {
                                product.setId(productSnap.getKey());
                                productList.add(product);
                            }
                        }
                        productAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MyProductsActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteProduct(String productId, int position) {
        if (productId == null) return;
        productsRef.child(productId).removeValue().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đã xóa sản phẩm!", Toast.LENGTH_SHORT).show();
            productList.remove(position);
            productAdapter.notifyItemRemoved(position);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Xóa sản phẩm thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
