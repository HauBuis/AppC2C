package com.example.appc2c.products;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appc2c.R;
import com.example.appc2c.models.OfferListActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private List<Product> productList;
    private String suggestionLabel = null;

    private static final int TYPE_LABEL = 0;
    private static final int TYPE_PRODUCT = 1;

    private OnItemActionListener actionListener;

    public interface OnItemActionListener {
        void onDeleteClick(Product product, int position);
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.actionListener = listener;
    }

    public ProductAdapter(Context context, List<Product> products) {
        this.context = context;
        this.productList = products;
    }

    /*** Set list sản phẩm kèm label gợi ý ***/
    public void setSuggestedList(List<Product> suggestions, String label) {
        this.productList = suggestions;
        this.suggestionLabel = label;
        notifyDataSetChanged();
    }

    /*** Reset về danh sách bình thường ***/
    public void resetToNormal(List<Product> products) {
        this.productList = products;
        this.suggestionLabel = null;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return productList == null ? 0 : (suggestionLabel == null ? productList.size() : productList.size() + 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (suggestionLabel != null && position == 0) return TYPE_LABEL;
        return TYPE_PRODUCT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_LABEL) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_suggestion_label, parent, false);
            return new SuggestionLabelViewHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SuggestionLabelViewHolder) {
            ((SuggestionLabelViewHolder) holder).label.setText(suggestionLabel);
        } else {
            int realPos = suggestionLabel == null ? position : position - 1;
            Product product = productList.get(realPos);
            ((ProductViewHolder) holder).bind(product);
        }
    }

    /*** ViewHolder cho label gợi ý ***/
    public static class SuggestionLabelViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        public SuggestionLabelViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.tvSuggestionLabel);
        }
    }

    /*** ViewHolder cho sản phẩm ***/
    public class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnAddToCart;
        TextView tvProductName, tvProductPrice, tvOfferCount, tvStatus, tvViews, tvInteractions;
        Button btnEdit, btnViewOffers;
        Spinner spinnerStatus;

        public ProductViewHolder(View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvOfferCount = itemView.findViewById(R.id.tvOfferCount);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvViews = itemView.findViewById(R.id.tvViews);
            tvInteractions = itemView.findViewById(R.id.tvInteractions);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnViewOffers = itemView.findViewById(R.id.btnViewOffers);
            spinnerStatus = itemView.findViewById(R.id.spinnerStatus);
        }

        public void bind(Product product) {
            tvProductName.setText(product.getName());
            tvProductPrice.setText(formatCurrency(product.getPrice()));

            String statusText = "Đang bán";
            if (product.getStatus() != null) {
                switch (product.getStatus()) {
                    case "da_ban": statusText = "Đã bán"; break;
                    case "tam_dung": statusText = "Tạm dừng"; break;
                }
            }
            tvStatus.setText(statusText);

            tvViews.setText("Lượt xem: " + product.getViews());
            tvInteractions.setText("Tương tác: " + product.getInteractions());

            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(imgProduct);
            } else {
                imgProduct.setImageResource(R.drawable.ic_launcher_foreground);
            }

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : null;

            boolean isSeller = currentUserId != null && product.getSellerId() != null
                    && product.getSellerId().equals(currentUserId);

            if (isSeller) {
                btnEdit.setVisibility(View.VISIBLE);
                btnViewOffers.setVisibility(View.VISIBLE);
                btnAddToCart.setVisibility(View.GONE);
                tvStatus.setVisibility(View.VISIBLE);
                spinnerStatus.setVisibility(View.GONE);

                btnViewOffers.setOnClickListener(v -> {
                    Intent intent = new Intent(context, OfferListActivity.class);
                    intent.putExtra("productId", product.getId());
                    context.startActivity(intent);
                });

            } else {
                btnEdit.setVisibility(View.GONE);
                btnViewOffers.setVisibility(View.GONE);
                tvStatus.setVisibility(View.VISIBLE);
                spinnerStatus.setVisibility(View.GONE);
                // Giữ nguyên icon giỏ hàng luôn hiển thị
                btnAddToCart.setVisibility(View.VISIBLE);
            }

            // Đếm số đề nghị từ Firestore
            FirebaseFirestore.getInstance()
                    .collection("offers")
                    .whereEqualTo("productId", product.getId())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        int offerCount = snapshot.size();
                        tvOfferCount.setText("Đề nghị: " + offerCount);
                    });

            itemView.setOnClickListener(v -> {
                // Tăng lượt xem trong Realtime DB
                FirebaseDatabase.getInstance().getReference("products")
                        .child(product.getId())
                        .child("views")
                        .setValue(product.getViews() + 1);

                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("productId", product.getId());
                context.startActivity(intent);
            });

            btnAddToCart.setOnClickListener(v -> {
                if (currentUserId == null) {
                    Toast.makeText(context, "Bạn cần đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Tăng tương tác sản phẩm trong Realtime DB
                FirebaseDatabase.getInstance().getReference("products")
                        .child(product.getId())
                        .child("interactions")
                        .setValue(product.getInteractions() + 1);

                // Thêm vào giỏ hàng trong Firestore
                Map<String, Object> cartItem = new HashMap<>();
                cartItem.put("productId", product.getId());
                cartItem.put("name", product.getName());
                cartItem.put("price", product.getPrice());
                cartItem.put("imageUrl", product.getImageUrl() != null ? product.getImageUrl() : "");
                cartItem.put("timestamp", System.currentTimeMillis());

                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(currentUserId)
                        .collection("cart")
                        .document(product.getId())
                        .set(cartItem)
                        .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(context, "Lỗi khi thêm vào giỏ", Toast.LENGTH_SHORT).show());
            });

            btnEdit.setOnClickListener(v -> {
                tvStatus.setVisibility(View.GONE);
                spinnerStatus.setVisibility(View.VISIBLE);

                Intent intent = new Intent(context, EditProductActivity.class);
                intent.putExtra("productId", product.getId());
                context.startActivity(intent);
            });

            // Quản lý Spinner trạng thái sản phẩm
            Map<String, String> displayToCode = new HashMap<>();
            displayToCode.put("Đang bán", "dang_ban");
            displayToCode.put("Tạm dừng", "tam_dung");
            displayToCode.put("Đã bán", "da_ban");

            Map<String, String> codeToDisplay = new HashMap<>();
            codeToDisplay.put("dang_ban", "Đang bán");
            codeToDisplay.put("tam_dung", "Tạm dừng");
            codeToDisplay.put("da_ban", "Đã bán");

            String[] optionsDisplay = {"Đang bán", "Tạm dừng", "Đã bán"};
            ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, optionsDisplay);
            statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerStatus.setAdapter(statusAdapter);

            int selectedIndex = Arrays.asList(optionsDisplay).indexOf(codeToDisplay.getOrDefault(product.getStatus(), "Đang bán"));
            spinnerStatus.setSelection(selectedIndex >= 0 ? selectedIndex : 0);

            spinnerStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                boolean firstLoad = true;
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (firstLoad) {
                        firstLoad = false;
                        return;
                    }
                    String selectedDisplay = optionsDisplay[position];
                    String selectedCode = displayToCode.get(selectedDisplay);
                    if (!selectedCode.equals(product.getStatus())) {
                        FirebaseDatabase.getInstance().getReference("products")
                                .child(product.getId())
                                .child("status")
                                .setValue(selectedCode)
                                .addOnSuccessListener(unused -> {
                                    product.setStatus(selectedCode);
                                    tvStatus.setText(selectedDisplay);
                                    Toast.makeText(context, "Cập nhật trạng thái thành công", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> Toast.makeText(context, "Lỗi khi cập nhật trạng thái", Toast.LENGTH_SHORT).show());
                    }
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
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
