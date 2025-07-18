package com.example.appc2c.products;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appc2c.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<Product> productList;
    private OnItemActionListener actionListener;

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.productList = products;
    }

    public interface OnItemActionListener {
        void onDeleteClick(Product product, int position);
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.actionListener = listener;
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnAddToCart;
        TextView tvProductName, tvProductPrice, tvOfferCount;
        Button btndelete;

        public ProductViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvOfferCount = itemView.findViewById(R.id.tvOfferCount);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btndelete = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvProductPrice.setText(formatCurrency(product.getPrice()));

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);

        FirebaseFirestore.getInstance()
                .collection("offers")
                .whereEqualTo("productId", product.getId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    int offerCount = snapshot.size();
                    holder.tvOfferCount.setText("Đề nghị: " + offerCount);
                });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("name", product.getName());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("desc", product.getDescription());
            intent.putExtra("category", product.getCategory());
            intent.putExtra("condition", product.getCondition());
            intent.putExtra("sellerId", product.getSellerId());
            intent.putExtra("productId", product.getId());
            ArrayList<String> imageList = new ArrayList<>();
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                imageList.add(product.getImageUrl());
            }
            intent.putStringArrayListExtra("images", imageList);
            context.startActivity(intent);
        });

        holder.btnAddToCart.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
            if (userId == null) {
                Toast.makeText(context, "Bạn cần đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> cartItem = new HashMap<>();
            cartItem.put("productId", product.getId());
            cartItem.put("name", product.getName());
            cartItem.put("price", product.getPrice());
            cartItem.put("image", product.getImageUrl() != null ? product.getImageUrl() : "");
            cartItem.put("timestamp", System.currentTimeMillis());

            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .collection("cart")
                    .document(product.getId())
                    .set(cartItem)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Lỗi khi thêm vào giỏ", Toast.LENGTH_SHORT).show());
        });

        holder.btndelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onDeleteClick(product, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    @SuppressLint("DefaultLocale")
    private String formatCurrency(String priceStr) {
        try {
            long price = Long.parseLong(priceStr.replace(",", ""));
            return String.format("%,d đ", price);
        } catch (NumberFormatException e) {
            return priceStr + " đ";
        }
    }
}