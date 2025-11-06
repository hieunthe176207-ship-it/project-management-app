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
import com.fpt.myapplication.util.Util;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class JoinRequestAdapter extends RecyclerView.Adapter<JoinRequestAdapter.ViewHolder> {

    private List<UserResponse> joinRequests;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onAccept(UserResponse user, int position);
        void onDecline(UserResponse user, int position);
    }

    public JoinRequestAdapter(List<UserResponse> joinRequests) {
        this.joinRequests = joinRequests != null ? joinRequests : new ArrayList<>();
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    public void updateData(List<UserResponse> newData) {
        this.joinRequests = newData != null ? newData : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < joinRequests.size()) {
            joinRequests.remove(position);
            notifyItemRemoved(position);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.join_request_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserResponse user = joinRequests.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return joinRequests.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imgAvatar;
        private TextView tvName;
        private TextView tvEmail;
        private ImageButton btnAccept;
        private ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnRemove = itemView.findViewById(R.id.btnRemove);

            btnAccept.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onAccept(joinRequests.get(position), position);
                }
            });

            btnRemove.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDecline(joinRequests.get(position), position);
                }
            });
        }

        public void bind(UserResponse user) {
            tvName.setText(user.getDisplayName() != null ? user.getDisplayName() : "Unknown");
            tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");

            // Load avatar using Glide or Picasso
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(FileUtil.GetImageUrl(user.getAvatar()))
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.drawable.default_avatar);
            }
        }
    }
}
