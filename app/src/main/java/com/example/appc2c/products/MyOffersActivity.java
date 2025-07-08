package com.example.appc2c.products;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.example.appc2c.models.Offer;
import com.example.appc2c.models.OfferAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MyOffersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ArrayList<Offer> offerList;
    private OfferAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offer_list);

        recyclerView = findViewById(R.id.offerListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        offerList = new ArrayList<>();
        adapter = new OfferAdapter(this, offerList);
        recyclerView.setAdapter(adapter);

        loadMyOffers();
    }

    private void loadMyOffers() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            Toast.makeText(this, "Chưa đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collectionGroup("offers")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    offerList.clear();
                    for (QueryDocumentSnapshot doc : query) {
                        Offer offer = doc.toObject(Offer.class);
                        offerList.add(offer);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải đề nghị", Toast.LENGTH_SHORT).show();
                });
    }
}
