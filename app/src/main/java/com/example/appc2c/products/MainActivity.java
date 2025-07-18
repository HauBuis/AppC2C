package com.example.appc2c.products;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.example.appc2c.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProductAdapter productAdapter;
    private final List<Product> productList = new ArrayList<>();
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        RecyclerView recyclerProducts = findViewById(R.id.recyclerProducts);
        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerProducts.setNestedScrollingEnabled(false);

        productAdapter = new ProductAdapter(this, productList);
        recyclerProducts.setAdapter(productAdapter);

        // Kết nối Firebase Database
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        loadProductsFromFirebase();

        // Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) return true;
            if (itemId == R.id.nav_post) {
                startActivity(new Intent(MainActivity.this, PostProductActivity.class));
                return true;
            }
            if (itemId == R.id.nav_account) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        bottomNav.setSelectedItemId(R.id.nav_home);

        // Mở danh sách người bán
        Button btnSellerList = findViewById(R.id.btnSellerList);
        btnSellerList.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SellerListActivity.class));
        });
    }

    private void loadProductsFromFirebase() {
        productsRef.addValueEventListener(new ValueEventListener() {
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
                Toast.makeText(MainActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}