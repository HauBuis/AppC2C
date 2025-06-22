package com.example.appc2c;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPhone, edtAddress;
    private ImageView imgEditAvatar;
    private static final int PICK_IMAGE_AVATAR = 2002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        Button btnSave = findViewById(R.id.btnSave);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        // Nhận dữ liệu cũ nếu có
        Intent intent = getIntent();
        edtName.setText(intent.getStringExtra("name"));
        edtEmail.setText(intent.getStringExtra("email"));
        edtPhone.setText(intent.getStringExtra("phone"));
        edtAddress.setText(intent.getStringExtra("address"));
        btnSave.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra("name", edtName.getText().toString());
            result.putExtra("email", edtEmail.getText().toString());
            result.putExtra("phone", edtPhone.getText().toString());
            result.putExtra("address", edtAddress.getText().toString());
            setResult(RESULT_OK, result);
            finish();
        });
        imgEditAvatar = findViewById(R.id.imgEditAvatar);
        imgEditAvatar.setOnClickListener(v -> {
            Intent intent1 = new Intent(Intent.ACTION_PICK);
            intent1.setType("image/*");
            startActivityForResult(intent1, PICK_IMAGE_AVATAR);
        });

    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_AVATAR && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri avatarUri = data.getData();
            imgEditAvatar.setImageURI(avatarUri);
        }
    }
}
