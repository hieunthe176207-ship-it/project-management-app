package com.fpt.myapplication.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {
    List<UserResponse> data = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.member_item, parent, false);
       return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserResponse user = data.get(position);
        holder.tvName.setText(user.getDisplayName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvRole.setText(user.getRole());

        String avatarUrl = user.getAvatar();               // ví dụ đã là full URL
        // String avatarUrl = FileUtil.GetImageUrl(user.getAvatar()); // nếu chỉ là path

        // Clear trước (tránh flicker khi recycle)
        holder.imgAvatar.setImageResource(R.drawable.default_avatar);

        // Load bằng Glide
        Glide.with(holder.imgAvatar.getContext())
                .load(FileUtil.GetImageUrl(avatarUrl))
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(holder.imgAvatar);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgAvatar;
        TextView tvName, tvEmail, tvRole;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
        }

    }

    public void submitList(List<UserResponse> users) {
        data.clear();
        if (users != null) {
            data.addAll(users);
        }
        notifyDataSetChanged();
    }
}
