package com.example.appc2c;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.common.SignInButton;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.*;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_login);

        auth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        CheckBox chkShowPassword = findViewById(R.id.chkShowPassword);

        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        SignInButton btnGGLogin = findViewById(R.id.btnGGLogin);

        TextView tvForgotPass = findViewById(R.id.tvForgotPass);

        // 👉 Hiện/ẩn mật khẩu
        chkShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                edtPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            edtPassword.setSelection(edtPassword.getText().length());
        });

        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        btnLogin.setOnClickListener(v -> handleLogin());

        tvForgotPass.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));

        // 👉 Đăng nhập bằng Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // từ google-services.json
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGGLogin.setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    private void handleLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            } else {
                                user.sendEmailVerification();
                                Toast.makeText(this, "Tài khoản chưa xác minh. Đã gửi lại email xác minh.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(this, "Không thể lấy thông tin người dùng", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi đăng nhập: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // 👉 Xử lý kết quả đăng nhập Google
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "Đăng nhập Google thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Firebase lỗi: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
