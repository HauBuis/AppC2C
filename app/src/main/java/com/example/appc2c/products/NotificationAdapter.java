package com.example.appc2c.products;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appc2c.R;
import com.example.appc2c.products.NotificationModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final Context context;
    private final List<NotificationModel> notificationList;

    public NotificationAdapter(Context context, List<NotificationModel> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationModel notification = notificationList.get(position);

        holder.txtTitle.setText(notification.getTitle() != null ? notification.getTitle() : "Thông báo");
        holder.txtMessage.setText(notification.getMessage() != null ? notification.getMessage() : "");

        // Hiển thị thời gian đọc từ Firestore Timestamp
        String timeStr = "";
        if (notification.getTimestamp() != null) {
            Date date = notification.getTimestamp().toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            timeStr = sdf.format(date);
        }
        holder.txtTimestamp.setText("Thời gian: " + timeStr);
    }

    @Override
    public int getItemCount() {
        return (notificationList != null) ? notificationList.size() : 0;
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtMessage, txtTimestamp;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtNotificationTitle);
            txtMessage = itemView.findViewById(R.id.txtNotificationMessage);
            txtTimestamp = itemView.findViewById(R.id.txtNotificationTimestamp);
        }
    }
}
