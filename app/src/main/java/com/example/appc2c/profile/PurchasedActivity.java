package com.example.appc2c.profile;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.example.appc2c.products.Product;
import com.example.appc2c.products.ProductAdapter;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class PurchasedActivity extends AppCompatActivity {

    private ProductAdapter adapter;
    private final List<Product> purchasedList = new ArrayList<>();
    private DatabaseReference offersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchased);

        RecyclerView recycler = findViewById(R.id.recyclerPurchased);
        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this, purchasedList);
        recycler.setAdapter(adapter);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        offersRef = FirebaseDatabase.getInstance().getReference("offers");

        // Lấy danh sách sản phẩm đã mua
        offersRef.orderByChild("buyerId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        purchasedList.clear();
                        for (DataSnapshot offerSnap : snapshot.getChildren()) {
                            Boolean accepted = offerSnap.child("accepted").getValue(Boolean.class);
                            if (accepted != null && accepted) {
                                Product product = offerSnap.child("product").getValue(Product.class);
                                if (product != null) {
                                    purchasedList.add(product);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
        MaterialToolbar toolbar = findViewById(R.id.toolbarPurchased);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
