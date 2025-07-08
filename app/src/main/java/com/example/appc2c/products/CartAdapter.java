package com.example.appc2c.products;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appc2c.R;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final List<Product> cartItems;
    private final Runnable onSelectionChanged;

    public CartAdapter(Context context, List<Product> cartItems, Runnable onSelectionChanged) {
        this.context = context;
        this.cartItems = cartItems;
        this.onSelectionChanged = onSelectionChanged;
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        TextView tvName, tvPrice;
        ImageView imgProduct;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            imgProduct = itemView.findViewById(R.id.imgProduct);
        }
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Product item = cartItems.get(position);
        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(item.getPrice() + "đ");


        Glide.with(context).load(item.getImageUrl()).into(holder.imgProduct);

        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            onSelectionChanged.run();
        });

        holder.cbSelect.setChecked(item.isSelected());
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }
}
