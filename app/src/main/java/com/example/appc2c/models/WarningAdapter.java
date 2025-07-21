package com.example.appc2c.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WarningAdapter extends RecyclerView.Adapter<WarningAdapter.WarningViewHolder> {

    private final Context context;
    private final List<Warning> warningList;

    public WarningAdapter(Context context, List<Warning> warningList) {
        this.context = context;
        this.warningList = warningList;
    }

    @NonNull
    @Override
    public WarningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_warning, parent, false);
        return new WarningViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WarningViewHolder holder, int position) {
        Warning warning = warningList.get(position);

        // Lý do (nếu không có reason thì lấy message)
        holder.txtReason.setText("Lý do: " +
                (warning.getReason() != null && !warning.getReason().isEmpty() ? warning.getReason() : warning.getMessage())
        );

        // User ID
        holder.txtUser.setText("User ID: " + (warning.getUserId() != null ? warning.getUserId() : ""));

        // Định dạng thời gian đẹp, ưu tiên timestamp (Firestore), nếu không lấy timeMillis (Realtime DB)
        String timeString = "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        if (warning.getTimestamp() != null) {
            Date date = warning.getTimestamp().toDate();
            timeString = sdf.format(date);
        } else if (warning.getTimeMillis() > 0) {
            Date date = new Date(warning.getTimeMillis());
            timeString = sdf.format(date);
        }
        holder.txtTimestamp.setText("Thời gian: " + timeString);
    }

    @Override
    public int getItemCount() {
        return warningList.size();
    }

    public static class WarningViewHolder extends RecyclerView.ViewHolder {
        TextView txtReason, txtTimestamp, txtUser;

        public WarningViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReason = itemView.findViewById(R.id.txtWarningReason);
            txtTimestamp = itemView.findViewById(R.id.txtWarningTimestamp);
            txtUser = itemView.findViewById(R.id.txtWarningUser);
        }
    }
}
