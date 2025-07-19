package com.example.appc2c.models;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    public interface OfferActionListener {
        void onAccept(Offer offer);
        void onReject(Offer offer);
        void onCounter(Offer offer);
        void onRate(String userId);
    }

    private final Context context;
    private final List<Offer> offerList;
    private OfferActionListener actionListener;

    public OfferAdapter(Context context, List<Offer> offerList, OfferActionListener listener) {
        this.context = context;
        this.offerList = offerList;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public OfferViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfferViewHolder holder, int position) {
        Offer offer = offerList.get(position);

        holder.txtOfferPrice.setText("Giá đề nghị: " + String.format("%,d VNĐ", offer.getProposedPrice()));
        holder.txtOfferNote.setText("Ghi chú: " + offer.getNote() + "\nTrạng thái: " + offer.getStatus());

        if ("pending".equalsIgnoreCase(offer.getStatus())) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnCounter.setVisibility(View.VISIBLE);
        } else {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnCounter.setVisibility(View.GONE);
        }

        holder.btnAccept.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onAccept(offer);
        });

        holder.btnReject.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onReject(offer);
        });

        holder.btnCounter.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onCounter(offer);
        });

        // Long click để đánh giá người dùng (ví dụ)
        holder.itemView.setOnLongClickListener(v -> {
            if (actionListener != null && offer.getBuyerId() != null) {
                actionListener.onRate(offer.getBuyerId());
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return offerList.size();
    }

    public static class OfferViewHolder extends RecyclerView.ViewHolder {
        TextView txtOfferPrice, txtOfferNote;
        Button btnAccept, btnReject, btnCounter;

        public OfferViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOfferPrice = itemView.findViewById(R.id.txtOfferPrice);
            txtOfferNote = itemView.findViewById(R.id.txtOfferNote);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
            btnCounter = itemView.findViewById(R.id.btnCounter);
        }
    }
}
