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

public class SoldActivity extends AppCompatActivity {

    private ProductAdapter adapter;
    private final List<Product> soldList = new ArrayList<>();
    private DatabaseReference offersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sold);

        RecyclerView recycler = findViewById(R.id.recyclerSold);
        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ProductAdapter(this, soldList);
        recycler.setAdapter(adapter);

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        offersRef = FirebaseDatabase.getInstance().getReference("offers");

        // Lấy danh sách sản phẩm đã bán
        offersRef.orderByChild("sellerId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        soldList.clear();
                        for (DataSnapshot offerSnap : snapshot.getChildren()) {
                            Boolean accepted = offerSnap.child("accepted").getValue(Boolean.class);
                            if (accepted != null && accepted) {
                                Product product = offerSnap.child("product").getValue(Product.class);
                                if (product != null) {
                                    soldList.add(product);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
        MaterialToolbar toolbar = findViewById(R.id.toolbarSold);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
}
