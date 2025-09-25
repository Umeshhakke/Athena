package com.example.WomenSafty;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class FloatingWidgetService extends Service {

    private WindowManager windowManager;
    private View floatingView;
    private boolean isCalling = false;
    private TelephonyManager telephonyManager;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Determine layout flag based on SDK
        int layoutFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // Create circular SOS button
        Button sosButton = new Button(this);
        sosButton.setText("SOS");
        sosButton.setTextColor(Color.WHITE);
        sosButton.setTextSize(16);
        sosButton.setAllCaps(false);
        sosButton.setGravity(Gravity.CENTER);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(Color.RED);
        shape.setStroke(4, Color.WHITE);
        sosButton.setBackground(shape);

        // Set fixed size for perfect circle
        int size = 150; // px, adjust as needed
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                size, size,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.END; // initial position
        params.x = 50;
        params.y = 100;

        // Make button draggable
        sosButton.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isDragging;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragging = false;
                        return false; // allow click to be detected

                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);

                        if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                            params.x = initialX + deltaX;
                            params.y = initialY + deltaY;
                            windowManager.updateViewLayout(sosButton, params);
                            isDragging = true;
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        return isDragging;
                }
                return false;
            }
        });

        // Add button to window
        windowManager.addView(sosButton, params);
        floatingView = sosButton;

        // Handle button click
        sosButton.setOnClickListener(v -> {
            Toast.makeText(this, "SOS Triggered!", Toast.LENGTH_SHORT).show();
            callEmergencyNumber();

            // Listen for call end to start recording
            telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            telephonyManager.listen(new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String phoneNumber) {
                    super.onCallStateChanged(state, phoneNumber);

                    switch (state) {
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                            isCalling = true;
                            break;

                        case TelephonyManager.CALL_STATE_IDLE:
                            if (isCalling) {
                                isCalling = false;
                                startRecordingActivity();
                            }
                            break;
                    }
                }
            }, PhoneStateListener.LISTEN_CALL_STATE);
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (floatingView != null) windowManager.removeView(floatingView);
        if (telephonyManager != null)
            telephonyManager.listen(null, PhoneStateListener.LISTEN_NONE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void callEmergencyNumber() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {

                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:123456"));
                callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(callIntent);

            } else {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:100"));
                dialIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(dialIntent);

                Toast.makeText(this, "Using dialer. Grant call permission for direct calling.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error making emergency call", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRecordingActivity() {
        Intent recordingIntent = new Intent(this, RecordingActivity.class);
        recordingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(recordingIntent);
    }
}
