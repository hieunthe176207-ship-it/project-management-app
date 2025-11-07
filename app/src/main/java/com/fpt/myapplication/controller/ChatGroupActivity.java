package com.fpt.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.config.WebSocketManager;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.response.ChatGroupResponse;
import com.fpt.myapplication.dto.response.GroupUpdateEvent;
import com.fpt.myapplication.dto.response.MessageResponse;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.model.ChatGroupModel;
import com.fpt.myapplication.util.SessionPrefs;
import com.fpt.myapplication.view.adapter.ChatGroupAdapter;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ChatGroupActivity extends AppCompatActivity implements WebSocketManager.MessageListener {

    private static final String TAG = "ChatGroupActivity";
    private ChatGroupModel chatGroupModel;
    private RecyclerView rvGroups;
    private ChatGroupAdapter adapter;

    private View loadingOverlay;
    private CircularProgressIndicator progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_group_layout);

        chatGroupModel = new ChatGroupModel(this);
        rvGroups = findViewById(R.id.rvGroups);
        loadingOverlay = findViewById(R.id.loadingOverlay);
        progressBar = findViewById(R.id.progressBar);
        adapter = new ChatGroupAdapter();
        rvGroups.setLayoutManager(new LinearLayoutManager(this));
        rvGroups.setAdapter(adapter);

        adapter.setOnGroupClickListener(group -> {
            int groupId = group.getId(); // hoặc group.id nếu là public field
            Intent i = new Intent(ChatGroupActivity.this, ChatActivity.class);
            i.putExtra("id", groupId);
            startActivity(i);

            group.setHasNew(false);
            int pos = findIndexById(groupId);
            if (pos >= 0) adapter.notifyItemChanged(pos);

            chatGroupModel.markGroupAsRead(groupId, new ChatGroupModel.MarkGroupAsReadCallBack() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(ResponseError error) {

                }

                @Override
                public void onLoading() {

                }
            });
        });


    }

    private void fetchApi(){
        chatGroupModel.getAllChatGroups(new ChatGroupModel.GetAllGroupChatCallBack() {
            @Override
            public void onSuccess(List<ChatGroupResponse> data) {
                adapter.submitList(data);
                hideLoading();
            }

            @Override
            public void onError(ResponseError error) {
                // TODO: show Toast/log
                hideLoading();
            }

            @Override
            public void onLoading() {
                showLoading();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchApi();
        UserResponse user = SessionPrefs.get(this).getUser();
        WebSocketManager ws = WebSocketManager.get();
        ws.addListener(this);
        ws.subscribeTopic("/topic/group-update/" + user.getId());
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onError(String error) {

    }

    @Override
    public void onNewMessage(String topic, String payload) {
           if(topic.startsWith("/topic/group-update/")) {
               Log.d(TAG, "onNewUpdate: " + payload);
               GroupUpdateEvent evt = new Gson().fromJson(payload, GroupUpdateEvent.class);
               int currentUserId = SessionPrefs.get(this).getUser().getId();
               boolean isSelf = (evt.getSenderId() != null && evt.getSenderId() == currentUserId);
               // Tùy payload: nếu có evt.getPreview() thì tách senderName ở server,
               // còn không thì dùng evt.getSenderName() + evt.getContent()
               String senderName = evt.getSenderName() != null ? evt.getSenderName() : "Người gửi";
               String content = evt.getContent();

               runOnUiThread(() -> {
                   adapter.applyGroupUpdate(evt.getGroupId(), senderName, content, isSelf);
               });
           }

    }
    private int findIndexById(int groupId) {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            ChatGroupResponse g = adapter.getItemAt(i); // thêm getter trong adapter
            if (g != null && g.getId() == groupId) return i;
        }
        return -1;
    }

    private void showLoading() {
        runOnUiThread(() -> {
            loadingOverlay.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    private void hideLoading() {
        runOnUiThread(() -> {
            loadingOverlay.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        });
    }
}
