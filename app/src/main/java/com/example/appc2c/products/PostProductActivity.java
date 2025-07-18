package com.example.appc2c.products;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.appc2c.R;
import com.example.appc2c.profile.ProfileActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostProductActivity extends AppCompatActivity {
    private EditText edtLocation, edtProductName, edtDescription, edtPrice;
    private Spinner spinnerCategory, spinnerCondition;
    private Button btnChooseImages, btnPost, btnPreview;
    private GridView gridImages;
    private ImageGridAdapter imageGridAdapter;
    private ArrayList<Uri> imageUris = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;

    private static final String CLOUD_NAME = "dgwgmsrxq";
    private static final String UPLOAD_PRESET = "upImg_preset";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_product);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initViews();
        setupToolbar();
        setupBottomNav();
        setupSpinners();
        setupButtons();
        requestLocationPermission();
    }

    private void initViews() {
        edtLocation = findViewById(R.id.edtLocation);
        edtProductName = findViewById(R.id.edtProductName);
        edtDescription = findViewById(R.id.edtDescription);
        edtPrice = findViewById(R.id.edtPrice);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerCondition = findViewById(R.id.spinnerCondition);
        btnChooseImages = findViewById(R.id.btnChooseImages);
        btnPost = findViewById(R.id.btnPost);
        btnPreview = findViewById(R.id.btnPreview);
        gridImages = findViewById(R.id.gridImages);
        imageGridAdapter = new ImageGridAdapter(this, imageUris);
        gridImages.setAdapter(imageGridAdapter);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        toolbar.setNavigationOnClickListener(v -> finish());
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

    private void setupSpinners() {
        String[] categories = {"Chọn danh mục", "Điện thoại", "Laptop", "Thời trang", "Đồ gia dụng"};
        String[] conditions = {"Tình trạng", "Mới", "Đã sử dụng"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<String> conditionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, conditions);
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(conditionAdapter);
    }

    private void setupButtons() {
        btnChooseImages.setOnClickListener(v -> openGallery());

        btnPreview.setOnClickListener(v -> {
            if (!imageUris.isEmpty()) previewProduct();
            else Toast.makeText(this, "Vui lòng chọn ảnh trước", Toast.LENGTH_SHORT).show();
        });

        btnPost.setOnClickListener(v -> {
            if (validateForm() && !imageUris.isEmpty()) uploadFirstImageAndSaveProduct();
            else Toast.makeText(this, "Vui lòng điền đầy đủ thông tin và chọn ảnh", Toast.LENGTH_SHORT).show();
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Chọn ảnh từ thư viện"), 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUris.clear();
            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    imageUris.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                imageUris.add(data.getData());
            }
            imageGridAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Đã chọn " + imageUris.size() + " ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            getLocation();
        }
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    edtLocation.setText(location.getLatitude() + ", " + location.getLongitude());
                } else {
                    edtLocation.setText("Không xác định vị trí");
                }
            });
        } else {
            edtLocation.setText("Không có quyền truy cập vị trí");
        }
    }

    private void uploadFirstImageAndSaveProduct() {
        Uri localUri = imageUris.get(0);
        String filePath = LocalPathUtil.getPath(this, localUri);
        if (filePath == null) {
            Toast.makeText(this, "Không thể lấy đường dẫn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(filePath);
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(file, MediaType.parse("image/*")))
                .addFormDataPart("upload_preset", UPLOAD_PRESET)
                .build();
        Request request = new Request.Builder()
                .url("https://api.cloudinary.com/v1_1/dgwgmsrxq/image/upload")
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(PostProductActivity.this, "Lỗi upload ảnh", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    try {
                        JSONObject obj = new JSONObject(json);
                        String url = obj.getString("secure_url");
                        runOnUiThread(() -> saveProduct(url));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void saveProduct(String cloudinaryImageUrl) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("products").push();
        Map<String, Object> productData = new HashMap<>();
        productData.put("name", edtProductName.getText().toString());
        productData.put("description", edtDescription.getText().toString());
        productData.put("price", edtPrice.getText().toString());
        productData.put("location", edtLocation.getText().toString());
        productData.put("category", spinnerCategory.getSelectedItem().toString());
        productData.put("condition", spinnerCondition.getSelectedItem().toString());
        productData.put("allowNegotiation", true);
        productData.put("features", "màu vàng");
        productData.put("tags", "dientu");
        productData.put("imageUrl", cloudinaryImageUrl);
        Map<String, Object> imagesMap = new HashMap<>();
        imagesMap.put("0", cloudinaryImageUrl);
        productData.put("images", imagesMap);
        dbRef.setValue(productData).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Đăng sản phẩm thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private boolean validateForm() {
        return !edtProductName.getText().toString().trim().isEmpty()
                && !edtDescription.getText().toString().trim().isEmpty()
                && !edtPrice.getText().toString().trim().isEmpty()
                && !edtLocation.getText().toString().trim().isEmpty()
                && spinnerCategory.getSelectedItemPosition() > 0
                && spinnerCondition.getSelectedItemPosition() > 0;
    }

    private void previewProduct() {
        Intent intent = new Intent(this, PreviewProductActivity.class);
        intent.putExtra("name", edtProductName.getText().toString());
        intent.putExtra("description", edtDescription.getText().toString());
        intent.putExtra("price", edtPrice.getText().toString());
        intent.putExtra("location", edtLocation.getText().toString());
        intent.putExtra("category", spinnerCategory.getSelectedItem().toString());
        intent.putExtra("condition", spinnerCondition.getSelectedItem().toString());
        if (!imageUris.isEmpty()) {
            intent.putExtra("imageUrl", imageUris.get(0).toString());
        }
        startActivity(intent);
    }
}
