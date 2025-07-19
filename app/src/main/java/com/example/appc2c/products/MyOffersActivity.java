package com.example.appc2c.products;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.example.appc2c.models.Offer;
import com.example.appc2c.models.OfferAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyOffersActivity extends AppCompatActivity {

    private static final String TAG = "MyOffersActivity";

    private RecyclerView recyclerView;
    private ArrayList<Offer> offerList;
    private OfferAdapter adapter;
    private String productId;
    private ListenerRegistration offersListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_list);

        recyclerView = findViewById(R.id.offerListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
        recyclerView.setAdapter(adapter);

        productId = getIntent().getStringExtra("productId");
        Log.d(TAG, "Received productId: " + productId);
        if (productId == null || productId.isEmpty()) {
            Toast.makeText(this, "Không xác định sản phẩm!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        listenOffers();
    }

    private void listenOffers() {
        if (offersListener != null) {
            offersListener.remove();
        }
        offersListener = FirebaseFirestore.getInstance()
                .collection("offers")
                .whereEqualTo("productId", productId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot snapshots, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.e(TAG, "Error loading offers", e);
                            Toast.makeText(MyOffersActivity.this, "Lỗi tải đề nghị!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        offerList.clear();
                        if (snapshots != null) {
                            Log.d(TAG, "Offers count: " + snapshots.size());
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                Offer offer = doc.toObject(Offer.class);
                                if (offer != null) {
                                    offer.setId(doc.getId());
                                    Log.d(TAG, "Offer loaded: id=" + offer.getId() + ", proposedPrice=" + offer.getProposedPrice());
                                    offerList.add(offer);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void respondOffer(Offer offer, String status) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);

        FirebaseFirestore.getInstance()
                .collection("offers")
                .document(offer.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    // Listener tự cập nhật danh sách nên không gọi load lại
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

                Map<String, Object> updates = new HashMap<>();
                updates.put("proposedPrice", counterPrice);
                updates.put("status", "countered");

                FirebaseFirestore.getInstance()
                        .collection("offers")
                        .document(offer.getId())
                        .update(updates)
                        .addOnSuccessListener(unused -> Toast.makeText(this, "Đã phản hồi với giá mới!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Lỗi phản hồi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Giá không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showRatingDialog(String userId) {
        Toast.makeText(this, "Hiện dialog đánh giá cho userId: " + userId, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        if (offersListener != null) {
            offersListener.remove();
        }
        super.onDestroy();
    }
}
