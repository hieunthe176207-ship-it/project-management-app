// SubTaskAdapter.java
package com.fpt.myapplication.view.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.response.SubTaskResponse;

import java.util.ArrayList;
import java.util.List;

public class SubTaskAdapter extends RecyclerView.Adapter<SubTaskAdapter.VH> {

    public interface OnToggleRequestListener {
        // requested: trạng thái người dùng MUỐN đổi sang; previous: trạng thái hiện tại trước khi click
        void onToggleRequest(SubTaskResponse item, boolean requested, boolean previous, int position, VH holder);
    }

    private final List<SubTaskResponse> data = new ArrayList<>();
    private OnToggleRequestListener toggleListener;

    public OnToggleRequestListener getOnToggleRequestListener() {
        return toggleListener;
    }
    public void setOnToggleRequestListener(OnToggleRequestListener l) { this.toggleListener = l; }

    public static class VH extends RecyclerView.ViewHolder {
        public final CheckBox cbDone;
        public final TextView tvTitle;
        public VH(@NonNull View itemView) {
            super(itemView);
            cbDone  = itemView.findViewById(R.id.cbDone);
            tvTitle = itemView.findViewById(R.id.tvSubtaskTitle);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sub_task_detail, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        SubTaskResponse st = data.get(position);

        h.tvTitle.setText(st.getTitle() != null ? st.getTitle() : "");

        // bind checked without firing listener
        h.cbDone.setOnCheckedChangeListener(null);
        h.cbDone.setChecked(st.isCompleted());
        applyStrike(h, st.isCompleted());

        h.cbDone.setOnCheckedChangeListener((button, requestedChecked) -> {
            int pos = h.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            boolean previous = st.isCompleted();

            // Trả UI về trạng thái cũ ngay (chưa “tick” nếu chưa có success)
            h.cbDone.setOnCheckedChangeListener(null);
            h.cbDone.setChecked(previous);
            applyStrike(h, previous);
            h.cbDone.setOnCheckedChangeListener((b, c) -> {});

            // Gọi ra Activity để call API
            if (toggleListener != null) {
                toggleListener.onToggleRequest(st, requestedChecked, previous, pos, h);
            }
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    public void submitAll(List<SubTaskResponse> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public void addOne(SubTaskResponse s) {
        data.add(s);
        notifyItemInserted(data.size() - 1);
    }


    public void applyServerChecked(int position, boolean checked) {
        if (position < 0 || position >= data.size()) return;
        SubTaskResponse st = data.get(position);
        st.setCompleted(checked);
        notifyItemChanged(position);
    }

    private void applyStrike(VH h, boolean done) {
        if (done) {
            h.tvTitle.setPaintFlags(h.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.itemView.setAlpha(0.9f);
        } else {
            h.tvTitle.setPaintFlags(h.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            h.itemView.setAlpha(1f);
        }
    }
}
