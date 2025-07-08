package com.example.appc2c.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appc2c.R;
import com.example.appc2c.products.Product;

import java.util.List;

public class ModerationAdapter extends RecyclerView.Adapter<ModerationAdapter.ModerationViewHolder> {
    private Context context;
    private List<Product> productList;

    public ModerationAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ModerationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_moderation, parent, false);
        return new ModerationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModerationViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(product.getPrice());
        Glide.with(context).load(product.getImageUrls().get(0)).into(holder.imgProduct);

        // Bổ sung xử lý nút nếu cần
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ModerationViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPrice;
        ImageView imgProduct;
        Button btnDelete, btnSuspend, btnWarn;

        public ModerationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnSuspend = itemView.findViewById(R.id.btnSuspend);
            btnWarn = itemView.findViewById(R.id.btnWarn);
        }
    }
}
