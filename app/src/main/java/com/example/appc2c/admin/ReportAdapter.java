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

        // Hành động xử lý
        holder.btnDelete.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("reports")
                    .document(report.getId())
                    .delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(context, "Đã xóa báo cáo", Toast.LENGTH_SHORT).show();
                        reportList.remove(position);
                        notifyItemRemoved(position);
                    });
        });

        holder.btnWarn.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(report.getTargetId())
                    .collection("warnings")
                    .add(Map.of(
                            "message", "Bạn đã bị cảnh báo vì vi phạm nội dung",
                            "timestamp", FieldValue.serverTimestamp()
                    ))
                    .addOnSuccessListener(unused -> Toast.makeText(context, "Đã ghi cảnh báo vào hồ sơ", Toast.LENGTH_SHORT).show());

            FirebaseFirestore.getInstance()
                    .collection("reports")
                    .document(report.getId())
                    .update("status", "handled");
        });

        holder.btnSuspend.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(report.getTargetId())
                    .update("active", false)
                    .addOnSuccessListener(unused -> Toast.makeText(context, "Đã tạm ngưng tài khoản", Toast.LENGTH_SHORT).show());

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
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
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