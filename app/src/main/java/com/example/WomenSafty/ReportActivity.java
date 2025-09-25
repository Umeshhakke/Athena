package com.example.WomenSafty;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ReportActivity extends Activity {

    private static final int FILE_SELECT_CODE = 0;
    private EditText editTextProblem;
    private TextView textFileName;
    private Uri fileUri = null;

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

        // Process the problemDescription and fileUri for submission here.
        // For example, upload to server.

        Toast.makeText(this, "Report submitted successfully", Toast.LENGTH_SHORT).show();

        // Optionally clear fields after submission
        editTextProblem.setText("");
        fileUri = null;
        textFileName.setText("No file attached");
        // Navigate back to the DashboardActivity

        Intent intent = new Intent(ReportActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
