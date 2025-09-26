package com.example.WomenSafty;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
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

import android.telephony.SmsManager;
import android.content.Intent;


public class DashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageButton btnSend, btnClear;
    private LinearLayout typingIndicator;

    private List<ChatMessage> messageList;
    private ChatAdapter chatAdapter;

    private Button btn1, btn2, btn3, btn4, btn5, btn6;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_REQUEST_CODE = 100;
    private static final int SMS_PERMISSION_CODE = 200;
    private boolean isSharingLocation = false;
    private static final long LOCATION_INTERVAL = 10000; // 10 sec
    private static final long LOCATION_DURATION = 5 * 60 * 1000; // 5 min
    private boolean locationSent = false;  // Add at class level


    private static final String GEMINI_API_KEY = "AIzaSyAeEv7wtgoYzHXWMIO0ZerWKCYyMn-zvhI";
    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=";
    private final OkHttpClient client = new OkHttpClient();

    private String emergencyNumber = ""; // Change as needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
        btn6 = findViewById(R.id.btn6);
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

        btn1.setOnClickListener(v -> sendQuickMessage("Emergency Help"));
        btn2.setOnClickListener(v -> sendQuickMessage("Nearest Police Station"));
        btn3.setOnClickListener(v -> sendQuickMessage("Self-defense tips"));
        btn4.setOnClickListener(v -> sendQuickMessage("Share my live location"));
        btn5.setOnClickListener(v -> sendQuickMessage("Call trusted contact"));
        btn6.setOnClickListener(v -> sendQuickMessage("Report Police"));

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

        switch (quickText) {
            case "Emergency Help":
                addMessage("🚨 Stay calm! Help is on the way. Press the SOS button to alert your contacts and nearby authorities.", false);
                break;

            case "Nearest Police Station":
                handleNearestPoliceStation();
                break;

            case "Self-defense tips":
                addMessage("🛡️ Quick tips:\n1. Stay aware of your surroundings.\n2. Use keys or small objects as weapons.\n3. Aim for sensitive areas (eyes, nose, groin).\n4. Shout loudly to draw attention.", false);
                break;

            case "Share my live location":
                emergencyNumber = getEmergencyNumber();
                startLiveLocationSharing();
                break;

            case "Call trusted contact":
                emergencyNumber = getEmergencyNumber();
                callTrustedContact();
                break;

            case "Report Police":
                Intent intent = new Intent(DashboardActivity.this, ReportActivity.class);
                startActivity(intent);
                break;
            default:
                sendToGemini(quickText);
                break;
        }
        showTypingIndicator(false);
    }

    private void handleNearestPoliceStation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST_CODE);
            addMessage("⚠️ Location permission required.", false);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();

                new AlertDialog.Builder(this)
                        .setTitle("Nearest Police Station")
                        .setMessage("📍 Nearest police stations found near your location.\nDo you want to open Google Maps?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            String uri = "geo:" + lat + "," + lng + "?q=police station";
                            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            mapIntent.setPackage("com.google.android.apps.maps");
                            startActivity(mapIntent);
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();

                addMessage("📍 Found nearest police stations. Check popup.", false);
            } else {
                addMessage("⚠️ Unable to fetch location.", false);
            }
        });
    }

    private void callTrustedContact() {
        addMessage("📞 Calling trusted contact...", false);
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL); // Direct call
            callIntent.setData(Uri.parse("tel:" + emergencyNumber));

            // Check CALL_PHONE permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CALL_PHONE},
                        300);
                addMessage("⚠️ Call permission required.", false);
                return;
            }

            startActivity(callIntent); // Starts the phone call
        } catch (Exception e) {
            addMessage("⚠️ Failed to call: " + e.getMessage(), false);
        }
    }


    private void startLiveLocationSharing() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);
            addMessage("⚠️ Location permission required.", false);
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE);
            addMessage("⚠️ SMS permission required.", false);
            return;
        }

        isSharingLocation = true;
        addMessage("📡 Live location sharing started for 5 minutes.", false);

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // GPS first

        LocationCallback locationCallback = new LocationCallback() {
            long startTime = System.currentTimeMillis();

            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (!isSharingLocation) return;

                Location location = null;

                // Try GPS
                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    location = locationResult.getLastLocation();
                }

                // Fallback to SIM/network if GPS unavailable
                if (location == null) {
                    try {
                        if (ActivityCompat.checkSelfPermission(DashboardActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(DashboardActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // Request permissions if not granted
                            ActivityCompat.requestPermissions(DashboardActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                                    LOCATION_REQUEST_CODE);
                            addMessage("⚠️ Location permission required for SIM/network location.", false);
                            return;
                        }

                        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, null)
                                .addOnSuccessListener(loc -> {
                                    if (loc != null && !locationSent) {
                                        locationSent = true;
                                        sendLocationSms(loc);
                                    }
                                });
                    } catch (Exception e) {
                        addMessage("⚠️ Failed to get SIM location: " + e.getMessage(), false);
                    }

                } else if (!locationSent) {
                    locationSent = true;
                    sendLocationSms(location);
                }

                if (System.currentTimeMillis() - startTime >= LOCATION_DURATION) {
                    isSharingLocation = false;
                    fusedLocationClient.removeLocationUpdates(this);
                    addMessage("✅ Live location sharing ended.", false);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void sendLocationSms(Location location) {
        if (location == null) return;
        String lat = String.valueOf(location.getLatitude());
        String lng = String.valueOf(location.getLongitude());
        String link = "https://maps.google.com/?q=" + lat + "," + lng;
        sendSms(emergencyNumber, "📡 Emergency! Live location: " + link);
        addMessage("📡 Live location sent: " + link, false);
    }


    private void sendSms(String number, String message) {
        try {
            Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
            smsIntent.setData(Uri.parse("smsto:" + number));
            smsIntent.putExtra("sms_body", message);
            startActivity(smsIntent);

            addMessage("📡 SMS prepared to send: " + message, false);
        } catch (Exception e) {
            addMessage("⚠️ Failed to send SMS: " + e.getMessage(), false);
        }
    }


    private void addMessage(String message, boolean isUser) {
        ChatMessage chatMessage = new ChatMessage(message, isUser);
        messageList.add(chatMessage);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }

    private void sendToGemini(String userMessage) {
        try {
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

            Request request = new Request.Builder()
                    .url(GEMINI_API_URL + GEMINI_API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        showTypingIndicator(false);
                        addMessage("⚠️ Gemini failed: " + e.getMessage(), false);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> {
                            showTypingIndicator(false);
                            addMessage("⚠️ Gemini error: " + response.code(), false);
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
                            addMessage("⚠️ Gemini parse error: " + e.getMessage(), false);
                        });
                    }
                }
            });

        } catch (Exception e) {
            showTypingIndicator(false);
            addMessage("⚠️ Gemini exception: " + e.getMessage(), false);
        }
    }

    private void showTypingIndicator(boolean show) {
        typingIndicator.setVisibility(show ? LinearLayout.VISIBLE : LinearLayout.GONE);
    }

    private void clearChat() {
        messageList.clear();
        chatAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addMessage("✅ Location permission granted. Please tap the action again.", false);
            } else {
                addMessage("⚠️ Location permission denied. Cannot proceed.", false);
            }
        } else if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addMessage("✅ SMS permission granted. Please tap the action again.", false);
            } else {
                addMessage("⚠️ SMS permission denied. Cannot send SMS.", false);
            }
        }
    }

    private String getEmergencyNumber() {
        String number = null;
        String filename = "user_data.txt"; // your JSON file
        File file = new File(getFilesDir(), filename);

        if (file.exists()) {
            try {
                FileInputStream fis = openFileInput(filename);
                int size = fis.available();
                byte[] buffer = new byte[size];
                fis.read(buffer);
                fis.close();

                String jsonStr = new String(buffer, "UTF-8");
                JSONObject userJson = new JSONObject(jsonStr);

                number = userJson.optString("emergencyContact", null);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return number;
    }
}

