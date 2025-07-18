package com.example.appc2c.products;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appc2c.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProductActivity extends AppCompatActivity {

    private EditText edtName, edtPrice, edtDescription, edtCategory, edtCondition;
    private ImageView imgProductEdit;
    private Button btnSave;
    private String productId;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_products);

        edtName = findViewById(R.id.edtProductName);
        edtPrice = findViewById(R.id.edtProductPrice);
        edtDescription = findViewById(R.id.edtProductDescription);
        edtCategory = findViewById(R.id.edtProductCategory);
        edtCondition = findViewById(R.id.edtProductCondition);
        btnSave = findViewById(R.id.btnSaveProduct);
        imgProductEdit = findViewById(R.id.imgProductEdit);

        productId = getIntent().getStringExtra("productId");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy ID sản phẩm!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadProductData();

        btnSave.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageToCloudinary(imageUri);
            } else {
                saveProductChanges(null);
            }
        });

        imgProductEdit.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
    }

    private void loadProductData() {
        FirebaseDatabase.getInstance().getReference("products")
                .child(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Product product = snapshot.getValue(Product.class);
                            if (product != null) {
                                edtName.setText(product.getName() != null ? product.getName() : "");
                                edtPrice.setText(product.getPrice() != null ? product.getPrice() : "");
                                edtDescription.setText(product.getDescription() != null ? product.getDescription() : "");
                                edtCategory.setText(product.getCategory() != null ? product.getCategory() : "");
                                edtCondition.setText(product.getCondition() != null ? product.getCondition() : "");

                                String imageUrl = product.getImageUrl() != null ? product.getImageUrl() : "";
                                if (!imageUrl.isEmpty()) {
                                    Glide.with(EditProductActivity.this).load(imageUrl).into(imgProductEdit);
                                }
                            } else {
                                Toast.makeText(EditProductActivity.this, "Dữ liệu sản phẩm không hợp lệ!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(EditProductActivity.this, "Sản phẩm không tồn tại!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditProductActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Không đọc được ảnh!", Toast.LENGTH_SHORT).show());
                    return;
                }

                ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    byteBuffer.write(buffer, 0, len);
                }
                inputStream.close();

                byte[] imageBytes = byteBuffer.toByteArray();

                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("file", "image.jpg", RequestBody.create(imageBytes, MediaType.parse("image/*")))
                        .addFormDataPart("upload_preset", "upImg_preset")
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.cloudinary.com/v1_1/dgwgmsrxq/image/upload")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String imageUrl = new JSONObject(response.body().string()).getString("secure_url");
                    runOnUiThread(() -> saveProductChanges(imageUrl));
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Lỗi upload ảnh!", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void saveProductChanges(String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", edtName.getText().toString().trim());
        updates.put("price", edtPrice.getText().toString().trim());
        updates.put("description", edtDescription.getText().toString().trim());
        updates.put("category", edtCategory.getText().toString().trim());
        updates.put("condition", edtCondition.getText().toString().trim());

        if (imageUrl != null) {
            updates.put("imageUrl", imageUrl);
        }

        FirebaseDatabase.getInstance().getReference("products")
                .child(productId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã cập nhật sản phẩm!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imgProductEdit);
        }
    }
}