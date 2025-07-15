package com.example.appc2c.products;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchProductActivity extends AppCompatActivity {

    private EditText edtSearch, edtDistance, edtMinPrice, edtMaxPrice, edtCustomLocation;
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
        edtCustomLocation = findViewById(R.id.edtCustomLocation);
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
                new String[]{"Tất cả", "Điện thoại", "Laptop", "Thời trang"}));
        spinnerCondition.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Tất cả", "Mới", "Đã sử dụng"}));
        spinnerSort.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Liên quan", "Mới nhất", "Giá tăng dần", "Giá giảm dần"}));

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> searchProducts();
                searchHandler.postDelayed(searchRunnable, 200);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        spinnerCategory.setOnItemSelectedListener(new SimpleItemSelectedListener(this::searchProducts));
        spinnerCondition.setOnItemSelectedListener(new SimpleItemSelectedListener(this::searchProducts));
        spinnerSort.setOnItemSelectedListener(new SimpleItemSelectedListener(this::searchProducts));

        requestLocation();
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

    @SuppressLint("NotifyDataSetChanged")
    private void searchProducts() {
        String keyword = edtSearch.getText().toString().trim().toLowerCase();
        String customLocation = edtCustomLocation.getText().toString().trim();
        if (!customLocation.isEmpty()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocationName(customLocation, 1);
                if (!addresses.isEmpty()) {
                    double lat = addresses.get(0).getLatitude();
                    double lng = addresses.get(0).getLongitude();
                    currentLocation = new Location("custom");
                    currentLocation.setLatitude(lat);
                    currentLocation.setLongitude(lng);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Không tìm được vị trí", Toast.LENGTH_SHORT).show();
            }
        }

        String category = spinnerCategory.getSelectedItem().toString();
        String condition = spinnerCondition.getSelectedItem().toString();
        String sort = spinnerSort.getSelectedItem().toString();
        String distanceStr = edtDistance.getText().toString().trim();
        float maxDistance = distanceStr.isEmpty() ? 5000 : Float.parseFloat(distanceStr) * 1000;

        String minStr = edtMinPrice.getText().toString().trim();
        String maxStr = edtMaxPrice.getText().toString().trim();
        long min = minStr.isEmpty() ? 0 : Long.parseLong(minStr);
        long max = maxStr.isEmpty() ? Long.MAX_VALUE : Long.parseLong(maxStr);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("products");

        if (!category.equals("Tất cả")) query = query.whereEqualTo("category", category);
        if (!condition.equals("Tất cả")) query = query.whereEqualTo("condition", condition);

        switch (sort) {
            case "Mới nhất": query = query.orderBy("timestamp", Query.Direction.DESCENDING); break;
            case "Giá tăng dần": query = query.orderBy("price_num", Query.Direction.ASCENDING); break;
            case "Giá giảm dần": query = query.orderBy("price_num", Query.Direction.DESCENDING); break;
        }

        query.get().addOnSuccessListener(snapshot -> {
            productList.clear();
            List<Product> popularList = new ArrayList<>();
            List<Product> nearbyList = new ArrayList<>();

            for (var doc : snapshot.getDocuments()) {
                Product p = doc.toObject(Product.class);
                if (p == null) continue;
                p.setId(doc.getId());
                if (!keyword.isEmpty() && !p.getName().toLowerCase().contains(keyword)) continue;

                try {
                    long priceLong = Long.parseLong(p.getPrice().replaceAll("\\D", ""));
                    if (priceLong < min || priceLong > max) continue;
                } catch (Exception e) {
                    continue;
                }

                if (currentLocation != null && doc.contains("lat") && doc.contains("lng")) {
                    Double lat = doc.getDouble("lat");
                    Double lng = doc.getDouble("lng");
                    if (lat == null || lng == null) continue;
                    float[] result = new float[1];
                    Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(), lat, lng, result);
                    if (result[0] <= maxDistance) nearbyList.add(p);
                } else {
                    productList.add(p);
                }

                if (doc.contains("views") && doc.getLong("views") != null && doc.getLong("views") > 50) {
                    popularList.add(p);
                }
            }

            productList.addAll(nearbyList);
            productList.addAll(popularList);
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải sản phẩm", Toast.LENGTH_SHORT).show());
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