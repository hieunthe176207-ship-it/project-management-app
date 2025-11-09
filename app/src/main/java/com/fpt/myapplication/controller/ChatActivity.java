package com.fpt.myapplication.controller;

            import android.content.Intent;
            import android.os.Bundle;
            import android.text.TextUtils;
            import android.util.Log;
            import android.view.Menu;
            import android.view.MenuItem;
            import android.view.View;
            import android.widget.EditText;
            import android.widget.ImageButton;
            import android.widget.TextView;

            import androidx.annotation.NonNull;
            import androidx.annotation.Nullable;
            import androidx.appcompat.app.AppCompatActivity;
            import androidx.recyclerview.widget.LinearLayoutManager;
            import androidx.recyclerview.widget.RecyclerView;

            import com.bumptech.glide.Glide;
            import com.fpt.myapplication.R;
            import com.fpt.myapplication.config.WebSocketManager;
            import com.fpt.myapplication.dto.ResponseError;
            import com.fpt.myapplication.dto.request.ChatMessage;
            import com.fpt.myapplication.dto.request.MessageRequest;
            import com.fpt.myapplication.dto.response.ChatGroupDetailResponse;
            import com.fpt.myapplication.dto.response.MessageResponse;
            import com.fpt.myapplication.dto.response.UserResponse;
            import com.fpt.myapplication.model.ChatGroupModel;
            import com.fpt.myapplication.model.MessageModel;
            import com.fpt.myapplication.util.FileUtil;
            import com.fpt.myapplication.util.SessionPrefs;
            import com.fpt.myapplication.view.adapter.ChatAdapter;
            import com.fpt.myapplication.view.adapter.MessageAdapter;
            import com.google.android.material.appbar.MaterialToolbar;
            import com.google.android.material.button.MaterialButton;
            import com.google.android.material.progressindicator.CircularProgressIndicator;
            import com.google.android.material.textfield.TextInputEditText;
            import com.google.gson.Gson;

            import java.io.File;
            import java.util.List;

            import cn.pedant.SweetAlert.SweetAlertDialog;
            import de.hdodenhof.circleimageview.CircleImageView;

            public class ChatActivity extends AppCompatActivity implements WebSocketManager.MessageListener {

                private CircleImageView imgAvatar;
                private TextView tvTitle;
                private RecyclerView recyclerView;
                private MessageAdapter adapter;
                private MessageModel messageModel;

                private ChatGroupModel chatGroupModel;
                private TextInputEditText edtMessage;
                private MaterialButton btnSend;
                private int groupId;
                private View loadingOverlay;
                private CircularProgressIndicator progressBar;

                // Lưu thông tin nhóm để truyền sang UpdateActivity
                private String groupName;
                private String groupImageUrl;

                private UserResponse user;
                @Override
                protected void onCreate(@Nullable Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);
                    setContentView(R.layout.chat_layout);
                    MaterialToolbar toolbar = findViewById(R.id.toolbar);
                    setSupportActionBar(toolbar);
                    toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
                    messageModel = new MessageModel(this);
                    chatGroupModel = new ChatGroupModel(this);
                    adapter = new MessageAdapter();

                    user = SessionPrefs.get(this).getUser();

                    Intent intent = getIntent();
                    groupId = intent.getIntExtra("id", -1);
                    if (groupId == -1) {
                        String idStr = intent.getStringExtra("id");
                        if (idStr != null) {
                            try { groupId = Integer.parseInt(idStr); } catch (NumberFormatException ignore) {}
                        }
                    }

                    loadingOverlay = findViewById(R.id.loadingOverlay);
                    progressBar = findViewById(R.id.progressBar);

                    edtMessage = findViewById(R.id.edtMessage);
                    btnSend = findViewById(R.id.btnSend);

                    recyclerView = findViewById(R.id.recyclerView);
                    tvTitle = findViewById(R.id.tvTitle);
                    imgAvatar = findViewById(R.id.imgAvatar);
                    LinearLayoutManager lm = new LinearLayoutManager(this);
                    recyclerView.setLayoutManager(lm);
                    recyclerView.setAdapter(adapter);

                    setSupportActionBar(toolbar);
                    toolbar.setTitleTextColor(getColor(R.color.black));
                    toolbar.setNavigationIconTint(getColor(R.color.black));
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
                public boolean onCreateOptionsMenu(Menu menu) {
                    getMenuInflater().inflate(R.menu.chat_menu, menu);
                    return true;
                }

                @Override
                public boolean onOptionsItemSelected(@NonNull MenuItem item) {
                    if (item.getItemId() == R.id.action_update_group) {
                        Intent intent = new Intent(this, UpdateChatGroupActivity.class);
                        intent.putExtra("groupId", groupId);
                        intent.putExtra("groupName", groupName);
                        intent.putExtra("imageUrl", groupImageUrl);
                        startActivity(intent);
                        return true;
                    }
                    return super.onOptionsItemSelected(item);
                }

                private void fetchApi(){
                    messageModel.getAllMessage(groupId, new MessageModel.GetAllMessageCallBack() {
                        @Override
                        public void onLoading() {
                            showLoading();
                        }

                        @Override
                        public void onError(ResponseError error) {

                            SweetAlertDialog dlg = new SweetAlertDialog(ChatActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Error")
                                    .setContentText(error != null && error.message != null ? error.message : "Unexpected error")
                                    .setConfirmText("Back to Group Chat");             // chặn nút Back
                            dlg.setCanceledOnTouchOutside(false);      // chặn chạm ngoài

                            dlg.setConfirmClickListener(sDialog -> {
                                sDialog.dismissWithAnimation();        // <-- đúng ở đây
                                Intent intent = new Intent(ChatActivity.this, ChatGroupActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            });
                            dlg.show();
                        }

                        @Override
                        public void onSuccess(ChatGroupDetailResponse data) {
                            hideLoading();
                            groupName = data.getName();
                            groupImageUrl = data.getAvatar();
                            tvTitle.setText(groupName);
                            adapter.setMessages(data.getMessages());

                            Glide.with(imgAvatar.getContext())
                                    .load(FileUtil.GetImageUrl(groupImageUrl))
                                    .placeholder(R.drawable.ic_group)
                                    .error(R.drawable.ic_group)
                                    .into(imgAvatar);
                            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                        }
                    });
                }

                @Override
                protected void onStart() {
                    super.onStart();
                    fetchApi();
                    WebSocketManager ws = WebSocketManager.get();
                    ws.addListener(this);
                    ws.subscribeTopic("/topic/group/"+groupId);
                    ws.subscribeTopic("/topic/error/"+user.getId());

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
                   if(topic.equals("/topic/error/"+user.getId())){
                       runOnUiThread(() -> {
                           SweetAlertDialog dlg = new SweetAlertDialog(ChatActivity.this, SweetAlertDialog.ERROR_TYPE)
                                   .setTitleText("Error")
                                   .setContentText(payload)
                                   .setConfirmText("OK");
                           dlg.show();
                       });
                   }
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