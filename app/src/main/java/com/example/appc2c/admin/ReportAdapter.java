package com.example.appc2c.admin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final Context context;
    private final List<Report> reportList;
    private OnActionListener actionListener;

    public interface OnActionListener {
        void onAction(Report report);
    }

    public void setOnActionListener(OnActionListener listener) {
        this.actionListener = listener;
    }

    public ReportAdapter(Context context, List<Report> reportList) {
        this.context = context;
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);

        holder.txtType.setText("Loại: " + report.getType());
        holder.txtReason.setText("Lý do: " + report.getReason());
        holder.txtReporter.setText("Người báo cáo: " + report.getReportedBy());
        holder.txtTarget.setText("ID bị tố: " + report.getTargetId());

        // Xóa báo cáo
        holder.btnDelete.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("reports")
                    .document(report.getId())
                    .delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Đã xóa báo cáo", Toast.LENGTH_SHORT).show();
                        reportList.remove(position);
                        notifyItemRemoved(position);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Lỗi xóa báo cáo: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        // Gửi cảnh báo + notification + lưu vào user_warnings (toàn cục)
        holder.btnWarn.setOnClickListener(v -> {
            // 1. Ghi vào collection con warnings của user
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(report.getTargetId())
                    .collection("warnings")
                    .add(Map.of(
                            "message", "Bạn đã bị cảnh báo vì vi phạm nội dung.",
                            "reason", report.getReason(),
                            "reportId", report.getId(),
                            "timestamp", FieldValue.serverTimestamp()
                    ))
                    .addOnSuccessListener(unused ->
                            Toast.makeText(context, "Đã ghi cảnh báo vào hồ sơ user.", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Lỗi lưu cảnh báo vào user: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

            // 2. Ghi vào collection user_warnings (toàn cục, cho admin tra cứu)
            Map<String, Object> warningData = Map.of(
                    "userId", report.getTargetId(),
                    "reason", report.getReason(),
                    "message", "Bạn đã bị cảnh báo vì vi phạm nội dung.",
                    "reportedBy", report.getReportedBy(),
                    "reportId", report.getId(),
                    "timestamp", FieldValue.serverTimestamp()
            );
            FirebaseFirestore.getInstance()
                    .collection("user_warnings")
                    .add(warningData)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(context, "Đã lưu vào danh sách cảnh báo toàn hệ thống.", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Lỗi lưu cảnh báo tổng: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

            // 3. Gửi notification tới user bị cảnh báo
            sendNotification(report.getTargetId(), "Cảnh báo", "Bạn đã bị cảnh báo vì vi phạm nội dung.");

            // 4. Đánh dấu báo cáo đã xử lý
            FirebaseFirestore.getInstance()
                    .collection("reports")
                    .document(report.getId())
                    .update("status", "handled");
        });

        // Tạm ngưng tài khoản + notification
        holder.btnSuspend.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(report.getTargetId())
                    .set(Map.of("active", false), com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener(unused ->
                            Toast.makeText(context, "Đã tạm ngưng tài khoản", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Lỗi tạm ngưng tài khoản: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );

            // Gửi notification tới user bị tạm ngưng
            sendNotification(report.getTargetId(), "Tạm ngưng tài khoản", "Tài khoản của bạn đã bị tạm ngưng do vi phạm.");

            // Đánh dấu báo cáo đã xử lý
            FirebaseFirestore.getInstance()
                    .collection("reports")
                    .document(report.getId())
                    .update("status", "handled");
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReportDetailActivity.class);
            intent.putExtra("targetId", report.getTargetId());
            intent.putExtra("type", report.getType());
            context.startActivity(intent);

            if (actionListener != null) {
                actionListener.onAction(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    // Hàm gửi notification vào collection 'notifications'
    private void sendNotification(String userId, String title, String message) {
        Map<String, Object> noti = Map.of(
                "userId", userId,
                "title", title,
                "message", message,
                "timestamp", FieldValue.serverTimestamp(),
                "read", false
        );
        FirebaseFirestore.getInstance()
                .collection("notifications")
                .add(noti);
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView txtType, txtReason, txtReporter, txtTarget;
        Button btnDelete, btnWarn, btnSuspend;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            txtType = itemView.findViewById(R.id.txtType);
            txtReason = itemView.findViewById(R.id.txtReason);
            txtReporter = itemView.findViewById(R.id.txtReporter);
            txtTarget = itemView.findViewById(R.id.txtTarget);
            btnDelete = itemView.findViewById(R.id.btnDeleteReport);
            btnWarn = itemView.findViewById(R.id.btnWarnUser);
            btnSuspend = itemView.findViewById(R.id.btnSuspendUser);
        }
    }
}
