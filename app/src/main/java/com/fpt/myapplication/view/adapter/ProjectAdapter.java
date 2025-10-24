package com.fpt.myapplication.view.adapter;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.response.ProjectResponse;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.util.FileUtil;
import com.google.android.material.chip.Chip;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private final List<ProjectResponse> data = new ArrayList<>();
    private OnProjectClickListener listener;

    public interface OnProjectClickListener {
        void onClick(ProjectResponse project);
    }

    public void setOnItemClick(OnProjectClickListener l) {
        this.listener = l;
    }

    public void submit(List<ProjectResponse> newData) {
        data.clear();
        if (newData != null) data.addAll(newData);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = View.inflate(parent.getContext(), R.layout.item_project, null);
        return new ProjectViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        ProjectResponse project = data.get(position);
        holder.tvTitle.setText(project.getName());
        holder.chipDeadline.setText("Deadline: " + project.getDeadline());
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(project);
        });

        Glide.with(holder.itemView).clear(holder.a1);
        Glide.with(holder.itemView).clear(holder.a2);
        Glide.with(holder.itemView).clear(holder.a3);
        Glide.with(holder.itemView).clear(holder.a4);

        holder.a1.setVisibility(View.GONE);
        holder.a2.setVisibility(View.GONE);
        holder.a3.setVisibility(View.GONE);
        holder.a4.setVisibility(View.GONE);
        holder.tvCount.setVisibility(View.GONE);
        holder.overlayCount.setVisibility(View.GONE);

        List<UserResponse> members = project.getMembers();
        int n = members == null ? 0 : members.size();

        if (n >= 1) {
            holder.a1.setVisibility(View.VISIBLE);
            loadAvatar(members.get(0).getAvatar(), holder.a1);
        }
        if (n >= 2) {
            holder.a2.setVisibility(View.VISIBLE);
            loadAvatar(members.get(1).getAvatar(), holder.a2);
        }
        if (n >= 3) {
            holder.a3.setVisibility(View.VISIBLE);
            loadAvatar(members.get(2).getAvatar(), holder.a3);
        }
        if (n >= 4) {
            holder.a4.setVisibility(View.VISIBLE);
            loadAvatar(members.get(3).getAvatar(), holder.a4);

            int remain = n - 4;
            if (remain > 0) {
                holder.tvCount.setText("+" + remain);
                holder.tvCount.setVisibility(View.VISIBLE);
                holder.overlayCount.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle;
        Chip chipDeadline;

        CircleImageView a1,a2,a3,a4;
        TextView tvCount;
        View overlayCount;
        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            chipDeadline = itemView.findViewById(R.id.chipDeadline);
            a1 = itemView.findViewById(R.id.memberAvatar1);
            a2 = itemView.findViewById(R.id.memberAvatar2);
            a3 = itemView.findViewById(R.id.memberAvatar3);
            a4 = itemView.findViewById(R.id.memberAvatar4);
            tvCount = itemView.findViewById(R.id.membersCount);
            overlayCount = itemView.findViewById(R.id.overlayCount);
        }
    }

    private void loadAvatar(String url, ImageView target){
        Glide.with(target.getContext())
                .load(FileUtil.GetImageUrl(url))
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(target);
    }
}
