package com.example.WomenSafty;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageButton btnSend, btnClear;
    private LinearLayout typingIndicator;

    private List<ChatMessage> messageList;
    private ChatAdapter chatAdapter;

    private Button btn1, btn2, btn3, btn4, btn5;

    // 🔑 Replace with your Gemini API key
    private static final String GEMINI_API_KEY = "AIzaSyAeEv7wtgoYzHXWMIO0ZerWKCYyMn-zvhI";

    // ✅ Use Gemini 1.5 Flash model
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=";

    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnClear = findViewById(R.id.btnClear);
        typingIndicator = findViewById(R.id.typingIndicator);

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
    }

    private void setupRecyclerView() {
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
    }

    private void setupClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        btnClear.setOnClickListener(v -> clearChat());

        // Quick-action buttons
        btn1.setOnClickListener(v -> sendQuickMessage("Emergency Help"));
        btn2.setOnClickListener(v -> sendQuickMessage("Nearest Police Station"));
        btn3.setOnClickListener(v -> sendQuickMessage("Self-defense tips"));
        btn4.setOnClickListener(v -> sendQuickMessage("Share my live location"));
        btn5.setOnClickListener(v -> sendQuickMessage("Call trusted contact"));
    }

    private void sendMessage() {
        String message = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(message)) return;

        addMessage(message, true);
        etMessage.setText("");

        showTypingIndicator(true);
        sendToGemini(message);
    }

    private void sendQuickMessage(String quickText) {
        addMessage(quickText, true);
        showTypingIndicator(true);
        sendToGemini(quickText);
    }

    private void addMessage(String message, boolean isUser) {
        ChatMessage chatMessage = new ChatMessage(message, isUser);
        messageList.add(chatMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void sendToGemini(String userMessage) {
        try {
            // Build request body
            JSONObject jsonBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject part = new JSONObject();
            part.put("text", userMessage);

            JSONObject content = new JSONObject();
            content.put("parts", new JSONArray().put(part));
            contents.put(content);

            jsonBody.put("contents", contents);

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            // Send request
            Request request = new Request.Builder()
                    .url(GEMINI_API_URL + GEMINI_API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        showTypingIndicator(false);
                        addMessage("⚠️ Failed: " + e.getMessage(), false);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> {
                            showTypingIndicator(false);
                            addMessage("⚠️ Error: " + response.code(), false);
                        });
                        return;
                    }

                    try {
                        String res = response.body().string();
                        JSONObject resJson = new JSONObject(res);
                        String botReply = resJson
                                .getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        runOnUiThread(() -> {
                            showTypingIndicator(false);
                            addMessage(botReply, false);
                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            showTypingIndicator(false);
                            addMessage("⚠️ Parse error: " + e.getMessage(), false);
                        });
                    }
                }
            });

        } catch (Exception e) {
            showTypingIndicator(false);
            addMessage("⚠️ Exception: " + e.getMessage(), false);
        }
    }

    private void showTypingIndicator(boolean show) {
        typingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void clearChat() {
        messageList.clear();
        chatAdapter.notifyDataSetChanged();
    }
}
