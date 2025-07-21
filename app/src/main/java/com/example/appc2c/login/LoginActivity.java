package com.example.appc2c.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.appc2c.R;
import com.example.appc2c.admin.AdminDashboardActivity;
import com.example.appc2c.products.MainActivity;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private EditText edtEmail, edtPassword;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 1001;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_login);

        auth = FirebaseAuth.getInstance();

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        CheckBox chkShowPassword = findViewById(R.id.chkShowPassword);
        btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvForgotPass = findViewById(R.id.tvForgotPass);

        btnLogin.setEnabled(false);

        TextWatcher textWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String emailInput = edtEmail.getText().toString().trim();
                String passwordInput = edtPassword.getText().toString().trim();
                boolean isValid = !emailInput.isEmpty() && !passwordInput.isEmpty();
                btnLogin.setEnabled(isValid);
                btnLogin.setAlpha(isValid ? 1.0f : 0.5f);
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        edtEmail.addTextChangedListener(textWatcher);
        edtPassword.addTextChangedListener(textWatcher);

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
    }

    private void handleLogin() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            if (user.isEmailVerified()) {
                                checkUserRole(user.getUid());
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

    private void checkUserRole(String uid) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Boolean isActive = doc.getBoolean("active");
                        String role = doc.getString("role");
                        String email = doc.getString("email");

                        if (Boolean.FALSE.equals(isActive)) {
                            FirebaseAuth.getInstance().signOut();
                            Toast.makeText(this, "Tài khoản bị vô hiệu hóa. Đã gửi yêu cầu kích hoạt lại.", Toast.LENGTH_LONG).show();

                            // Gửi yêu cầu kích hoạt lại
                            Map<String, Object> request = new HashMap<>();
                            request.put("uid", uid);
                            request.put("email", email);
                            request.put("type", "reactivate_account");
                            request.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

                            FirebaseFirestore.getInstance()
                                    .collection("reactivation_requests")
                                    .add(request)
                                    .addOnSuccessListener(ref -> Toast.makeText(this, "Đã gửi yêu cầu kích hoạt lại thành công", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi gửi yêu cầu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            return;
                        }

                        // Lưu quyền vào SharedPreferences
                        saveUserRole(role);

                        Toast.makeText(this, "Đăng nhập thành công (" + role + ")", Toast.LENGTH_SHORT).show();
                        if ("admin".equals(role)) {
                            startActivity(new Intent(this, AdminDashboardActivity.class));
                        } else {
                            startActivity(new Intent(this, MainActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(this, "Không tìm thấy quyền người dùng!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi phân quyền: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Lưu role vào SharedPreferences
    private void saveUserRole(String role) {
        getSharedPreferences("user_info", MODE_PRIVATE)
                .edit()
                .putString("role", role)
                .apply();
    }

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
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) checkUserRole(user.getUid());
                    } else {
                        Toast.makeText(this, "Firebase lỗi: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
