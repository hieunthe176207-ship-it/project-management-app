package com.fpt.myapplication.controller;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fpt.myapplication.R;
import com.fpt.myapplication.config.WebSocketManager;
import com.fpt.myapplication.dto.request.ChatMessage;
import com.fpt.myapplication.view.adapter.ChatAdapter;
import com.google.gson.Gson;

import java.util.List;

public class ChatActivity extends AppCompatActivity implements  WebSocketManager.MessageListener {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private EditText edtMessage;
    private ImageButton btnSend;


    private List<ChatMessage> messageList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);

        recyclerView = findViewById(R.id.recyclerView);
        edtMessage   = findViewById(R.id.edtMessage);
        btnSend      = findViewById(R.id.btnSend);

        adapter = new ChatAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        WebSocketManager.get().connect("");

        btnSend.setOnClickListener(v -> {
            String content = edtMessage.getText() != null ? edtMessage.getText().toString().trim() : "";
            if (TextUtils.isEmpty(content)) return;
            WebSocketManager.get().sendToChat(content);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            edtMessage.setText("");
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        WebSocketManager ws = WebSocketManager.get();
        ws.addListener(this);
        ws.subscribeTopic("/topic/public");
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
        Log.d("CHAT", "onNewMessage: "+ payload);
        ChatMessage message = new Gson().fromJson(payload, ChatMessage.class);
        runOnUiThread(() -> {
            adapter.addMessage(message);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        });
    }
}
