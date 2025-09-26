package com.example.WomenSafty;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DashActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 100;

    private Button btnSOS, btnLiveLocation;
    private ImageView navHome, navChat, navLocation;
    private ImageButton btnUserProfile;
    private TextView chatMessage;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean isTracking = false; // to track status
    Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash);

        // Initialize views
        btnSOS = findViewById(R.id.btnSOS);
        btnLiveLocation = findViewById(R.id.btnLiveLocation);
        navHome = findViewById(R.id.navHome);
        navChat = findViewById(R.id.navChat);
        navLocation = findViewById(R.id.navLocation);
        btnUserProfile = findViewById(R.id.btnUserProfile);
        chatMessage = findViewById(R.id.chatMessage);

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // SOS button
        btnSOS.setOnClickListener(v -> {
            try {
                // Read user's phone number from user_data.txt
                FileInputStream fis = openFileInput("user_data.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject userJson = new JSONObject(sb.toString());
                String phoneNo = userJson.getString("phone"); // fetch phone number

                new Thread(() -> {
                    try {
                        // Build multipart request (even though no file, we can use form data)
                        MultipartBody.Builder builder = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("phone_no", phoneNo)
                                .addFormDataPart("status", "pending");

                        RequestBody requestBody = builder.build();

                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url("https://h19c6sn3-3000.inc1.devtunnels.ms/api/request_callback") // your server endpoint
                                .post(requestBody)
                                .build();

                        Response response = client.newCall(request).execute();
                        final String resp = response.body().string();

                        runOnUiThread(() -> {
                            Toast.makeText(DashActivity.this, "Request submitted: " + resp, Toast.LENGTH_LONG).show();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                Toast.makeText(DashActivity.this, "Request failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                        );
                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Live location button toggle
        btnLiveLocation.setOnClickListener(v -> {
            if (isTracking) {
                stopLiveLocationTracking();
            } else {
                startLiveLocationTracking();
            }
        });

        // Bottom nav buttons
        navHome.setOnClickListener(v -> {
            intent = new Intent(DashActivity.this, RecordingActivity.class);
            startActivity(intent);
        });

        navChat.setOnClickListener(v -> {
            intent = new Intent(DashActivity.this, DashboardActivity.class);
            startActivity(intent);
        });

        navLocation.setOnClickListener(v -> {
            intent = new Intent(DashActivity.this, LivelocationActivity.class);
            startActivity(intent);
        });

        // User profile button click - show menu
        btnUserProfile.setOnClickListener(this::showProfileMenu);
    }

    private void startLiveLocationTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        // Create location request
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // 5 seconds
        locationRequest.setFastestInterval(2000); // 2 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Define callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        String message = "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude();
                        chatMessage.setText(message);
                        try {
                            // 1. Get phone number from user_data.txt
                            FileInputStream fis = openFileInput("user_data.txt");
                            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) sb.append(line);
                            reader.close();

                            JSONObject userJson = new JSONObject(sb.toString());
                            String phoneNo = userJson.getString("phone");

                            // 2. Get current location
                            double latitude = location.getLatitude();   // update with real location
                            double longitude = location.getLongitude(); // update with real location

                            // 3. Send to server
                            JSONObject payload = new JSONObject();
                            payload.put("phone_no", phoneNo);
                            payload.put("latitude", latitude);
                            payload.put("longitude", longitude);

                            new Thread(() -> {
                                try {
                                    OkHttpClient client = new OkHttpClient();
                                    RequestBody body = RequestBody.create(
                                            payload.toString(), MediaType.parse("application/json")
                                    );

                                    Request request = new Request.Builder()
                                            .url("https://h19c6sn3-3000.inc1.devtunnels.ms/api/track")
                                            .post(body)
                                            .build();

                                    Response response = client.newCall(request).execute();
                                    response.close(); // Close to prevent leaks
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }).start();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(DashActivity.this, "Location Updated!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        // Start location updates
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        isTracking = true;
        btnLiveLocation.setText("Stop Tracking"); // change button text
        Toast.makeText(this, "Live tracking started!", Toast.LENGTH_SHORT).show();

    }

    private void stopLiveLocationTracking() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isTracking = false;
            btnLiveLocation.setText("Start Live Location"); // revert button text
            chatMessage.setText("Tracking stopped");
            Toast.makeText(this, "Live tracking stopped!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLiveLocationTracking();
            } else {
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showProfileMenu(View view) {
        PopupMenu popup = new PopupMenu(DashActivity.this, view);
        popup.getMenuInflater().inflate(R.menu.profile_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menuProfile) {
                Toast.makeText(DashActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.menuSettings) {
                Intent intent = new Intent(DashActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            } else if (id == R.id.menuLogout) {
                Toast.makeText(DashActivity.this, "Logout clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });

        popup.show();
    }
}