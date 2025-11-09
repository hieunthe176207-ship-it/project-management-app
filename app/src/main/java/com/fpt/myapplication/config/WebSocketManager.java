package com.fpt.myapplication.config;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
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
 * - Cho phép nhiều Activity/Fragment đăng ký làm "listener".
 * - Quản lý subscribe theo topic, tránh trùng lặp + tự re-subscribe sau reconnect.
 * - Auto-reconnect với exponential backoff + jitter, có heartbeat.
 * - Hỗ trợ TokenProvider để refresh JWT trước mỗi lần reconnect.
 *
 * Cách dùng gợi ý:
 *   WebSocketManager ws = WebSocketManager.get();
 *   ws.setTokenProvider(() -> SessionPrefs.getJwt()); // nếu JWT có thể thay đổi
 *   ws.connect(null); // truyền null để lấy từ TokenProvider
 *   ws.subscribeTopic("/topic/public");
 *
 *   // Trong Activity/Fragment:
 *   ws.addListener(listener);   // onStart()
 *   ws.removeListener(listener);// onStop()
 *
 *   // Nếu muốn ngắt hẳn (sign out…):
 *   ws.disconnect();
 */
public class WebSocketManager {

    // ================== CẤU HÌNH CHUNG ========================================

    private static final String TAG = "WebSocketManager";

    /**
     * URL WebSocket thuần (KHÔNG SockJS).
     * - Ví dụ: "wss://booking.realmreader.site/ws"
     */

    /** Singleton instance */
    private static WebSocketManager INSTANCE;

    /** Lấy instance Singleton (thread-safe) */
    public static synchronized WebSocketManager get() {
        if (INSTANCE == null) INSTANCE = new WebSocketManager();
        return INSTANCE;
    }

    // ================== TRẠNG THÁI & THÀNH PHẦN CHÍNH =========================

    /** STOMP client chạy trên OkHttp */
    private StompClient stomp;

    /** Cờ trạng thái kết nối */
    private volatile boolean connected = false;

    /** Token dùng lần connect gần nhất (để reconnect) */
    private volatile String lastJwt = null;

    /** Người dùng có chủ động gọi disconnect hay không */
    private volatile boolean userInitiatedDisconnect = false;

    /** Handler main thread để schedule reconnect */
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // ================== RECONNECT CONFIG ======================================

    private boolean autoReconnect = true;   // bật/tắt auto-reconnect
    private int reconnectAttempts = 0;
    private int maxReconnectAttempts = 10;  // có thể set Integer.MAX_VALUE
    private long baseBackoffMs = 1_000L;    // 1s
    private long maxBackoffMs  = 30_000L;   // 30s
    private long jitterMs      = 500L;      // ±500ms

    // ================== TOKEN PROVIDER (TÙY CHỌN) =============================

    /**
     * Dùng khi JWT có thể hết hạn; mỗi lần reconnect sẽ xin token mới.
     */
    public interface TokenProvider {
        String getTokenOrNull();
    }
    private volatile TokenProvider tokenProvider = null;

    public void setTokenProvider(TokenProvider p) { this.tokenProvider = p; }

    // ================== LISTENER & SUBSCRIPTION ================================

    public interface MessageListener {
        void onConnected();
        void onDisconnected();
        void onError(String error);
        void onNewMessage(String topic, String payload);
    }

    /** Tập listeners an toàn luồng */
    private final Set<MessageListener> listeners = new CopyOnWriteArraySet<>();

    /** Map topic -> Disposable để quản lý subscribe/unsubscribe */
    private final Map<String, Disposable> topicSubscriptions = new ConcurrentHashMap<>();

    /** “Ý định” subscribe: để re-subscribe sau reconnect */
    private final Set<String> desiredTopics = new CopyOnWriteArraySet<>();

    /** Lưu tin cuối theo topic (tuỳ chọn) để replay cho màn hình mới mở */
    private final Map<String, String> lastMessageByTopic = new ConcurrentHashMap<>();

    // ================== CONSTRUCTOR ===========================================

    private WebSocketManager() { /* ép dùng Singleton */ }

    // ================== API: LISTENER ==========================================

    public void addListener(MessageListener l) {
        if (l != null) listeners.add(l);
    }

    public void removeListener(MessageListener l) {
        if (l != null) listeners.remove(l);
    }

    public boolean isConnected() {
        return connected;
    }

    /** Phát lại “tin cuối” của 1 topic cho listener mới (tuỳ chọn) */
    public void replayLastFor(String topic, MessageListener l) {
        if (l == null || topic == null) return;
        String last = lastMessageByTopic.get(topic);
        if (last != null) l.onNewMessage(topic, last);
    }

    // ================== API: KẾT NỐI / NGẮT KẾT NỐI ===========================

    /**
     * Mở kết nối STOMP.
     * - Ưu tiên jwtToken truyền vào; nếu null sẽ lấy từ TokenProvider (nếu có).
     * - Thiết lập heartbeat nếu lib hỗ trợ.
     * - Lắng nghe lifecycle; re-subscribe topic sau khi OPENED.
     * - Auto-reconnect khi ERROR/CLOSED.
     */
    public synchronized void connect(String jwtToken) {
        if (stomp != null && connected) return;

        userInitiatedDisconnect = false; // đánh dấu không phải ngắt do user

        // Lấy token
        if ((jwtToken == null || jwtToken.isEmpty()) && tokenProvider != null) {
            jwtToken = tokenProvider.getTokenOrNull();
        }
        if (jwtToken == null || jwtToken.isEmpty()) {
            logW("connect(): missing JWT");
            return;
        }
        this.lastJwt = jwtToken;

        Map<String, String> httpHeaders = new HashMap<>();
        httpHeaders.put("Authorization", "Bearer " + jwtToken);
        stomp = Stomp.over(Stomp.ConnectionProvider.OKHTTP, Constant.SOCKET_URL, httpHeaders);


        // Heartbeat (nếu version lib hỗ trợ)
        try {
            stomp.withClientHeartbeat(10_000);  // gửi ping mỗi 10s
            stomp.withServerHeartbeat(10_000);  // kỳ vọng server ping về mỗi 10s
        } catch (Throwable ignore) {
            // Một số phiên bản không có API này
        }

        // Header Authorization cho frame CONNECT
        StompHeader authHeader = new StompHeader("Authorization", "Bearer " + jwtToken);

        // Lắng nghe vòng đời
        stomp.lifecycle().subscribe(event -> {
            switch (event.getType()) {
                case OPENED:
                    connected = true;
                    reconnectAttempts = 0; // reset backoff
                    logD("STOMP OPENED");
                    resubscribeAll(); // re-subscribe lại các topic mong muốn
                    for (MessageListener l : listeners) l.onConnected();
                    break;

                case ERROR:
                    connected = false;
                    String err = (event.getException() != null)
                            ? event.getException().getMessage()
                            : "Unknown STOMP error";
                    logE("Lifecycle ERROR: " + err, event.getException());
                    for (MessageListener l : listeners) l.onError(err);
                    scheduleReconnectIfNeeded();
                    break;

                case CLOSED:
                    connected = false;
                    logW("STOMP CLOSED");
                    for (MessageListener l : listeners) l.onDisconnected();
                    scheduleReconnectIfNeeded();
                    break;
            }
        }, throwable -> {
            String err = throwable.getMessage();
            logE("Lifecycle subscribe error: " + err, throwable);
            for (MessageListener l : listeners) l.onError("Lifecycle subscribe error: " + err);
            scheduleReconnectIfNeeded();
        });

        // Thực hiện CONNECT
        stomp.connect(Collections.singletonList(authHeader));
    }

    /**
     * Ngắt kết nối hoàn toàn:
     * - Hủy subscriptions hiện tại.
     * - Đóng STOMP client và đặt cờ để ngăn auto-reconnect.
     */
    public synchronized void disconnect() {
        userInitiatedDisconnect = true;  // ngăn auto-reconnect
        // Nếu muốn tắt auto-reconnect tạm thời khi disconnect:
        // autoReconnect = false;

        // Hủy các topic subscribe
        for (Disposable d : topicSubscriptions.values()) {
            if (d != null && !d.isDisposed()) d.dispose();
        }
        topicSubscriptions.clear();

        // Đóng client
        if (stomp != null) {
            try { stomp.disconnect(); } catch (Throwable ignore) {}
            stomp = null;
        }
        connected = false;
    }

    // ================== API: SUBSCRIBE / UNSUBSCRIBE ===========================

    /**
     * Subscribe 1 topic STOMP (ví dụ "/topic/public", "/user/queue/chat").
     * - Lưu vào desiredTopics để re-subscribe sau reconnect.
     * - Tránh subscribe trùng.
     */
    public synchronized void subscribeTopic(String topic) {
        if (topic == null || topic.isEmpty()) return;

        desiredTopics.add(topic); // lưu ý định

        if (stomp == null || !connected) {
            logW("subscribeTopic: not connected yet, will subscribe after reconnect: " + topic);
            return;
        }
        if (topicSubscriptions.containsKey(topic)) {
            // đã subscribe
            return;
        }

        Disposable sub = stomp.topic(topic).subscribe(
                // onNext
                (StompMessage msg) -> {
                    String payload = msg.getPayload();
                    lastMessageByTopic.put(topic, payload); // tuỳ chọn
                    for (MessageListener l : listeners) l.onNewMessage(topic, payload);
                },
                // onError
                (Throwable t) -> {
                    String err = t.getMessage();
                    logE("Subscribe error for " + topic + ": " + err, t);
                    for (MessageListener l : listeners)
                        l.onError("Subscribe error (" + topic + "): " + err);
                    topicSubscriptions.remove(topic);
                }
        );

        topicSubscriptions.put(topic, sub);
    }

    /** Hủy subscribe 1 topic. */
    public synchronized void unsubscribeTopic(String topic) {
        if (topic == null) return;
        desiredTopics.remove(topic);
        Disposable d = topicSubscriptions.remove(topic);
        if (d != null && !d.isDisposed()) d.dispose();
        // Có thể giữ lastMessageByTopic để replay sau, hoặc xoá nếu muốn:
        // lastMessageByTopic.remove(topic);
    }

    // ================== API: GỬI TIN ==========================================

    /**
     * Gửi payload JSON đến 1 destination STOMP ở "app" (ví dụ "/app/chat").
     * Trên Spring: @MessageMapping("/chat") → "/app/chat"
     */
    public void send(String destinationApp, String jsonBody) {
        if (!ensureConnected()) return;
        stomp.send(destinationApp, jsonBody).subscribe(
                () -> logD("send OK: " + destinationApp),
                err -> {
                    logE("send error: " + err.getMessage(), err);
                    for (MessageListener l : listeners) {
                        l.onError("Không gửi được tin: " + err.getMessage());
                    }
                }
        );
    }

    // ================== RECONNECT LOGIC =======================================

    private synchronized void scheduleReconnectIfNeeded() {
        if (!autoReconnect) return;
        if (userInitiatedDisconnect) return;
        if (connected) return;

        // Refresh token nếu có
        if (tokenProvider != null) {
            String refreshed = tokenProvider.getTokenOrNull();
            if (refreshed != null && !refreshed.isEmpty()) {
                lastJwt = refreshed;
            }
        }

        if (reconnectAttempts >= maxReconnectAttempts) {
            logW("Reached max reconnect attempts. Stop retrying.");
            return;
        }
        reconnectAttempts++;

        long backoff = Math.min(
                maxBackoffMs,
                (long) (baseBackoffMs * Math.pow(2, reconnectAttempts - 1))
        );
        long jitter = (long) (Math.random() * (2 * jitterMs)) - jitterMs; // [-jitter, +jitter]
        long delay = Math.max(0, backoff + jitter);

        logW("Will try reconnect in ~" + delay + " ms (attempt " + reconnectAttempts + ")");
        mainHandler.postDelayed(() -> {
            if (!connected && !userInitiatedDisconnect) {
                try {
                    connect(lastJwt);
                } catch (Throwable t) {
                    logE("Reconnect attempt failed fast: " + t.getMessage(), t);
                    scheduleReconnectIfNeeded();
                }
            }
        }, delay);
    }

    private synchronized void resubscribeAll() {
        for (String t : desiredTopics) {
            if (!topicSubscriptions.containsKey(t)) {
                try {
                    subscribeTopic(t);
                } catch (Throwable e) {
                    logE("resubscribe fail: " + t, e);
                }
            }
        }
    }

    // ================== TIỆN ÍCH NỘI BỘ =======================================

    private boolean ensureConnected() {
        if (stomp == null || !connected) {
            logW("Not connected. Call connect() first.");
            return false;
        }
        return true;
    }

    private static void logD(String msg) { Log.d(TAG, msg); }
    private static void logW(String msg) { Log.w(TAG, msg); }
    private static void logE(String msg, Throwable t) { Log.e(TAG, msg, t); }

    // ================== TUỲ CHỈNH HÀNH VI (SETTER) =============================

    public void setAutoReconnect(boolean autoReconnect) { this.autoReconnect = autoReconnect; }
    public void setMaxReconnectAttempts(int maxReconnectAttempts) { this.maxReconnectAttempts = Math.max(1, maxReconnectAttempts); }
    public void setBaseBackoffMs(long baseBackoffMs) { this.baseBackoffMs = Math.max(100L, baseBackoffMs); }
    public void setMaxBackoffMs(long maxBackoffMs) { this.maxBackoffMs = Math.max(500L, maxBackoffMs); }
    public void setJitterMs(long jitterMs) { this.jitterMs = Math.max(0L, jitterMs); }
}
