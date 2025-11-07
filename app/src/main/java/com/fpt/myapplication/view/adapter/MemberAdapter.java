package com.fpt.myapplication.view.adapter;

        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageButton;
        import android.widget.TextView;
        import androidx.annotation.NonNull;
        import androidx.fragment.app.FragmentManager;
        import androidx.lifecycle.LifecycleOwner;
        import androidx.recyclerview.widget.RecyclerView;
        import com.bumptech.glide.Glide;
        import com.fpt.myapplication.R;
        import com.fpt.myapplication.dto.response.UserResponse;
        import com.fpt.myapplication.util.FileUtil;
        import com.fpt.myapplication.view.fragment.RolePickerDialog;
        import java.util.ArrayList;
        import java.util.List;
        import de.hdodenhof.circleimageview.CircleImageView;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.ViewHolder> {

    private List<UserResponse> data = new ArrayList<>();
    private final FragmentManager fragmentManager;
    private final OnRoleListener onRoleUpdateListener;

    // giữ vị trí item đang sửa role
    private int pendingPosition = RecyclerView.NO_POSITION;
    public int consumePendingPosition() {
        int p = pendingPosition;
        pendingPosition = RecyclerView.NO_POSITION;
        return p;
    }

    public interface OnRoleListener {
        void onRoleUpdate(int position, int newRole); // 0 = Thành viên, 1 = Quản lý
        void onRemoveMember(int position, UserResponse user);
    }

    public MemberAdapter(FragmentManager fragmentManager, LifecycleOwner lifecycleOwner, OnRoleListener listener) {
        this.fragmentManager = fragmentManager;
        this.onRoleUpdateListener = listener;
    }

    public OnRoleListener getOnRoleUpdateListener() {
        return onRoleUpdateListener;
    }
    @NonNull @Override
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

        String avatarUrl = user.getAvatar();
        holder.imgAvatar.setImageResource(R.drawable.default_avatar);
        Glide.with(holder.imgAvatar.getContext())
                .load(FileUtil.GetImageUrl(avatarUrl))
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(holder.imgAvatar);

        holder.btnCheck.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            // Map role hiện tại -> index spinner
            String roleStr = data.get(pos).getRole();
            int currentRoleIndex = "Quản lý".equalsIgnoreCase(roleStr) ? 1 : 0; // 0=Thành viên,1=Quản lý

            // lưu vị trí đang đổi để Activity xử lý result
            pendingPosition = pos;

            RolePickerDialog dialog = RolePickerDialog.newInstance(currentRoleIndex, null);
            dialog.show(fragmentManager, RolePickerDialog.TAG);
        });

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            if (onRoleUpdateListener != null) {
                onRoleUpdateListener.onRemoveMember(pos, data.get(pos));
            }
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgAvatar;
        TextView tvName, tvEmail, tvRole;
        ImageButton btnCheck, btnDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnCheck = itemView.findViewById(R.id.btnUpdate);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public void submitList(List<UserResponse> users) {
        data.clear();
        if (users != null) data.addAll(users);
        notifyDataSetChanged();
    }

    public List<UserResponse> getCurrentList() { return data; }
}
