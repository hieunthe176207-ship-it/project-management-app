package com.fpt.myapplication.config;

import android.util.Log;

import com.fpt.myapplication.dto.request.ChatMessage;
import com.google.gson.Gson;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;
import ua.naiksoftware.stomp.dto.StompMessage;


/**
 * WebSocketManager
 * ----------------
 * - Giữ 1 kết nối STOMP duy nhất (Singleton) cho toàn app.
 * - Cho phép nhiều Activity/Fragment đăng ký làm "listener" để nhận:
 *   + Trạng thái kết nối (connected/disconnected/error)
 *   + Tin nhắn mới từ các topic đã subscribe
 * - Quản lý subscribe theo topic (subscribe/unsubscribe), tránh trùng lặp.
 * - Cung cấp API gửi tin tới server (ví dụ /app/chat).
 *
 * Sử dụng cơ bản:
 *  1) MainActivity: WebSocketManager.get().connect(jwtToken);
 *  2) ChatActivity:
 *     - onStart(): WebSocketManager.get().addListener(this); (implements MessageListener)
 *                  WebSocketManager.get().subscribeTopic("/topic/public");
 *     - onStop():  WebSocketManager.get().removeListener(this);
 *     - gửi tin:   WebSocketManager.get().sendToChat("hello");
 *  3) Khi sign-out hoặc muốn ngắt: WebSocketManager.get().disconnect();
 */
public class WebSocketManager {

    // ====== CẤU HÌNH & TRẠNG THÁI CHUNG ======================================

    private static final String TAG = "WebSocketManager";

    /**
     * URL WebSocket thuần (KHÔNG SockJS).
     * - Emulator (Android Studio) → PC: 10.0.2.2
     * - Thiết bị thật → dùng IP LAN của PC (vd: 192.168.1.100)
     * Ví dụ backend Spring Boot expose endpoint /ws (thuần WS, không withSockJS) cho mobile.
     */

    private static final String WS_URL = "wss://booking.realmreader.site/ws";

    /** Singleton instance */
    private static WebSocketManager INSTANCE;

    /** Lấy instance Singleton (thread-safe) */
    public static synchronized WebSocketManager get() {
        if (INSTANCE == null) INSTANCE = new WebSocketManager();
        return INSTANCE;
    }

    /** STOMP client chạy trên OkHttp */
    private StompClient stomp;

    /** Trạng thái kết nối hiện tại (đọc/ghi đa luồng) */
    private volatile boolean connected = false;

    /** Token được dùng lần connect gần nhất (để auto-reconnect nếu cần) */
    private volatile String lastJwt = null;

    // ====== QUẢN LÝ LISTENER & SUBSCRIPTION ==================================

    /**
     * Listener nhận sự kiện & tin nhắn đẩy về UI.
     * Khuyến nghị: add/remove ở onStart()/onStop() của Activity/Fragment.
     */
    public interface MessageListener {
        void onConnected();
        void onDisconnected();
        void onError(String error);
        void onNewMessage(String topic, String payload);
    }

    /** Tập listeners an toàn luồng (không crash khi thêm/xóa trong lúc phát sự kiện) */
    private final Set<MessageListener> listeners = new CopyOnWriteArraySet<>();

    /**
     * Quản lý các subscription theo topic → Disposable.
     * - Tránh subscribe cùng 1 topic nhiều lần (gây trùng tin).
     * - Cho phép unsubscribe từng topic khi không cần nữa.
     */
    private final Map<String, Disposable> topicSubscriptions = new ConcurrentHashMap<>();

    /**
     * Lưu "tin cuối" theo topic (tuỳ chọn) để khi màn hình mới mở có thể "replay".
     * - Không bắt buộc. Bạn có thể bỏ nếu không cần.
     */
    private final Map<String, String> lastMessageByTopic = new ConcurrentHashMap<>();

    // ====== CONSTRUCTOR PRIVATE ==============================================

    private WebSocketManager() {
        // private để ép dùng Singleton qua get()
    }

    // ====== API CHO UI: LISTENER ==============================================

    /** Đăng ký nhận sự kiện/tin nhắn (gọi ở onStart()) */
    public void addListener(MessageListener l) {
        if (l != null) listeners.add(l);
    }

    /** Gỡ đăng ký (gọi ở onStop()) */
    public void removeListener(MessageListener l) {
        if (l != null) listeners.remove(l);
    }

    /** Kiểm tra đang kết nối chưa (UI có thể dùng để hiển thị trạng thái) */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Phát lại "tin cuối" của 1 topic cho listener mới (tuỳ chọn).
     * Dùng khi bạn muốn UI không trống trơn lúc vừa vào màn hình.
     */
    public void replayLastFor(String topic, MessageListener l) {
        if (l == null || topic == null) return;
        String last = lastMessageByTopic.get(topic);
        if (last != null) l.onNewMessage(topic, last);
    }

    // ====== KẾT NỐI / NGẮT KẾT NỐI ===========================================

    /**
     * Mở kết nối STOMP.
     * - Gắn header Authorization: Bearer <jwtToken> trong STOMP CONNECT frame.
     * - Đăng ký lắng nghe lifecycle để phát sự kiện cho UI.
     * - KHÔNG tự subscribe topic ở đây: để UI tự gọi subscribeTopic() topic cần thiết.
     */
    public synchronized void connect(String jwtToken) {
        // Nếu đã connected, bỏ qua (tránh tạo kết nối mới)
        if (stomp != null && connected) return;

        this.lastJwt = jwtToken; // lưu lại để tái kết nối nếu muốn

        // Tạo STOMP client dùng OkHttp
        stomp = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL);

        // Header Authorization cho frame CONNECT
        StompHeader authHeader = new StompHeader("Authorization", "Bearer " + jwtToken);

        // Lắng nghe vòng đời kết nối (OPENED / ERROR / CLOSED)
        stomp.lifecycle().subscribe(event -> {
            switch (event.getType()) {
                case OPENED:
                    connected = true;
                    for (MessageListener l : listeners) l.onConnected();
                    break;

                case ERROR:
                    String err = (event.getException() != null)
                            ? event.getException().getMessage()
                            : "Unknown STOMP error";
                    logE("Lifecycle ERROR: " + err, event.getException());
                    for (MessageListener l : listeners) l.onError(err);
                    break;

                case CLOSED:
                    connected = false;
                    for (MessageListener l : listeners) l.onDisconnected();
                    break;
            }
        }, throwable -> {
            // Lỗi đăng ký lifecycle (hiếm)
            String err = throwable.getMessage();
            logE("Lifecycle subscribe error: " + err, throwable);
            for (MessageListener l : listeners) l.onError("Lifecycle subscribe error: " + err);
        });

        // Thực hiện CONNECT
        stomp.connect(Collections.singletonList(authHeader));

        stomp.topic("/topic/public").subscribe();
    }

    /**
     * Ngắt kết nối STOMP.
     * - Hủy toàn bộ subscription topic.
     * - Đóng STOMP client.
     * - Xoá trạng thái cục bộ liên quan đến kết nối.
     */
    public synchronized void disconnect() {
        // Hủy các topic đang subscribe
        for (Disposable d : topicSubscriptions.values()) {
            if (d != null && !d.isDisposed()) d.dispose();
        }
        topicSubscriptions.clear();

        // Đóng kết nối
        if (stomp != null) {
            stomp.disconnect();
            stomp = null;
        }
        connected = false;

        // Giữ lastJwt nếu bạn muốn reconnect sau này (tuỳ chiến lược)
        // lastMessageByTopic vẫn giữ để màn hình mới vào còn "replay" nếu thích
    }

    // ====== SUBSCRIBE / UNSUBSCRIBE THEO TOPIC =================================

    /**
     * Subscribe 1 topic STOMP (ví dụ "/topic/public" hoặc "/user/queue/chat").
     * - An toàn: không tạo trùng nếu đã subscribe trước đó.
     * - Broadcast mọi tin nhắn tới toàn bộ listeners đang đăng ký.
     */
    public synchronized void subscribeTopic(String topic) {
        if (topic == null || topic.isEmpty()) return;
        if (stomp == null) {
            logW("subscribeTopic: STOMP is null. Call connect() first.");
            return;
        }
        if (topicSubscriptions.containsKey(topic)) {
            // Đã subscribe rồi → bỏ qua
            return;
        }

        Disposable sub = stomp.topic(topic).subscribe(
                // onNext: nhận tin nhắn từ broker
                (StompMessage msg) -> {
                    String payload = msg.getPayload();
                    // Lưu lại "tin cuối" của topic (tuỳ chọn)
                    lastMessageByTopic.put(topic, payload);
                    // Phát cho toàn bộ listeners đang có
                    for (MessageListener l : listeners) l.onNewMessage(topic, payload);
                },
                // onError: lỗi subscription
                (Throwable t) -> {
                    String err = t.getMessage();
                    logE("Subscribe error for " + topic + ": " + err, t);
                    for (MessageListener l : listeners) l.onError("Subscribe error (" + topic + "): " + err);
                    // Nếu muốn: auto-remove để có thể subscribe lại sau
                    topicSubscriptions.remove(topic);
                }
        );

        topicSubscriptions.put(topic, sub);
    }

    /**
     * Hủy subscribe 1 topic để tiết kiệm tài nguyên khi không còn cần.
     */
    public synchronized void unsubscribeTopic(String topic) {
        if (topic == null) return;
        Disposable d = topicSubscriptions.remove(topic);
        if (d != null && !d.isDisposed()) d.dispose();
        // Có thể giữ lastMessageByTopic nếu muốn replay lần sau; hoặc xoá:
        // lastMessageByTopic.remove(topic);
    }

    // ====== GỬI TIN LÊN SERVER =================================================

    /**
     * Gửi tin nhắn "chat chung" tới server.
     * - Bên Spring Boot: @MessageMapping("/chat") → đích STOMP là "/app/chat".
     * - Server sau đó broadcast ra các topic (ví dụ "/topic/public").
     */

    /**
     * Ví dụ: gửi tin tới 1 destination bất kỳ (linh hoạt hơn).
     * - Dùng khi bạn có nhiều @MessageMapping khác nhau.
     */
    public void send(String destinationApp, String jsonBody) {
        if (!ensureConnected()) return;
        stomp.send(destinationApp, jsonBody)
                .subscribe(
                        () -> logD("send OK: " + destinationApp),
                        err -> {
                            // LỖI TRANSPORT (chưa kết nối, socket down, handshake fail…)
                            logE("send error: " + err.getMessage(), err);
                            for (MessageListener l : listeners) {
                                l.onError("Không gửi được tin: " + err.getMessage());
                            }
                        }
                );
    }

    // ====== TIỆN ÍCH NỘI BỘ ====================================================

    /** Đảm bảo đã kết nối trước khi gửi/subscribe; log cảnh báo nếu chưa. */
    private boolean ensureConnected() {
        if (stomp == null || !connected) {
            logW("Not connected. Call connect() first.");
            return false;
        }
        return true;
    }

    /** Escape đơn giản để tránh null/quote lỗi JSON thủ công (demo). */
    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }

    private static void logD(String msg) { Log.d(TAG, msg); }
    private static void logW(String msg) { Log.w(TAG, msg); }
    private static void logE(String msg, Throwable t) { Log.e(TAG, msg, t); }

    // ====== (TÙY CHỌN) TỰ ĐỘNG RECONNECT CƠ BẢN ===============================
    // Nếu muốn tự reconnect khi CLOSED/ERROR, bạn có thể thêm logic sau:
    // - Trong lifecycle CLOSED/ERROR: schedule reconnect (vd: new Handler().postDelayed(...))
    // - Nhớ tránh reconnect dồn dập (backoff).
    // - Ví dụ:
    //   if (autoReconnect && lastJwt != null) {
    //       new Handler(Looper.getMainLooper()).postDelayed(() -> connect(lastJwt), 2000);
    //   }
    // Triển khai tuỳ nhu cầu app của bạn.
}
