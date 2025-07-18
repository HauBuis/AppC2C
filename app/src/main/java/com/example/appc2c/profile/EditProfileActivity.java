package com.example.appc2c.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appc2c.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPhone, edtAddress, edtBio;
    private ImageView imgEditAvatar;
    private Uri avatarUri;
    private static final int PICK_IMAGE_AVATAR = 2002;
    private final String cloudinaryUrl = "https://api.cloudinary.com/v1_1/dgwgmsrxq/image/upload";
    private final String uploadPreset = "upImg_preset";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        edtBio = findViewById(R.id.edtBio);
        imgEditAvatar = findViewById(R.id.imgEditAvatar);

        Button btnSave = findViewById(R.id.btnSave);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        Intent intent = getIntent();
        edtName.setText(intent.getStringExtra("name"));
        edtEmail.setText(intent.getStringExtra("email"));
        edtPhone.setText(intent.getStringExtra("phone"));
        edtAddress.setText(intent.getStringExtra("address"));
        edtBio.setText(intent.getStringExtra("bio"));

        String avatarUriString = intent.getStringExtra("avatarUri");
        if (avatarUriString != null) {
            avatarUri = Uri.parse(avatarUriString);
            imgEditAvatar.setImageURI(avatarUri);
        }

        btnSave.setOnClickListener(v -> saveProfile());

        imgEditAvatar.setOnClickListener(v -> {
            Intent intentPick = new Intent(Intent.ACTION_PICK);
            intentPick.setType("image/*");
            startActivityForResult(intentPick, PICK_IMAGE_AVATAR);
        });
    }

    private void saveProfile() {
        if (avatarUri != null) {
            uploadImageToCloudinary(avatarUri);
        } else {
            updateFirestoreProfile(null);
        }
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                byte[] imageBytes = inputStreamToByteArray(inputStream);

                OkHttpClient client = new OkHttpClient();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", "avatar.jpg",
                                RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                        .addFormDataPart("upload_preset", uploadPreset)
                        .build();

                Request request = new Request.Builder()
                        .url(cloudinaryUrl)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);
                    String imageUrl = jsonObject.getString("secure_url");

                    runOnUiThread(() -> updateFirestoreProfile(imageUrl));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[4096];
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    private void updateFirestoreProfile(String imageUrl) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", edtName.getText().toString());
            updates.put("e-mail", edtEmail.getText().toString());
            updates.put("phone", edtPhone.getText().toString());
            updates.put("address", edtAddress.getText().toString());
            updates.put("bio", edtBio.getText().toString());
            if (imageUrl != null) {
                updates.put("avatar", imageUrl);
            }
            FirebaseFirestore.getInstance().collection("users")
                    .document(user.getUid())
                    .update(updates)
                    .addOnSuccessListener(unused -> finish());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_AVATAR && resultCode == RESULT_OK && data != null && data.getData() != null) {
            avatarUri = data.getData();
            imgEditAvatar.setImageURI(avatarUri);
        }
    }
}
