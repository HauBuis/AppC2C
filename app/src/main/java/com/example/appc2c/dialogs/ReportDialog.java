package com.example.appc2c.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.appc2c.R;
import com.example.appc2c.admin.Report;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReportDialog extends DialogFragment {

    private String targetId;
    private String type;

    public static ReportDialog newInstance(String targetId, String type) {
        ReportDialog dialog = new ReportDialog();
        Bundle args = new Bundle();
        args.putString("targetId", targetId);
        args.putString("type", type);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            targetId = getArguments().getString("targetId");
            type = getArguments().getString("type");
        }

        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_report, null);
        RadioGroup radioGroup = view.findViewById(R.id.radioReasons);
        EditText edtOtherReason = new EditText(getContext());
        edtOtherReason.setId(View.generateViewId());
        edtOtherReason.setHint("Nhập lý do khác");
        edtOtherReason.setVisibility(View.GONE);
        ((ViewGroup) view).addView(edtOtherReason);

        Button btnSubmit = view.findViewById(R.id.btnSubmitReport);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(view);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selected = view.findViewById(checkedId);
            if (selected != null && "Khác".equals(selected.getText().toString())) {
                edtOtherReason.setVisibility(View.VISIBLE);
            } else {
                edtOtherReason.setVisibility(View.GONE);
            }
        });

        btnSubmit.setOnClickListener(v -> {
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(getContext(), "Vui lòng chọn lý do", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedRadio = view.findViewById(selectedId);
            String reason = selectedRadio.getText().toString();
            if ("Khác".equals(reason)) {
                reason = edtOtherReason.getText().toString().trim();
                if (reason.isEmpty()) {
                    Toast.makeText(getContext(), "Vui lòng nhập lý do cụ thể", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String userId = FirebaseAuth.getInstance().getUid();
            if (userId == null) {
                Toast.makeText(getContext(), "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }

            Report report = new Report();
            report.setId(FirebaseFirestore.getInstance().collection("reports").document().getId());
            report.setType(type);
            report.setTargetId(targetId);
            report.setReportedBy(userId);
            report.setReason(reason);

            FirebaseFirestore.getInstance()
                    .collection("reports")
                    .document(report.getId())
                    .set(report)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Đã gửi báo cáo", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        return builder.create();
    }
}
