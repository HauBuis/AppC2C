package com.example.appc2c.products;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.example.appc2c.admin.AdminDashboardActivity;
import com.example.appc2c.profile.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ProductAdapter productAdapter;
    private final List<Product> productList = new ArrayList<>();
    private DatabaseReference productsRef;
    private ProgressBar progressBar;
    private TextView tvNoProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Phân quyền admin: nếu role = admin thì chuyển sang admin dashboard luôn
        SharedPreferences prefs = getSharedPreferences("user_info", MODE_PRIVATE);
        String role = prefs.getString("role", "user");
        if ("admin".equals(role)) {
            startActivity(new Intent(this, AdminDashboardActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Thiết lập Toolbar và set menu cho nó hoạt động đúng
        Toolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        FirebaseApp.initializeApp(this);

        RecyclerView recyclerProducts = findViewById(R.id.recyclerProducts);
        recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerProducts.setNestedScrollingEnabled(false);

        productAdapter = new ProductAdapter(this, productList);
        recyclerProducts.setAdapter(productAdapter);

        progressBar = findViewById(R.id.progressBar);
        tvNoProduct = findViewById(R.id.tvNoProduct);

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

        Button btnSellerList = findViewById(R.id.btnSellerList);
        btnSellerList.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SellerListActivity.class)));

        MaterialCardView cardOpenSearch = findViewById(R.id.cardOpenSearch);
        if (cardOpenSearch != null) {
            cardOpenSearch.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SearchProductActivity.class)));
        }

        // Xử lý sự kiện click sản phẩm: mở ProductDetailActivity
        productAdapter.setOnItemActionListener((product, position) -> {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            intent.putExtra("productId", product.getId());
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu (Thông báo, Giỏ hàng)
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_notification) {
            startActivity(new Intent(this, NotificationActivity.class));
            return true;
        } else if (id == R.id.menu_cart) {
            startActivity(new Intent(this, CartActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Load sản phẩm từ Firebase Realtime Database, sắp xếp theo views giảm dần
    private void loadProductsFromFirebase() {
        progressBar.setVisibility(android.view.View.VISIBLE);
        tvNoProduct.setVisibility(android.view.View.GONE);

        productsRef.addValueEventListener(new ValueEventListener() {
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
                Collections.sort(productList, (a, b) -> Integer.compare(b.getViews(), a.getViews()));
                productAdapter.notifyDataSetChanged();

                progressBar.setVisibility(android.view.View.GONE);
                tvNoProduct.setVisibility(productList.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(android.view.View.GONE);
                Toast.makeText(MainActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                tvNoProduct.setVisibility(android.view.View.VISIBLE);
            }
        });
    }
}
