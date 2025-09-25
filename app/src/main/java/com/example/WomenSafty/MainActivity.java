package com.example.WomenSafty;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnRegister;

    private String savedEmail = null;
    private String savedPassword = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 101);
            } else {
                startService(new Intent(this, FloatingWidgetService.class));
            }
        }

        initializeViews();
        loadUserDataAndPrefill();  // load data and prefill inputs
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void loadUserDataAndPrefill() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput("user_data.txt")));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();

            String jsonData = stringBuilder.toString();
            JSONObject userJson = new JSONObject(jsonData);
            savedEmail = userJson.optString("email", "");
            savedPassword = userJson.optString("password", "");

            etEmail.setText(savedEmail);
            etPassword.setText(savedPassword);

        } catch (Exception e) {
            e.printStackTrace();
            // No file or error, keep empty fields
        }
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());

        btnRegister.setOnClickListener(v -> openRegisterScreen());
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isValidCredentials(email, password)) {
            Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
            openHomeScreen();
        } else {
            Toast.makeText(MainActivity.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidCredentials(String email, String password) {
        return email.equals(savedEmail) && password.equals(savedPassword);
    }

    private void openHomeScreen() {
        Intent intent = new Intent(MainActivity.this, DashActivity.class);
        startActivity(intent);
        finish();
    }

    private void openRegisterScreen() {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}
