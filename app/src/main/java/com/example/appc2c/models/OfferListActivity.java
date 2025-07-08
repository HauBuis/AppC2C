package com.example.appc2c.models;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OfferListActivity extends AppCompatActivity {

    private RecyclerView offerListView;
    private ArrayList<Offer> offerList;
    private OfferAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_list);

        offerListView = findViewById(R.id.offerListView);
        offerListView.setLayoutManager(new LinearLayoutManager(this));

        offerList = new ArrayList<>();
        adapter = new OfferAdapter(this, offerList);
        offerListView.setAdapter(adapter);

        loadOffers();
    }

    private void loadOffers() {
        String sellerId = FirebaseAuth.getInstance().getUid();
        if (sellerId == null) {
            Toast.makeText(this, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("offers")
                .whereEqualTo("sellerId", sellerId)
                .get()
                .addOnSuccessListener(query -> {
                    offerList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Offer offer = doc.toObject(Offer.class);
                        offer.setId(doc.getId());
                        offerList.add(offer);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("OfferList", "Lỗi tải đề nghị", e);
                    Toast.makeText(this, "Lỗi tải danh sách!", Toast.LENGTH_SHORT).show();
                });
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

                    // Kiểm duyệt từ cấm
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
                    data.put("timestamp", FieldValue.serverTimestamp());

                    FirebaseFirestore.getInstance()
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
