package com.example.appc2c.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.appc2c.R;

public class PublicProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile);

        ImageView imgAvatar = findViewById(R.id.imgAvatar);
        TextView txtName = findViewById(R.id.txtName);
        TextView txtEmail = findViewById(R.id.txtEmail);

        Intent intent = getIntent();
        String userName = intent.getStringExtra("userName");
        String userEmail = intent.getStringExtra("userEmail");
        String userAvatar = intent.getStringExtra("userAvatar");

        txtName.setText(userName != null ? userName : "Người dùng");
        txtEmail.setText(userEmail != null ? userEmail : "Không có email");

        if (userAvatar != null && !userAvatar.isEmpty()) {
            Glide.with(this).load(userAvatar).placeholder(R.drawable.ic_person).into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.ic_person);
        }
    }
}