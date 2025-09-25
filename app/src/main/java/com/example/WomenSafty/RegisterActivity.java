package com.example.WomenSafty;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.FileOutputStream;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etPhone, etEmergencyContact;
    private Button btnRegister;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPhone = findViewById(R.id.etPhone);
        etEmergencyContact = findViewById(R.id.etEmergencyContact);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> registerUser());
        tvLoginLink.setOnClickListener(v -> navigateToLogin());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String emergency = etEmergencyContact.getText().toString().trim();

        if (!validateInputs(name, email, password, phone, emergency)) return;

        // Save data locally
        if (saveUserToLocalFile(name, email, password, phone, emergency)) {
            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            navigateToLogin();
        } else {
            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInputs(String name, String email, String password, String phone, String emergency) {
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name required"); etName.requestFocus(); return false;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required"); etEmail.requestFocus(); return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 chars");
            etPassword.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone required"); etPhone.requestFocus(); return false;
        }
        if (TextUtils.isEmpty(emergency)) {
            etEmergencyContact.setError("Emergency contact required"); etEmergencyContact.requestFocus(); return false;
        }
        return true;
    }

    private boolean saveUserToLocalFile(String name, String email, String password, String phone, String emergency) {
        try {
            JSONObject userJson = new JSONObject();
            userJson.put("name", name);
            userJson.put("email", email);
            userJson.put("password", password); // optionally encrypt or hash password in real app
            userJson.put("phone", phone);
            userJson.put("emergencyContact", emergency);

            String filename = "user_data.txt";
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            fos.write(userJson.toString().getBytes());
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
