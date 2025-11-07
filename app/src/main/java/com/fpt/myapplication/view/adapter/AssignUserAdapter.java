package com.fpt.myapplication.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AssignUserAdapter extends RecyclerView.Adapter<AssignUserAdapter.VH> {

    public interface OnItemClickListener {
        void onItemClick(@NonNull UserResponse user, int position);
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(@NonNull UserResponse user, int position);
    }

    private OnItemClickListener itemClickListener;
    private OnRemoveClickListener removeClickListener;

    public void setOnItemClickListener(OnItemClickListener l) { this.itemClickListener = l; }
    public void setOnRemoveClickListener(OnRemoveClickListener l) { this.removeClickListener = l; }

    // === Data ===
    private final List<UserResponse> data = new ArrayList<>();
    private boolean showRemove = false;

    /** Bật/tắt nút xoá ở item (mặc định: false) */
    public void setShowRemove(boolean show) {
        this.showRemove = show;
        notifyDataSetChanged();
    }

    /** Nạp danh sách mới */
    public void submitList(List<UserResponse> users) {
        data.clear();
        if (users != null) data.addAll(users);
        notifyDataSetChanged();
    }

    /** Lấy item theo vị trí */
    public UserResponse getItem(int position) {
        return data.get(position);
    }

    /** Xoá phần tử tại vị trí (đồng bộ RecyclerView) */
    public void removeAt(int position) {
        if (position < 0 || position >= data.size()) return;
        data.remove(position);
        notifyItemRemoved(position);
    }

    /** Trả về toàn bộ danh sách hiện tại (copy) */
    public List<UserResponse> getCurrent() {
        return new ArrayList<>(data);
    }

    // === RecyclerView.Adapter ===

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.assign_user_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        UserResponse user = data.get(position);

        // Bind text
        h.tvName.setText(user.getDisplayName());
        h.tvEmail.setText(user.getEmail());

        // Avatar
        h.imgAvatar.setImageResource(R.drawable.default_avatar);
        Glide.with(h.imgAvatar.getContext())
                .load(FileUtil.GetImageUrl(user.getAvatar()))
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(h.imgAvatar);

        // Nút xoá hiển thị theo mode

        // Clicks
        h.btnRemove.setOnClickListener(v -> {
            if (itemClickListener != null) {
                int pos = h.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    itemClickListener.onItemClick(data.get(pos), pos);
                }
            }
        });

        h.btnRemove.setOnClickListener(v -> {
            if (removeClickListener != null) {
                int pos = h.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    removeClickListener.onRemoveClick(data.get(pos), pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // === ViewHolder ===
    static class VH extends RecyclerView.ViewHolder {
        CircleImageView imgAvatar;
        TextView tvName, tvEmail;
        ImageButton btnRemove;

        VH(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName    = itemView.findViewById(R.id.tvName);
            tvEmail   = itemView.findViewById(R.id.tvEmail);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}
