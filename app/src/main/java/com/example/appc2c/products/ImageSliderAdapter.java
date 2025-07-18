package com.example.appc2c.products;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.appc2c.R;

import java.util.ArrayList;
import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder> {

    private final Context context;
    private List<String> imageUrls = new ArrayList<>();

    public ImageSliderAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        if (imageUrls != null) {
            this.imageUrls = imageUrls;
        }
    }

    public void updateImages(List<String> newImages) {
        this.imageUrls = newImages != null ? newImages : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image_slider, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = imageUrls.get(position);
        if (url != null && url.startsWith("http")) {
            Glide.with(context)
                    .load(url)
                    .placeholder(R.drawable.placeholder) // thêm placeholder nếu muốn
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.imgSlide);
        }
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSlide;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSlide = itemView.findViewById(R.id.imgSlide);
        }
    }
}
