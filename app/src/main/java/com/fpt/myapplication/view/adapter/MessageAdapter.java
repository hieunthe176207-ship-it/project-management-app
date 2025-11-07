package com.fpt.myapplication.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.response.MessageResponse;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.util.FileUtil;
import com.fpt.myapplication.util.SessionPrefs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final int TYPE_INCOMING = 0;
    private static final int TYPE_OUTGOING = 1;

    private Context  context;

    private final List<MessageResponse> data = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_INCOMING) {
            View v = inf.inflate(R.layout.item_message_incoming, parent, false);
            return new IncomingVH(v);
        } else {
            View v = inf.inflate(R.layout.item_message_outcoming, parent, false);
            return new OutgoingVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageResponse m = data.get(position);
        LocalDateTime dt = LocalDateTime.parse(m.timestamp);
        String outHourSecond = dt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:ss"));
        if (holder instanceof IncomingVH) {
            IncomingVH ivh = (IncomingVH) holder;
            ivh.tvSenderName.setText(m.senderName);
            ivh.tvMessage.setText(m.content);
            ivh.tvTime.setText(outHourSecond);

            if (m.avatarUrl != null && !m.avatarUrl.isEmpty()) {
                Glide.with(ivh.itemView.getContext())
                        .load(FileUtil.GetImageUrl(m.avatarUrl))
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(ivh.imgAvatar);
            } else {
                ivh.imgAvatar.setImageResource(R.drawable.default_avatar);
            }
        } else if (holder instanceof OutgoingVH) {
            OutgoingVH ovh = (OutgoingVH) holder;
            ovh.tvMessage.setText(m.content);
            ovh.tvTime.setText(outHourSecond);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        UserResponse currentUser = SessionPrefs.get(context).getUser();
        MessageResponse message = data.get(position);
        if (message.getSenderId() == currentUser.getId()) {
            return TYPE_OUTGOING;
        } else {
            return TYPE_INCOMING;
        }
    }

    static class IncomingVH extends RecyclerView.ViewHolder {
        CircleImageView imgAvatar;
        TextView tvSenderName, tvMessage, tvTime;
        IncomingVH(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    static class OutgoingVH extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        OutgoingVH(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }

    public void setMessages(List<MessageResponse> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    public void addMessage(MessageResponse msg) {
        data.add(msg);
        notifyItemInserted(data.size() - 1);
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }

}
