package com.example.appc2c.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appc2c.R;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPhone, edtAddress, edtBio, edtRating;
    private ImageView imgEditAvatar;
    private static final int PICK_IMAGE_AVATAR = 2002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Ánh xạ View
        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        edtBio = findViewById(R.id.edtBio);
        edtRating = findViewById(R.id.edtRating);
        imgEditAvatar = findViewById(R.id.imgEditAvatar);

        Button btnSave = findViewById(R.id.btnSave);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Nhận dữ liệu từ ProfileActivity
        Intent intent = getIntent();
        edtName.setText(intent.getStringExtra("name"));
        edtEmail.setText(intent.getStringExtra("email"));
        edtPhone.setText(intent.getStringExtra("phone"));
        edtAddress.setText(intent.getStringExtra("address"));
        edtBio.setText(intent.getStringExtra("bio"));
        edtRating.setText(intent.getStringExtra("rating"));

        // Nút lưu
        btnSave.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra("name", edtName.getText().toString());
            result.putExtra("email", edtEmail.getText().toString());
            result.putExtra("phone", edtPhone.getText().toString());
            result.putExtra("address", edtAddress.getText().toString());
            result.putExtra("bio", edtBio.getText().toString());
            result.putExtra("rating", edtRating.getText().toString());
            setResult(RESULT_OK, result);
            finish();
        });

        // Chọn ảnh đại diện
        imgEditAvatar.setOnClickListener(v -> {
            Intent intentPick = new Intent(Intent.ACTION_PICK);
            intentPick.setType("image/*");
            startActivityForResult(intentPick, PICK_IMAGE_AVATAR);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_AVATAR && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri avatarUri = data.getData();
            imgEditAvatar.setImageURI(avatarUri);
        }
    }
}
