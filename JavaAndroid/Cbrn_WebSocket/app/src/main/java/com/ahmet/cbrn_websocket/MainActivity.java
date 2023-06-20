package com.ahmet.cbrn_websocket;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "WebSocket";

    private WebSocket webSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://10.42.0.1/xip/api/device/stream").build();

        WebSocketListener webSocketListener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket bağlantısı başarıyla açıldı.");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Mesaj alındı: " + text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                Log.d(TAG, "Binary mesaj alındı: " + bytes.hex());
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.e(TAG, "WebSocket hatası: " + t.getMessage());
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d(TAG, "WebSocket bağlantısı kapatıldı.");
            }
        };

        webSocket = client.newWebSocket(request, webSocketListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webSocket.cancel();
    }
}
