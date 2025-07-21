package com.example.appc2c.products;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerCart;
    private CartAdapter cartAdapter;
    private final List<Product> cartList = new ArrayList<>();
    private TextView tvTotalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerCart = findViewById(R.id.recyclerCart);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        recyclerCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, cartList, this::updateTotalPrice);

        // Thiết lập listener để xử lý xóa sản phẩm
        cartAdapter.setOnItemActionListener((product, position) -> {
            String userId = FirebaseAuth.getInstance().getUid();
            if (userId == null) return;

            // Kiểm tra productId hợp lệ trước khi xóa
            if (product.getId() == null || product.getId().isEmpty()) {
                Toast.makeText(CartActivity.this, "Sản phẩm không hợp lệ để xóa", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(product.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        cartList.remove(position);
                        cartAdapter.notifyItemRemoved(position);
                        updateTotalPrice();
                        Toast.makeText(CartActivity.this, "Đã xóa sản phẩm khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(CartActivity.this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
                    });
        });

        recyclerCart.setAdapter(cartAdapter);

        loadCart();
    }

    private void loadCart() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(userId)
                .collection("cart")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Product p = doc.toObject(Product.class);
                        if (p != null) {
                            // Lấy trường "picture" từ document (nếu có)
                            String picture = doc.getString("picture");
                            if (picture != null && !picture.isEmpty()) {
                                p.setImageUrl(picture);  // Gán vào imageUrl để dùng chung
                            }
                            // Nếu chưa có selected, set false mặc định
                            if (!p.isSelected()) {
                                p.setSelected(false);
                            }
                            cartList.add(p);
                        }
                    }
                    cartAdapter.notifyDataSetChanged();
                    updateTotalPrice();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CartActivity.this, "Lỗi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateTotalPrice() {
        int total = 0;
        for (Product p : cartList) {
            if (p.isSelected()) {
                int priceInt = 0;
                try {
                    priceInt = Integer.parseInt(p.getPrice().replaceAll("[^\\d]", ""));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                total += priceInt;
            }
        }
        tvTotalPrice.setText("Tổng: " + formatCurrency(total));
    }

    private String formatCurrency(int amount) {
        return NumberFormat.getNumberInstance(Locale.US).format(amount) + " đ";
    }
}
