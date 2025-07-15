package com.example.appc2c.products;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.appc2c.R;
import com.example.appc2c.profile.ProfileActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;
    private static final int MAX_IMAGES = 10;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 200;

    private final List<Uri> imageUris = new ArrayList<>();
    private final List<String> uploadedUrls = new ArrayList<>();
    private GridView gridImages;
    private EditText edtProductName, edtDescription, edtPrice, edtLocation, edtFeatures, edtTags;
    private Spinner spinnerCategory, spinnerCondition;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_post_product);

        checkStoragePermission();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initUI();
        checkLocationPermission();
        setupBottomNav();
    }

    private void initUI() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());

        gridImages = findViewById(R.id.gridImages);
        edtProductName = findViewById(R.id.edtProductName);
        edtDescription = findViewById(R.id.edtDescription);
        edtPrice = findViewById(R.id.edtPrice);
        edtLocation = findViewById(R.id.edtLocation);
        edtFeatures = findViewById(R.id.edtFeatures);
        edtTags = findViewById(R.id.edtTags);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerCondition = findViewById(R.id.spinnerCondition);

        String[] categories = {"Chọn danh mục", "Điện thoại", "Laptop", "Thời trang", "Đồ gia dụng"};
        String[] conditions = {"Tình trạng", "Mới", "Đã sử dụng", "Còn bảo hành"};

        spinnerCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories));
        spinnerCondition.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, conditions));

        findViewById(R.id.btnChooseImages).setOnClickListener(v -> openGallery());
        findViewById(R.id.btnPreview).setOnClickListener(v -> {
            if (validateForm()) uploadImagesToCloudinary(imageUris, true);
        });
        findViewById(R.id.btnPost).setOnClickListener(v -> {
            if (validateForm()) uploadImagesToCloudinary(imageUris, false);
        });
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_post);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home)
                startActivity(new Intent(this, MainActivity.class));
            else if (item.getItemId() == R.id.nav_account)
                startActivity(new Intent(this, ProfileActivity.class));
            return true;
        });
    }

    private boolean validateForm() {
        return !(edtProductName.getText().toString().trim().isEmpty() ||
                edtDescription.getText().toString().trim().isEmpty() ||
                edtPrice.getText().toString().trim().isEmpty() ||
                edtLocation.getText().toString().trim().isEmpty() ||
                spinnerCategory.getSelectedItem().toString().equals("Chọn danh mục") ||
                spinnerCondition.getSelectedItem().toString().equals("Tình trạng") ||
                imageUris.isEmpty());
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_REQUEST_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                edtLocation.setText("Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
            } else {
                Toast.makeText(this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("IntentReset")
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh từ thư viện"), PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUris.clear();
            try {
                if (data.getClipData() != null) {
                    int count = Math.min(data.getClipData().getItemCount(), MAX_IMAGES);
                    for (int i = 0; i < count; i++) {
                        imageUris.add(data.getClipData().getItemAt(i).getUri());
                    }
                } else if (data.getData() != null) {
                    imageUris.add(data.getData());
                }
                if (!imageUris.isEmpty()) {
                    gridImages.setAdapter(new ImageAdapter(this, imageUris));
                    Toast.makeText(this, "Đã chọn " + imageUris.size() + " ảnh", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadImagesToCloudinary(List<Uri> uris, boolean isPreview) {
        uploadedUrls.clear();
        for (Uri uri : uris) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                byte[] imageBytes = new byte[inputStream.available()];
                inputStream.read(imageBytes);
                inputStream.close();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "image.jpg",
                                RequestBody.create(imageBytes, MediaType.parse("image/*")))
                        .addFormDataPart("upload_preset", "upImg_preset")
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.cloudinary.com/v1_1/dgwgmsrxq/image/upload")
                        .post(requestBody)
                        .build();

                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        runOnUiThread(() -> Toast.makeText(PostProductActivity.this,
                                "Upload lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String responseString = response.body().string();
                        Log.d("CLOUDINARY_RESPONSE", responseString);
                        if (response.isSuccessful()) {
                            try {
                                JSONObject json = new JSONObject(responseString);
                                String imageUrl = json.getString("secure_url");
                                synchronized (uploadedUrls) {
                                    uploadedUrls.add(imageUrl);
                                    if (uploadedUrls.size() == uris.size()) {
                                        runOnUiThread(() -> {
                                            Log.d("CLOUDINARY_UPLOAD", "All Uploaded URLs: " + uploadedUrls);
                                            if (isPreview) previewProduct(uploadedUrls);
                                            else saveProductToRealtimeDB(uploadedUrls);
                                        });
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(PostProductActivity.this,
                                    "Phản hồi lỗi từ Cloudinary: " + responseString, Toast.LENGTH_LONG).show());
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Lỗi đọc ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProductToRealtimeDB(List<String> imageUrls) {
        String name = edtProductName.getText().toString().trim();
        String price = edtPrice.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String condition = spinnerCondition.getSelectedItem().toString();
        String features = edtFeatures.getText().toString().trim();
        String tags = edtTags.getText().toString().trim();


        String sellerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Tạo đối tượng Product và gán dữ liệu
        Product product = new Product(name, price, imageUrls, description, true);
        product.setCategory(category);
        product.setCondition(condition);
        product.setFeatures(features);
        product.setTags(tags);
        product.setSellerId(sellerId);

        // Tạo key và lưu lên Firebase
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("products").push();
        String productId = productRef.getKey();
        product.setId(productId);

        productRef.setValue(product)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đăng bài thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Đăng bài thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void previewProduct(List<String> imageUrls) {
        Intent intent = new Intent(this, PreviewProductActivity.class);
        intent.putExtra("name", edtProductName.getText().toString());
        intent.putExtra("description", edtDescription.getText().toString());
        intent.putExtra("price", edtPrice.getText().toString());
        intent.putExtra("location", edtLocation.getText().toString());
        intent.putExtra("features", edtFeatures.getText().toString());
        intent.putExtra("tags", edtTags.getText().toString());
        intent.putExtra("category", spinnerCategory.getSelectedItem().toString());
        intent.putExtra("condition", spinnerCondition.getSelectedItem().toString());
        intent.putStringArrayListExtra("imageUris", new ArrayList<>(imageUrls));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Đã cấp quyền truy cập ảnh", Toast.LENGTH_SHORT).show();
        }
    }
}
