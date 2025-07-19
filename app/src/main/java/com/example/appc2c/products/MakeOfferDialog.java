package com.example.appc2c.products;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.appc2c.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MakeOfferDialog extends Dialog {

    public MakeOfferDialog(Context context, String productId) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_make_offer);
        setCancelable(true);

        EditText edtPrice = findViewById(R.id.offerPrice);
        EditText edtNote = findViewById(R.id.offerNote);
        Button btnSubmit = findViewById(R.id.sendOfferBtn);

        btnSubmit.setOnClickListener(v -> {
            String priceStr = edtPrice.getText().toString().trim();
            String note = edtNote.getText().toString().trim();

            if (priceStr.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập giá", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int price = Integer.parseInt(priceStr);
                String userId = FirebaseAuth.getInstance().getUid();

                if (userId == null) {
                    Toast.makeText(getContext(), "Bạn cần đăng nhập", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> offer = new HashMap<>();
                offer.put("productId", productId);
                offer.put("userId", userId);
                offer.put("proposedPrice", price);
                offer.put("note", note);
                offer.put("status", "pending");
                offer.put("timestamp", System.currentTimeMillis());

                FirebaseFirestore.getInstance()
                        .collection("offers")
                        .add(offer)
                        .addOnSuccessListener(docRef -> {
                            Toast.makeText(getContext(), "Đã gửi đề nghị!", Toast.LENGTH_SHORT).show();
                            dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Giá không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }
}