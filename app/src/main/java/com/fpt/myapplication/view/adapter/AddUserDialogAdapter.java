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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddUserDialogAdapter extends RecyclerView.Adapter<AddUserDialogAdapter.ViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(UserResponse user, int position);
    }

    private OnItemClickListener listener;

    public void setOnItemClick(OnItemClickListener l) {
        this.listener = l;
    }
    List<UserResponse> data = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.add_member_dialog_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserResponse user = data.get(position);
        holder.tvName.setText(user.getDisplayName());
        holder.tvEmail.setText(user.getEmail());

        String avatar = user.getAvatar();
        holder.imgAvatar.setImageResource(R.drawable.default_avatar);
        Glide.with(holder.imgAvatar.getContext())
                .load(FileUtil.GetImageUrl(avatar))
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(holder.imgAvatar);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(user, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        CircleImageView imgAvatar;
        TextView tvName , tvEmail;
        public ViewHolder(View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
        }
    };

    public void submitList(List<UserResponse> users) {
        data.clear();
        if (users != null) data.addAll(users);
        notifyDataSetChanged();
    }

}
