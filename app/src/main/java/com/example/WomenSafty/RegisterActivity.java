package com.example.WomenSafty;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etPhone, etEmergencyContact;
    private Button btnRegister;
    private TextView tvLoginLink;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize Firebase Auth & Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

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

        // Create Firebase Auth user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore(name, email, phone, emergency);
                    } else {
                        String errorMsg = task.getException() != null ?
                                task.getException().getMessage() : "Registration failed";
                        Toast.makeText(RegisterActivity.this,
                                "Registration failed: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateInputs(String name, String email, String password, String phone, String emergency) {
        if (TextUtils.isEmpty(name)) { etName.setError("Name required"); etName.requestFocus(); return false; }
        if (TextUtils.isEmpty(email)) { etEmail.setError("Email required"); etEmail.requestFocus(); return false; }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 chars");
            etPassword.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(phone)) { etPhone.setError("Phone required"); etPhone.requestFocus(); return false; }
        if (TextUtils.isEmpty(emergency)) { etEmergencyContact.setError("Emergency contact required"); etEmergencyContact.requestFocus(); return false; }
        return true;
    }

    private void saveUserToFirestore(String name, String email, String phone, String emergency) {
        String uid = mAuth.getCurrentUser().getUid();

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email);
        userMap.put("phone", phone);
        userMap.put("emergencyContact", emergency);

        db.collection("users").document(uid)
                .set(userMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        navigateToLogin();
                    } else {
                        if (mAuth.getCurrentUser() != null) mAuth.getCurrentUser().delete();
                        Toast.makeText(RegisterActivity.this, "Failed to save user info. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
