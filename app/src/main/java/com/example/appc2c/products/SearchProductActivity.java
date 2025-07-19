package com.example.appc2c.products;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SearchProductActivity extends AppCompatActivity {

    private EditText edtSearch, edtDistance, edtMinPrice, edtMaxPrice;
    private Spinner spinnerCategory, spinnerCondition, spinnerSort;
    private ProductAdapter adapter;
    private final List<Product> productList = new ArrayList<>();
    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;
    private static final int LOCATION_PERMISSION_CODE = 123;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);

        edtSearch = findViewById(R.id.edtSearch);
        edtDistance = findViewById(R.id.edtDistance);
        edtMinPrice = findViewById(R.id.edtMinPrice);
        edtMaxPrice = findViewById(R.id.edtMaxPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerCondition = findViewById(R.id.spinnerCondition);
        spinnerSort = findViewById(R.id.spinnerSort);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(this, productList);
        recyclerView.setAdapter(adapter);

        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Tất cả", "Điện thoại", "Laptop", "Phụ kiện"}));
        spinnerCondition.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Tất cả", "Mới", "Đã sử dụng"}));
        spinnerSort.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Liên quan", "Mới nhất", "Giá tăng dần", "Giá giảm dần"}));

        // Nếu có keyword truyền vào thì tìm kiếm luôn
        String initKeyword = getIntent().getStringExtra("keyword");
        if (initKeyword != null && !initKeyword.isEmpty()) {
            edtSearch.setText(initKeyword);
            searchProducts();
        }

        // Hiện gợi ý khi click vào ô tìm kiếm mà chưa nhập gì
        edtSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && edtSearch.getText().toString().trim().isEmpty()) {
                showSuggestions();
            }
        });

        // Auto lọc cho tất cả các EditText liên quan
        addAutoSearch(edtSearch);
        addAutoSearch(edtMinPrice);
        addAutoSearch(edtMaxPrice);
        addAutoSearch(edtDistance);

        spinnerCategory.setOnItemSelectedListener(new SimpleItemSelectedListener(this::searchProducts));
        spinnerCondition.setOnItemSelectedListener(new SimpleItemSelectedListener(this::searchProducts));
        spinnerSort.setOnItemSelectedListener(new SimpleItemSelectedListener(this::searchProducts));

        requestLocation();
    }

    // Hàm gắn auto lọc sau 200ms cho EditText
    private void addAutoSearch(EditText edt) {
        edt.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = SearchProductActivity.this::searchProducts;
                searchHandler.postDelayed(searchRunnable, 200);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> currentLocation = location);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    private void showSuggestions() {
        SharedPreferences prefs = getSharedPreferences("viewed_products", MODE_PRIVATE);
        String ids = prefs.getString("history", "");
        if (!ids.isEmpty()) {
            suggestByHistory();
            return;
        }
        String category = spinnerCategory.getSelectedItem().toString();
        if (!category.equals("Tất cả")) {
            suggestByCategory();
            return;
        }
        suggestPopularOrNearby();
    }

    // Gợi ý theo danh mục sản phẩm
    private void suggestByCategory() {
        String category = spinnerCategory.getSelectedItem().toString();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> suggestedList = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Product p = snap.getValue(Product.class);
                    if (p == null) continue;
                    if (!category.equals("Tất cả") && !category.equals(p.getCategory())) continue;
                    suggestedList.add(p);
                }
                // Sort theo views giảm dần, lấy tối đa 10
                Collections.sort(suggestedList, (a, b) -> Integer.compare(b.getViews(), a.getViews()));
                if (suggestedList.size() > 10)
                    suggestedList = suggestedList.subList(0, 10);
                adapter.setSuggestedList(suggestedList, "Gợi ý theo danh mục");
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Gợi ý sản phẩm đã xem (cá nhân hóa)
    private void suggestByHistory() {
        SharedPreferences prefs = getSharedPreferences("viewed_products", MODE_PRIVATE);
        String ids = prefs.getString("history", "");
        if (ids.isEmpty()) return;
        String[] idArr = ids.split(",");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> historyList = new ArrayList<>();
                for (String id : idArr) {
                    DataSnapshot snap = snapshot.child(id);
                    Product p = snap.getValue(Product.class);
                    if (p != null) historyList.add(p);
                }
                adapter.setSuggestedList(historyList, "Sản phẩm đã xem");
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Gợi ý sản phẩm phổ biến & gần vị trí user
    private void suggestPopularOrNearby() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> popularList = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Product p = snap.getValue(Product.class);
                    if (p == null) continue;
                    if (currentLocation != null && p.getLat() != null && p.getLng() != null) {
                        float[] result = new float[1];
                        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), p.getLat(), p.getLng(), result);
                        if (result[0] <= 5000) { // dưới 5km
                            popularList.add(p);
                        }
                    } else {
                        popularList.add(p);
                    }
                }
                // Sort views giảm dần, lấy tối đa 10
                Collections.sort(popularList, (a, b) -> Integer.compare(b.getViews(), a.getViews()));
                if (popularList.size() > 10)
                    popularList = popularList.subList(0, 10);
                adapter.setSuggestedList(popularList, "Sản phẩm phổ biến & gần bạn");
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // === Tìm kiếm chính ===
    @SuppressLint("NotifyDataSetChanged")
    private void searchProducts() {
        String keyword = edtSearch.getText().toString().trim().toLowerCase();

        String category = spinnerCategory.getSelectedItem().toString();
        String condition = spinnerCondition.getSelectedItem().toString();
        String sort = spinnerSort.getSelectedItem().toString();

        // Xử lý khoảng cách an toàn, chỉ gán final 1 lần!
        String distanceStr = edtDistance.getText().toString().trim();
        float tempDistance = 5000;
        try {
            if (!distanceStr.isEmpty()) {
                tempDistance = Float.parseFloat(distanceStr) * 1000;
                if (tempDistance < 0) tempDistance = 5000;
            }
        } catch (Exception e) {
            tempDistance = 5000;
        }
        final float maxDistance = tempDistance;

        String minStr = edtMinPrice.getText().toString().trim();
        String maxStr = edtMaxPrice.getText().toString().trim();
        long min = minStr.isEmpty() ? 0 : Long.parseLong(minStr);
        long max = maxStr.isEmpty() ? Long.MAX_VALUE : Long.parseLong(maxStr);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("products");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                List<Product> filteredList = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Product p = snap.getValue(Product.class);
                    if (p == null) continue;

                    // Filter từ khóa
                    if (!keyword.isEmpty() && (p.getName() == null || !p.getName().toLowerCase().contains(keyword))) continue;

                    // Filter category
                    if (!category.equals("Tất cả") && (p.getCategory() == null || !p.getCategory().equals(category))) continue;

                    // Filter condition
                    if (!condition.equals("Tất cả") && (p.getCondition() == null || !p.getCondition().equals(condition))) continue;

                    // Filter khoảng giá (ĐƠN VỊ ĐỒNG, KHÔNG NHÂN 1000)
                    try {
                        long priceLong = Long.parseLong(p.getPrice().replaceAll("\\D", ""));
                        if (priceLong < min || priceLong > max) continue;
                    } catch (Exception e) {
                        continue;
                    }

                    // Filter vị trí
                    if (currentLocation != null && p.getLat() != null && p.getLng() != null) {
                        float[] result = new float[1];
                        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), p.getLat(), p.getLng(), result);
                        if (result[0] > maxDistance) continue;
                    }
                    filteredList.add(p);
                }

                // Sort
                switch (sort) {
                    case "Mới nhất":
                        Collections.sort(filteredList, (a, b) -> {
                            try {
                                long ta = a.getClass().getDeclaredField("timestamp") != null ? (long) a.getClass().getDeclaredField("timestamp").get(a) : 0;
                                long tb = b.getClass().getDeclaredField("timestamp") != null ? (long) b.getClass().getDeclaredField("timestamp").get(b) : 0;
                                return Long.compare(tb, ta);
                            } catch (Exception ignore) {
                                return 0;
                            }
                        });
                        break;
                    case "Giá tăng dần":
                        Collections.sort(filteredList, Comparator.comparingLong(p -> {
                            try { return Long.parseLong(p.getPrice().replaceAll("\\D", "")); }
                            catch (Exception e) { return Long.MAX_VALUE; }
                        }));
                        break;
                    case "Giá giảm dần":
                        Collections.sort(filteredList, (a, b) -> {
                            try {
                                long pa = Long.parseLong(a.getPrice().replaceAll("\\D", ""));
                                long pb = Long.parseLong(b.getPrice().replaceAll("\\D", ""));
                                return Long.compare(pb, pa);
                            } catch (Exception e) { return 0; }
                        });
                        break;
                    case "Liên quan":
                        Collections.sort(filteredList, (a, b) -> {
                            int scoreA = getRelevanceScore(a, keyword, currentLocation);
                            int scoreB = getRelevanceScore(b, keyword, currentLocation);
                            return Integer.compare(scoreB, scoreA);
                        });
                        break;
                }

                productList.addAll(filteredList);
                adapter.resetToNormal(productList);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SearchProductActivity.this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm tính điểm "liên quan"
    private int getRelevanceScore(Product p, String keyword, Location userLocation) {
        int score = 0;
        if (p.getName() != null && p.getName().toLowerCase().contains(keyword)) score += 10;
        if (p.getViews() > 50) score += 3;
        if (userLocation != null && p.getLat() != null && p.getLng() != null) {
            float[] result = new float[1];
            Location.distanceBetween(userLocation.getLatitude(), userLocation.getLongitude(), p.getLat(), p.getLng(), result);
            if (result[0] < 2000) score += 5;
        }
        return score;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocation();
        }
    }
}
