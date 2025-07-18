package com.example.appc2c.products;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appc2c.R;
import com.example.appc2c.models.User;

import java.util.List;

public class SellerAdapter extends RecyclerView.Adapter<SellerAdapter.SellerViewHolder> {

    private final Context context;
    private final List<User> sellerList;
    private OnItemClickListener listener;

    public SellerAdapter(Context context, List<User> sellerList) {
        this.context = context;
        this.sellerList = sellerList;
    }

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SellerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_seller, parent, false);
        return new SellerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerViewHolder holder, int position) {
        User user = sellerList.get(position);
        holder.tvSellerName.setText(user.getName());
        holder.tvSellerEmail.setText(user.getEmail());
        Glide.with(context).load(user.getAvatar()).into(holder.imgSellerAvatar);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return sellerList.size();
    }

    public static class SellerViewHolder extends RecyclerView.ViewHolder {
        TextView tvSellerName, tvSellerEmail;
        ImageView imgSellerAvatar;

        public SellerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSellerName = itemView.findViewById(R.id.tvSellerName);
            tvSellerEmail = itemView.findViewById(R.id.tvSellerEmail);
            imgSellerAvatar = itemView.findViewById(R.id.imgSellerAvatar);
        }
    }
}
