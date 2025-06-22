package com.example.appc2c;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        Button btnRegister = findViewById(R.id.btnRegister);
        Button btnBack = findViewById(R.id.btnBack);

        auth = FirebaseAuth.getInstance();

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải từ 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) user.sendEmailVerification();

                            Toast.makeText(this, "Đăng ký thành công. Vui lòng xác minh email", Toast.LENGTH_LONG).show();

                            // Quay lại màn hình login
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            String message = task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại";
                            Toast.makeText(this, "Lỗi: " + message, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
