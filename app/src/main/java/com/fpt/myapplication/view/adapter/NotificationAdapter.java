package com.fpt.myapplication.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.response.NotificationResponse;
import com.fpt.myapplication.util.FileUtil;
import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends ListAdapter<NotificationResponse, NotificationAdapter.VH> {

    public interface OnItemClick {
        void onClick(@NonNull NotificationResponse item, int position);
    }

    private final Context ctx;
    private OnItemClick onItemClick;

    public NotificationAdapter(Context ctx) {
        super(DIFF);
        this.ctx = ctx;
    }

    public void setOnItemClick(OnItemClick cb) { this.onItemClick = cb; }

    private static final DiffUtil.ItemCallback<NotificationResponse> DIFF =
            new DiffUtil.ItemCallback<NotificationResponse>() {
                @Override public boolean areItemsTheSame(@NonNull NotificationResponse o, @NonNull NotificationResponse n) {
                    return o.getId() != null && o.getId().equals(n.getId());
                }
                @Override public boolean areContentsTheSame(@NonNull NotificationResponse o, @NonNull NotificationResponse n) {
                    return safeEq(o.getTitle(), n.getTitle())
                            && safeEq(o.getContent(), n.getContent())
                            && safeEq(o.getType(), n.getType())
                            && safeEq(o.getCreatedAt(), n.getCreatedAt())
                            && safeEq(o.getIsRead(), n.getIsRead())
                            && (o.getSender() == null ? n.getSender() == null :
                            (n.getSender() != null && safeEq(o.getSender().getAvatar(), n.getSender().getAvatar())
                                    && safeEq(o.getSender().getDisplayName(), n.getSender().getDisplayName())));
                }
                private boolean safeEq(Object a, Object b) { return a == b || (a != null && a.equals(b)); }
            };

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.notification_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        h.bind(getItem(position), onItemClick, ctx);
    }

    // -------------------- ViewHolder --------------------
    static class VH extends RecyclerView.ViewHolder {
        CircleImageView imgSender;
        View dotUnread;
        TextView tvTitle, tvContent, tvTime;

        VH(@NonNull View itemView) {
            super(itemView);
            imgSender = itemView.findViewById(R.id.imgSender);
            dotUnread = itemView.findViewById(R.id.dotUnread);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        void bind(NotificationResponse it, OnItemClick cb, Context ctx) {
            // Avatar
            String avatar = it.getSender() != null ? it.getSender().getAvatar() : null;
            Glide.with(ctx)
                    .load(FileUtil.GetImageUrl(avatar))
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(imgSender);

            // Tiêu đề + nội dung
            tvTitle.setText(emptyTo(it.getTitle(), "(Không tiêu đề)"));
            tvContent.setText(emptyTo(it.getContent(), ""));

            // “5m / 2h / 3d …”
            tvTime.setText(formatAbsolute(it.getCreatedAt()));

            // Unread dot
            Boolean isRead = it.getIsRead();
            dotUnread.setVisibility((isRead != null && !isRead) ? View.VISIBLE : View.GONE);
            // đảm bảo có nền tròn:
            if (dotUnread.getBackground() == null) {
                dotUnread.setBackground(ctx.getDrawable(R.drawable.bg_bubble_outgoing));
            }

            itemView.setOnClickListener(v -> {
                if (cb != null) cb.onClick(it, getBindingAdapterPosition());
            });
        }

        private String emptyTo(String s, String ifEmpty) {
            return TextUtils.isEmpty(s) ? ifEmpty : s;
        }

        private String formatAbsolute(String createdAt) {
            if (TextUtils.isEmpty(createdAt)) return "";
            // Output: dd/MM/yyyy HH:mm  (ví dụ 08/11/2025 10:07)
            java.time.format.DateTimeFormatter outFmt =
                    java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());

            try {
                // 1) Thử OffsetDateTime (có offset, ví dụ ...+07:00)
                java.time.OffsetDateTime odt = java.time.OffsetDateTime.parse(createdAt);
                return odt.atZoneSameInstant(java.time.ZoneId.systemDefault()).format(outFmt);
            } catch (Exception ignore) { }

            try {
                // 2) Thử Instant (có 'Z')
                java.time.Instant ins = java.time.Instant.parse(createdAt);
                return ins.atZone(java.time.ZoneId.systemDefault()).format(outFmt);
            } catch (Exception ignore) { }

            try {
                // 3) Thử LocalDateTime (không offset, ví dụ 2025-11-08T03:07:28.101824)
                //    - giả sử thời gian là local time của thiết bị
                java.time.format.DateTimeFormatter inFmt =
                        new java.time.format.DateTimeFormatterBuilder()
                                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                                .optionalStart().appendFraction(java.time.temporal.ChronoField.NANO_OF_SECOND, 1, 9, true).optionalEnd()
                                .toFormatter();

                java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(createdAt, inFmt);
                return ldt.atZone(java.time.ZoneId.systemDefault()).format(outFmt);
            } catch (Exception ignore) { }

            // 4) Fallback: trả nguyên văn để dễ debug
            return createdAt;
        }
    }
}
