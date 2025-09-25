package com.example.WomenSafty;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchCamera, switchLocation, switchMicrophone, switchContacts;
    private Button btnConnectWearable;

    private static final int PERMISSION_CAMERA = 100;
    private static final int PERMISSION_LOCATION = 101;
    private static final int PERMISSION_MICROPHONE = 102;
    private static final int PERMISSION_CONTACTS = 103;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings); // Make sure this is your correct layout file

        // Initialize switches
        switchCamera = findViewById(R.id.switchCameraPermission);
        switchLocation = findViewById(R.id.switchLocation);
        switchMicrophone = findViewById(R.id.switchMicrophonePermission);
        switchContacts = findViewById(R.id.switchContactsPermission);

        btnConnectWearable = findViewById(R.id.btnConnectWearable);

        // Set listeners for permission switches
        switchCamera.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) requestPermission(Manifest.permission.CAMERA, PERMISSION_CAMERA);
        });

        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, PERMISSION_LOCATION);
        });

        switchMicrophone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) requestPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_MICROPHONE);
        });

        switchContacts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) requestPermission(Manifest.permission.READ_CONTACTS, PERMISSION_CONTACTS);
        });

        // Wearable button
        btnConnectWearable.setOnClickListener(v -> {
            WearableConnector connector = new WearableConnector(SettingsActivity.this);
            connector.connect();
        });


        // Check permissions initially
        checkAllPermissions();
    }

    private void requestPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
        } else {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAllPermissions() {
        switchCamera.setChecked(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
        switchLocation.setChecked(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        switchMicrophone.setChecked(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);
        switchContacts.setChecked(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case PERMISSION_CAMERA:
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
                    break;
                case PERMISSION_LOCATION:
                    Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
                    break;
                case PERMISSION_MICROPHONE:
                    Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show();
                    break;
                case PERMISSION_CONTACTS:
                    Toast.makeText(this, "Contacts permission granted", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }

        checkAllPermissions(); // Update switches after permission change
    }
}
