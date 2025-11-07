package com.fpt.myapplication.controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.config.WebSocketManager;
import com.fpt.myapplication.dto.ResponseError;
import com.fpt.myapplication.dto.request.ChatMessage;
import com.fpt.myapplication.dto.request.MessageRequest;
import com.fpt.myapplication.dto.response.ChatGroupDetailResponse;
import com.fpt.myapplication.dto.response.MessageResponse;
import com.fpt.myapplication.dto.response.UserResponse;
import com.fpt.myapplication.model.MessageModel;
import com.fpt.myapplication.util.SessionPrefs;
import com.fpt.myapplication.view.adapter.ChatAdapter;
import com.fpt.myapplication.view.adapter.MessageAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements  WebSocketManager.MessageListener {


    private CircleImageView imgAvatar;

    private TextView tvTitle;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;

    private MessageModel  messageModel;

    private TextInputEditText edtMessage;

    private MaterialButton btnSend;

    private int groupId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        messageModel = new MessageModel(this);
        adapter = new MessageAdapter();

        UserResponse user = SessionPrefs.get(this).getUser();

        Intent intent = getIntent();
        groupId = intent.getIntExtra("id", -1);
        if (groupId == -1) {
            String idStr = intent.getStringExtra("id");
            if (idStr != null) {
                try { groupId = Integer.parseInt(idStr); } catch (NumberFormatException ignore) {}
            }
        }

        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);

        recyclerView = findViewById(R.id.recyclerView);
        tvTitle = findViewById(R.id.tvTitle);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);


        messageModel.getAllMessage(groupId, new MessageModel.GetAllMessageCallBack() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onError(ResponseError error) {

            }

            @Override
            public void onSuccess(ChatGroupDetailResponse data) {
                tvTitle.setText(data.getName());
                adapter.setMessages(data.getMessages());
            }
        });

        btnSend.setOnClickListener(v -> {
            String content = edtMessage.getText().toString().trim();
            if (TextUtils.isEmpty(content)) {
                return;
            }
            MessageRequest request = new MessageRequest();
            request.setChatGroupId(groupId);
            request.setContent(content);
            request.setEmail(user.getEmail());
            String bodyJson = new Gson().toJson(request);
            WebSocketManager.get().send("/app/chat", bodyJson);
            edtMessage.setText("");
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        WebSocketManager ws = WebSocketManager.get();
        ws.addListener(this);
        ws.subscribeTopic("/topic/group/"+groupId);
    }

    @Override
    protected void onStop() {
        WebSocketManager.get().removeListener(this);
        super.onStop();
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
       if(topic.equals("/topic/group/"+groupId)){
           MessageResponse message = new Gson().fromJson(payload, MessageResponse.class);
           runOnUiThread(() -> {
               adapter.addMessage(message);
               recyclerView.scrollToPosition(adapter.getItemCount() - 1);
           });
       }
    }
}
