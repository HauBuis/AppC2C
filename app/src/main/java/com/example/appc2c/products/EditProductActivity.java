package com.example.appc2c.products;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import java.util.Arrays;
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
    private Spinner spinnerEditStatus;
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
        spinnerEditStatus = findViewById(R.id.spinnerEditStatus);

        // Thiết lập Spinner
        String[] statusOptions = {"\u0110ang b\u00e1n", "T\u1ea1m d\u1eebng", "\u0110\u00e3 b\u00e1n"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditStatus.setAdapter(adapter);

        productId = getIntent().getStringExtra("productId");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Kh\u00f4ng t\u00ecm th\u1ea5y ID s\u1ea3n ph\u1ea9m!", Toast.LENGTH_SHORT).show();
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

                                // Set status cho spinner
                                String status = product.getStatus();
                                if (status != null) {
                                    switch (status) {
                                        case "dang_ban":
                                            spinnerEditStatus.setSelection(0);
                                            break;
                                        case "tam_dung":
                                            spinnerEditStatus.setSelection(1);
                                            break;
                                        case "da_ban":
                                            spinnerEditStatus.setSelection(2);
                                            break;
                                    }
                                }

                                String imageUrl = product.getImageUrl() != null ? product.getImageUrl() : "";
                                if (!imageUrl.isEmpty()) {
                                    Glide.with(EditProductActivity.this).load(imageUrl).into(imgProductEdit);
                                }
                            } else {
                                Toast.makeText(EditProductActivity.this, "D\u1eef li\u1ec7u s\u1ea3n ph\u1ea9m kh\u00f4ng h\u1ee3p l\u1ec7!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(EditProductActivity.this, "S\u1ea3n ph\u1ea9m kh\u00f4ng t\u1ed3n t\u1ea1i!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EditProductActivity.this, "L\u1ed7i: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                if (inputStream == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Kh\u00f4ng \u0111\u1ecdc \u0111\u01b0\u1ee3c \u1ea3nh!", Toast.LENGTH_SHORT).show());
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
                    runOnUiThread(() -> Toast.makeText(this, "L\u1ed7i upload \u1ea3nh!", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "L\u1ed7i: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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

        String selectedStatus = spinnerEditStatus.getSelectedItem().toString();
        switch (selectedStatus) {
            case "\u0110ang b\u00e1n":
                updates.put("status", "dang_ban");
                break;
            case "T\u1ea1m d\u1eebng":
                updates.put("status", "tam_dung");
                break;
            case "\u0110\u00e3 b\u00e1n":
                updates.put("status", "da_ban");
                break;
        }

        if (imageUrl != null) {
            updates.put("imageUrl", imageUrl);
        }

        FirebaseDatabase.getInstance().getReference("products")
                .child(productId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "\u0110\u00e3 c\u1eadp nh\u1eadt s\u1ea3n ph\u1ea9m!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "L\u1ed7i c\u1eadp nh\u1eadt: " + e.getMessage(), Toast.LENGTH_SHORT).show()
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
