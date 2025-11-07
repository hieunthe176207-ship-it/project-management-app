package com.fpt.myapplication.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.response.ChatGroupResponse;
import com.fpt.myapplication.util.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class ChatGroupAdapter extends RecyclerView.Adapter<ChatGroupAdapter.VH> {

    public interface OnGroupClickListener { void onClick(ChatGroupResponse group); }

    public ChatGroupResponse getItemAt(int position) { return chatGroups.get(position); }

    private final List<ChatGroupResponse> chatGroups = new ArrayList<>();
    private OnGroupClickListener listener;

    public void setOnGroupClickListener(OnGroupClickListener l) { this.listener = l; }

    public void submitList(List<ChatGroupResponse> groups) {
        chatGroups.clear();
        if (groups != null) chatGroups.addAll(groups);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_group_item, parent, false);
        return new VH(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(chatGroups.get(position));
    }

    @Override
    public int getItemCount() { return chatGroups.size(); }

    static class VH extends RecyclerView.ViewHolder {
        de.hdodenhof.circleimageview.CircleImageView imgGroup;
        TextView tvGroupName, tvLastMessage;
        ChatGroupResponse current;

        VH(@NonNull View itemView, OnGroupClickListener listener) {
            super(itemView);
            imgGroup     = itemView.findViewById(R.id.imgGroup);
            tvGroupName  = itemView.findViewById(R.id.tvGroupName);
            tvLastMessage= itemView.findViewById(R.id.tvLastMessage);

            itemView.setOnClickListener(v -> {
                if (listener == null) return;
                int pos = getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION || current == null) return;
                listener.onClick(current);
            });
        }

        void bind(ChatGroupResponse g) {
            current = g;

            // Tên nhóm
            tvGroupName.setText(g.getName());

            // Preview: "LastUser: Last message"
            String preview;
            if (g.getLastMessage() != null && g.getLastUser() != null) {
                preview = g.getLastUser().getDisplayName() + ": " + g.getLastMessage().getContent();
            } else if (g.getLastMessage() != null) {
                preview = g.getLastMessage().getContent();
            } else {
                preview = "Chưa có tin nhắn";
            }
            tvLastMessage.setText(preview);

            // Đậm khi hasNew = true, ngược lại NORMAL
            int style = g.isHasNew() ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL;

            tvGroupName.setTypeface(null, style);
            tvLastMessage.setTypeface(null, style);

            // Avatar nhóm (nếu bạn có URL trong g.getAvatar())
            if (g.getAvatar() != null && !g.getAvatar().isEmpty()) {
                com.bumptech.glide.Glide.with(imgGroup.getContext())
                        .load(FileUtil.GetImageUrl(g.getAvatar()))
                        .placeholder(R.drawable.ic_group)
                        .error(R.drawable.ic_group)
                        .into(imgGroup);
            } else {
                imgGroup.setImageResource(R.drawable.ic_group);
            }
        }
    }

    public void applyGroupUpdate(int groupId, @NonNull String senderName,
                                 @NonNull String content, boolean isSelf) {
        int idx = -1;
        for (int i = 0; i < chatGroups.size(); i++) {
            if (chatGroups.get(i).getId() == groupId) { idx = i; break; }
        }
        if (idx == -1) return; // chưa có trong list -> có thể bỏ qua hoặc fetch lại list

        ChatGroupResponse g = chatGroups.get(idx);

        // Cập nhật lastUser / lastMessage
        if (g.getLastUser() == null) g.setLastUser(new com.fpt.myapplication.dto.response.UserResponse());
        g.getLastUser().setDisplayName(senderName);

        if (g.getLastMessage() == null) g.setLastMessage(new com.fpt.myapplication.dto.response.MessageResponse());
        g.getLastMessage().setContent(content);
        // Nếu event có timestamp -> g.getLastMessage().setTimestamp(event.getTimestamp());

        // Nếu không phải do chính mình gửi và không đang mở phòng -> đánh dấu hasNew
        g.setHasNew(!isSelf);

        // Đẩy lên đầu
        if (idx != 0) {
            chatGroups.remove(idx);
            chatGroups.add(0, g);
            notifyItemMoved(idx, 0);
            notifyItemChanged(0);
        } else {
            notifyItemChanged(0);
        }
    }
}
