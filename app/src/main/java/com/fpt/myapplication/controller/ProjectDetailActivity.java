package com.fpt.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.ProjectResponse;
import com.fpt.myapplication.model.ProjectModel;
import com.fpt.myapplication.util.FileUtil;
import com.google.android.material.chip.Chip;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProjectDetailActivity extends AppCompatActivity {

    private ProjectModel model;
    private TextView tvTitle, tvDescription, tvCreatorName, tvCreatorEmail, tvCount, badgePendingCount;
    private Chip chipDeadline;
    private ImageView imgCreatorAvatar;
    private FrameLayout overlay;
    private View cardJoinRequests;
    private ProgressBar progress;
    private CircleImageView a1, a2, a3, a4;
    private View overlayCount;
    private int projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.project_detail_layout);

        projectId = getIntent().getIntExtra("project_id", -1);

        initViews();
        setupClickListeners();

        model = new ProjectModel(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadProjectDetail();
    }

    private void initViews() {
        progress = findViewById(R.id.progress);
        tvDescription = findViewById(R.id.tvDescription);
        tvTitle = findViewById(R.id.tvTitle);
        chipDeadline = findViewById(R.id.chipDeadline);
        overlay = findViewById(R.id.loadingOverlay);
        imgCreatorAvatar = findViewById(R.id.imgCreatorAvatar);
        tvCreatorName = findViewById(R.id.tvCreatorName);
        tvCreatorEmail = findViewById(R.id.tvCreatorEmail);
        a1 = findViewById(R.id.memberAvatar1);
        a2 = findViewById(R.id.memberAvatar2);
        a3 = findViewById(R.id.memberAvatar3);
        a4 = findViewById(R.id.memberAvatar4);
        tvCount = findViewById(R.id.membersCount);
        overlayCount = findViewById(R.id.overlayCount);
        cardJoinRequests = findViewById(R.id.cardJoinRequests);
        badgePendingCount = findViewById(R.id.badgePendingCount);
    }

    private void setupClickListeners() {
        View cardMember = findViewById(R.id.cardThanhVien);
        cardMember.setOnClickListener(v -> {
            if (projectId == -1) return;
            Intent i = new Intent(ProjectDetailActivity.this, MemberActivity.class);
            i.putExtra("projectId", projectId);
            startActivity(i);
        });

        View cardKaban = findViewById(R.id.cardBaoCao);
        cardKaban.setOnClickListener(v -> {
            if (projectId == -1) return;
            Intent i = new Intent(ProjectDetailActivity.this, KabanBoardActitvity.class);
            i.putExtra("project_id", projectId);
            startActivity(i);
        });

        View cardTask = findViewById(R.id.cardCongViec);
        cardTask.setOnClickListener(v -> {
            if (projectId == -1) return;
            Intent i = new Intent(ProjectDetailActivity.this, TaskActivity.class);
            i.putExtra("project_id", projectId);
            startActivity(i);
        });

        cardJoinRequests.setOnClickListener(v -> {
            if (projectId == -1) return;
            Intent i = new Intent(ProjectDetailActivity.this, JoinRequestActivity.class);
            i.putExtra("project_id", projectId);
            startActivity(i);
        });
    }

    private void loadProjectDetail() {
        model.getProjectById(projectId, new ProjectModel.GetProjectDetailCallBack() {
            @Override
            public void onSuccess(ProjectResponse data) {
                updateUI(data);
                overlay.setVisibility(View.GONE);
                progress.setVisibility(View.GONE);
            }

            @Override
            public void onError(ResponseError error) {
                Log.e("PROJECTDETAIL", "onError: " + error);
                progress.setVisibility(View.GONE);
                overlay.setVisibility(View.GONE);
            }

            @Override
            public void onLoading() {
                progress.setVisibility(View.VISIBLE);
                overlay.setVisibility(View.VISIBLE);
            }
        });
    }

    private void updateUI(ProjectResponse data) {
        if (data.getIsPublic() == 1) {
            cardJoinRequests.setVisibility(View.VISIBLE);
            int pendingCount = data.getCountJoinRequest();
            if (pendingCount > 0) {
                badgePendingCount.setVisibility(View.VISIBLE);
                badgePendingCount.setText(String.valueOf(pendingCount));
            } else {
                badgePendingCount.setVisibility(View.GONE);
            }
        }

        tvDescription.setText(data.getDescription());
        tvTitle.setText(data.getName());
        chipDeadline.setText("Deadline: " + data.getDeadline().toString());

        Glide.with(ProjectDetailActivity.this)
                .load(FileUtil.GetImageUrl(data.getCreatedBy().getAvatar()))
                .error(R.drawable.default_avatar)
                .into(imgCreatorAvatar);
        tvCreatorName.setText(data.getCreatedBy().getDisplayName());
        tvCreatorEmail.setText(data.getCreatedBy().getEmail());

        updateMemberAvatars(data);
    }

    private void updateMemberAvatars(ProjectResponse data) {
        int size = data.getMembers().size();
        a1.setVisibility(View.GONE);
        a2.setVisibility(View.GONE);
        a3.setVisibility(View.GONE);
        a4.setVisibility(View.GONE);
        tvCount.setVisibility(View.GONE);
        overlayCount.setVisibility(View.GONE);

        if (size >= 1) {
            a1.setVisibility(View.VISIBLE);
            loadAvatar(data.getMembers().get(0).getAvatar(), a1);
        }
        if (size >= 2) {
            a2.setVisibility(View.VISIBLE);
            loadAvatar(data.getMembers().get(1).getAvatar(), a2);
        }
        if (size >= 3) {
            a3.setVisibility(View.VISIBLE);
            loadAvatar(data.getMembers().get(2).getAvatar(), a3);
        }
        if (size >= 4) {
            a4.setVisibility(View.VISIBLE);
            loadAvatar(data.getMembers().get(3).getAvatar(), a4);

            int remain = size - 4;
            if (remain > 0) {
                tvCount.setText("+" + remain);
                overlayCount.setVisibility(View.VISIBLE);
                tvCount.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadAvatar(String url, ImageView target) {
        Glide.with(target.getContext())
                .load(FileUtil.GetImageUrl(url))
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(target);
    }
}
