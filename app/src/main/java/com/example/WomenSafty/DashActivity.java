package com.example.WomenSafty;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DashActivity extends AppCompatActivity {

    private Button btnSOS, btnLiveLocation;
    private ImageView navHome, navChat, navLocation;
    private ImageButton btnUserProfile;
    private TextView chatMessage;
    Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash); // your layout filename

        // Initialize views
        btnSOS = findViewById(R.id.btnSOS);
        btnLiveLocation = findViewById(R.id.btnLiveLocation);
        navHome = findViewById(R.id.navHome);
        navChat = findViewById(R.id.navChat);
        navLocation = findViewById(R.id.navLocation);
        btnUserProfile = findViewById(R.id.btnUserProfile);
        chatMessage = findViewById(R.id.chatMessage);

        // SOS button
        btnSOS.setOnClickListener(v ->
                Toast.makeText(DashActivity.this, "SOS activated!", Toast.LENGTH_SHORT).show()
        );

        // Live location button
        btnLiveLocation.setOnClickListener(v ->
                Toast.makeText(DashActivity.this, "Live tracking started!", Toast.LENGTH_SHORT).show()
        );

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
        btnUserProfile.setOnClickListener(v -> showProfileMenu(v));
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
