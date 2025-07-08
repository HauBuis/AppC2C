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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    private final Context context;
    private final List<Offer> offerList;

    public OfferAdapter(Context context, List<Offer> offerList) {
        this.context = context;
        this.offerList = offerList;
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

        // Ẩn/hiện nút theo trạng thái
        if ("pending".equalsIgnoreCase(offer.getStatus())) {
            holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnCounter.setVisibility(View.VISIBLE);
        } else {
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnCounter.setVisibility(View.GONE);
        }

        // Sự kiện: Chấp nhận
        holder.btnAccept.setOnClickListener(v -> updateOfferStatus(offer, "accepted"));

        // Sự kiện: Từ chối
        holder.btnReject.setOnClickListener(v -> updateOfferStatus(offer, "rejected"));

        // Sự kiện: Phản hồi
        holder.btnCounter.setOnClickListener(v -> showCounterOfferDialog(offer));
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

    // Cập nhật trạng thái lên Firestore và làm mới danh sách
    private void updateOfferStatus(Offer offer, String status) {
        FirebaseFirestore.getInstance()
                .collection("offers")
                .document(offer.getId())
                .update("status", status)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context, "Đã cập nhật trạng thái: " + status, Toast.LENGTH_SHORT).show();
                    offer.setStatus(status); // cập nhật local
                    notifyDataSetChanged();  // làm mới RecyclerView
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Phản hồi đề nghị: mở dialog nhập giá mới
    private void showCounterOfferDialog(Offer offer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Phản hồi giá");

        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nhập giá phản hồi");
        builder.setView(input);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String priceStr = input.getText().toString().trim();
            if (priceStr.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập giá", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int counterPrice = Integer.parseInt(priceStr);

                FirebaseFirestore.getInstance()
                        .collection("offers")
                        .document(offer.getId())
                        .update("proposedPrice", counterPrice, "status", "countered")
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Đã phản hồi với giá mới!", Toast.LENGTH_SHORT).show();
                            offer.setProposedPrice(counterPrice);
                            offer.setStatus("countered");
                            notifyDataSetChanged();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Lỗi phản hồi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            } catch (NumberFormatException e) {
                Toast.makeText(context, "Giá không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
