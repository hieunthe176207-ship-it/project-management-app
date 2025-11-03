package com.fpt.myapplication.controller;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.ChatGroupResponse;
import com.fpt.myapplication.model.ChatGroupModel;
import com.fpt.myapplication.view.adapter.ChatGroupAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatGroupActivity extends AppCompatActivity {

    private static final String TAG = "ChatGroupActivity";
    private ChatGroupModel chatGroupModel;
    private RecyclerView rvGroups;
    private ChatGroupAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_group_layout);
        chatGroupModel = new ChatGroupModel(this);
        rvGroups = findViewById(R.id.rvGroups);

        adapter = new ChatGroupAdapter();
        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setAdapter(adapter);

        chatGroupModel.getAllChatGroups(new ChatGroupModel.GetAllGroupChatCallBack() {
            @Override
            public void onSuccess(List<ChatGroupResponse> data) {
                adapter.submitList(data);
            }

            @Override
            public void onError(ResponseError error) {

            }

            @Override
            public void onLoading() {

            }
        });
    }
}
