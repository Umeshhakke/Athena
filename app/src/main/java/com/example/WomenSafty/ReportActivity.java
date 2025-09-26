package com.example.WomenSafty;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReportActivity extends Activity {

    private static final int FILE_SELECT_CODE = 0;
    private EditText editTextProblem;
    private TextView textFileName;
    private Uri fileUri = null;
    String phone= null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        editTextProblem = findViewById(R.id.editTextProblem);
        textFileName = findViewById(R.id.textFileName);
        Button buttonAttachFile = findViewById(R.id.buttonAttachFile);
        Button buttonSubmit = findViewById(R.id.buttonSubmit);

        buttonAttachFile.setOnClickListener(v -> openFileChooser());

        buttonSubmit.setOnClickListener(v -> submitReport());
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Any file type
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a file to attach"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                fileUri = data.getData();
                String fileName = fileUri.getLastPathSegment();
                textFileName.setText("File attached: " + fileName);
            }
        }
    }

    private void submitReport() {
        String problemDescription = editTextProblem.getText().toString().trim();

        if (problemDescription.isEmpty()) {
            Toast.makeText(this, "Please describe the problem", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String filename = "user_data.txt";
            FileInputStream fis = openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();

            // Convert to JSONObject
            JSONObject userJson = new JSONObject(sb.toString());

            // Get phone number
            phone = userJson.getString("phone");

            // Use the phone number
            Log.d("UserPhone", phone);

        } catch (Exception e) {
            e.printStackTrace();
        }


        new Thread(() -> {
            try {
                MultipartBody.Builder builder = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("user_id", phone) // replace with actual user ID
                        .addFormDataPart("reportD", problemDescription);

                if (fileUri != null) { // Only add file if selected
                    InputStream is = getContentResolver().openInputStream(fileUri);
                    byte[] fileBytes = new byte[is.available()];
                    is.read(fileBytes);
                    is.close();

                    String fileName = fileUri.getLastPathSegment();
                    RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse(getContentResolver().getType(fileUri)));
                    builder.addFormDataPart("file", fileName, fileBody);
                }

                RequestBody requestBody = builder.build();

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("https://h19c6sn3-3000.inc1.devtunnels.ms/api/report")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                final String resp = response.body().string();

                runOnUiThread(() -> {
                    Toast.makeText(ReportActivity.this, "Report submitted: " + resp, Toast.LENGTH_LONG).show();
                    editTextProblem.setText("");
                    fileUri = null;
                    textFileName.setText("No file attached");
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(ReportActivity.this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();

        Intent intent = new Intent(ReportActivity.this, DashActivity.class);
        startActivity(intent);
        finish();
    }

}
