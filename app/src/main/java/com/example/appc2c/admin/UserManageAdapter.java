package com.example.appc2c.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.example.appc2c.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserManageAdapter extends RecyclerView.Adapter<UserManageAdapter.UserViewHolder> {

    private final Context context;
    private final List<User> userList;

    public UserManageAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user_manage, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.txtEmail.setText("Email: " + (user.getEmail() != null ? user.getEmail() : ""));
        holder.txtName.setText("Tên: " + (user.getName() != null ? user.getName() : ""));
        holder.txtRole.setText("Role: " + (user.getRole() != null ? user.getRole() : ""));

        holder.btnDelete.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getId())
                    .delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Đã xóa tài khoản", Toast.LENGTH_SHORT).show();
                        userList.remove(position);
                        notifyItemRemoved(position);
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Lỗi xóa user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtEmail, txtName, txtRole;
        Button btnDelete;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtEmail = itemView.findViewById(R.id.txtUserEmail);
            txtName = itemView.findViewById(R.id.txtUserName);
            txtRole = itemView.findViewById(R.id.txtRole);
            btnDelete = itemView.findViewById(R.id.btnDeleteUser);
        }
    }
}
