package com.fpt.myapplication.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.ProjectResponse;
import com.fpt.myapplication.model.ProjectModel;
import com.fpt.myapplication.util.FileUtil;
import com.google.android.material.chip.Chip;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProjectDetailActivity extends AppCompatActivity {

    private ProjectModel model;
    private TextView tvTitle, tvDescription, tvCreatorName, tvCreatorEmail, tvCount;
    private Chip chipDeadline;
    private ImageView imgCreatorAvatar;
    private FrameLayout overlay;

    private CircleImageView a1, a2, a3, a4;
    private View overlayCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.project_detail_layout);
        int projectId = getIntent().getIntExtra("project_id", -1);

        tvDescription = findViewById(R.id.tvDescription);
        tvTitle       = findViewById(R.id.tvTitle);
        chipDeadline  = findViewById(R.id.chipDeadline);
        overlay = findViewById(R.id.loadingOverlay);
        imgCreatorAvatar = findViewById(R.id.imgCreatorAvatar);
        tvCreatorName = findViewById(R.id.tvCreatorName);
        tvCreatorEmail = findViewById(R.id.tvCreatorEmail);
        a1 = findViewById(R.id.memberAvatar1);
        a2 = findViewById(R.id.memberAvatar2);
        a3 = findViewById(R.id.memberAvatar3);
        a4 = findViewById(R.id.memberAvatar4);
        tvCount  = findViewById(R.id.membersCount);
        overlayCount = findViewById(R.id.overlayCount);



        model = new ProjectModel(this);
        model.getProjectById(projectId, new ProjectModel.GetProjectDetailCallBack() {
            @Override
            public void onSuccess(ProjectResponse data) {
                tvDescription.setText(data.getDescription());
                tvTitle.setText(data.getName());
                chipDeadline.setText("Deadline: " + data.getDeadline().toString());

                Glide.with(ProjectDetailActivity.this)
                        .load(FileUtil.GetImageUrl(data.getCreatedBy().getAvatar()))
                        .error(R.drawable.default_avatar)
                        .into(imgCreatorAvatar);
                tvCreatorName.setText(data.getCreatedBy().getDisplayName());
                tvCreatorEmail.setText(data.getCreatedBy().getEmail());

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
                    loadAvatar(data.getMembers().get(2).getAvatar(), a4);

                    int remain = size - 4;
                    if (remain > 0) {
                        tvCount.setText("+" + remain);
                        overlayCount.setVisibility(View.VISIBLE);
                        tvCount.setVisibility(View.VISIBLE);
                    } else {
                        tvCount.setVisibility(View.INVISIBLE); // chỉ dùng avatar
                    }
                }
                overlay.setVisibility(View.GONE);
            }

            @Override
            public void onError(ResponseError error) {
                Log.e("PROJECTDETAIL", "onError: "+error );
                overlay.setVisibility(View.GONE);
            }

            @Override
            public void onLoading() {
                overlay.setVisibility(View.VISIBLE);
            }
        });

    }

    private void loadAvatar(String url, ImageView target) {
        Glide.with(target.getContext())
                .load(FileUtil.GetImageUrl(url))
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .into(target);
    }

}
