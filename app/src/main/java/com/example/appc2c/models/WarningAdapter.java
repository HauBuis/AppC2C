package com.example.appc2c.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appc2c.R;

import java.util.List;

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
        holder.txtReason.setText("Lý do: " + warning.getReason());
        holder.txtTimestamp.setText("Thời gian: " + warning.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return warningList.size();
    }

    public static class WarningViewHolder extends RecyclerView.ViewHolder {
        TextView txtReason, txtTimestamp;

        public WarningViewHolder(@NonNull View itemView) {
            super(itemView);
            txtReason = itemView.findViewById(R.id.txtWarningReason);
            txtTimestamp = itemView.findViewById(R.id.txtWarningTimestamp);
        }
    }
}
