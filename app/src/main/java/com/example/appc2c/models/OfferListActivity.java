package com.example.appc2c.models;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OfferListActivity extends AppCompatActivity {

    private RecyclerView offerListView;
    private ArrayList<Offer> offerList;
    private OfferAdapter adapter;
    private String productId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_list);

        offerListView = findViewById(R.id.offerListView);
        offerListView.setLayoutManager(new LinearLayoutManager(this));

        offerList = new ArrayList<>();
        adapter = new OfferAdapter(this, offerList, new OfferAdapter.OfferActionListener() {
            @Override
            public void onAccept(Offer offer) {
                respondOffer(offer, "accepted");
            }

            @Override
            public void onReject(Offer offer) {
                respondOffer(offer, "rejected");
            }

            @Override
            public void onCounter(Offer offer) {
                showCounterOfferDialog(offer);
            }

            @Override
            public void onRate(String userId) {
                showRatingDialog(userId);
            }
        });
        offerListView.setAdapter(adapter);

        productId = getIntent().getStringExtra("productId");
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Không xác định sản phẩm!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadOffers();
    }

    private void loadOffers() {
        FirebaseDatabase.getInstance()
                .getReference("offers")
                .orderByChild("productId")
                .equalTo(productId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        offerList.clear();
                        for (DataSnapshot doc : snapshot.getChildren()) {
                            Offer offer = doc.getValue(Offer.class);
                            if (offer != null) {
                                offer.setId(doc.getKey());
                                offerList.add(offer);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Log.e("OfferList", "Lỗi tải đề nghị", error.toException());
                        Toast.makeText(OfferListActivity.this, "Lỗi tải danh sách!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void respondOffer(Offer offer, String status) {
        FirebaseDatabase.getInstance()
                .getReference("offers")
                .child(offer.getId())
                .child("status")
                .setValue(status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    loadOffers();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật!", Toast.LENGTH_SHORT).show());
    }

    private void showCounterOfferDialog(Offer offer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Phản hồi giá");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("Nhập giá phản hồi");
        builder.setView(input);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String priceStr = input.getText().toString().trim();
            if (priceStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập giá", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                int counterPrice = Integer.parseInt(priceStr);

                FirebaseDatabase.getInstance()
                        .getReference("offers")
                        .child(offer.getId())
                        .child("proposedPrice")
                        .setValue(counterPrice);

                FirebaseDatabase.getInstance()
                        .getReference("offers")
                        .child(offer.getId())
                        .child("status")
                        .setValue("countered");

                Toast.makeText(this, "Đã phản hồi với giá mới!", Toast.LENGTH_SHORT).show();
                loadOffers();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Giá không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void showRatingDialog(String targetUserId) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rating, null);
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        EditText edtComment = dialogView.findViewById(R.id.edtComment);

        new AlertDialog.Builder(this)
                .setTitle("Đánh giá người dùng")
                .setView(dialogView)
                .setPositiveButton("Gửi", (dialog, which) -> {
                    float rating = ratingBar.getRating();
                    String comment = edtComment.getText().toString().trim();

                    String[] bannedWords = {"đm", "vkl", "vãi", "cặc", "lồn", "shit", "fuck"};
                    for (String word : bannedWords) {
                        if (comment.toLowerCase().contains(word)) {
                            Toast.makeText(this, "Nhận xét chứa từ ngữ không phù hợp", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put("rating", rating);
                    data.put("comment", comment);
                    data.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(targetUserId)
                            .collection("ratings")
                            .add(data)
                            .addOnSuccessListener(docRef -> Toast.makeText(this, "Đã gửi đánh giá", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi gửi đánh giá", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
