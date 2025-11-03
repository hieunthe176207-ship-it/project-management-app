package com.fpt.myapplication.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.response.ChatGroupResponse;

import java.util.ArrayList;
import java.util.List;

public class ChatGroupAdapter extends RecyclerView.Adapter<ChatGroupAdapter.VH> {


    List<ChatGroupResponse> chatGroups = new ArrayList<>();

    public void submitList(List<ChatGroupResponse> groups) {
        chatGroups.clear();
        chatGroups.addAll(groups);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_group_item, parent, false);
        return new ChatGroupAdapter.VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ChatGroupResponse group = chatGroups.get(position);
        holder.tvGroupName.setText(group.getName());
    }

    @Override
    public int getItemCount() {
        return chatGroups.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        private TextView tvGroupName;
        public VH(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tvGroupName);
        }
    }
}
