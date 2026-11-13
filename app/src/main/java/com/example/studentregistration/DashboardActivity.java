
package com.example.studentregistration;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvDashboardWelcome;

    private Button btnLibraryService;
    private Button btnHostelService;
    private Button btnMpesaService;
    private Button btnRegistrationStatus;

    private String studentID = "";
    private String studentRecordKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Connect Java variables to XML components
        tvDashboardWelcome = findViewById(R.id.tvDashboardWelcome);

        btnLibraryService = findViewById(R.id.btnLibraryService);
        btnHostelService = findViewById(R.id.btnHostelService);
        btnMpesaService = findViewById(R.id.btnMpesaService);
        btnRegistrationStatus = findViewById(R.id.btnRegistrationStatus);

        // Receive student information
        studentID = getIntent().getStringExtra("studentID");
        studentRecordKey = getIntent().getStringExtra("studentRecordKey");

        if (studentID == null) {
            studentID = "";
        }

        if (studentRecordKey == null) {
            studentRecordKey = "";
        }

        // Display welcome message
        if (!studentID.isEmpty()) {
            tvDashboardWelcome.setText(
                    "Welcome, Student!\nID: " + studentID
            );
        }

        // Open Library Activity
        btnLibraryService.setOnClickListener(v ->
                openService(LibraryActivity.class)
        );

        // Open Hostel Activity
        btnHostelService.setOnClickListener(v ->
                openService(HostelActivity.class)
        );

        // Open M-PESA Activity
        btnMpesaService.setOnClickListener(v ->
                openService(MpesaActivity.class)
        );

        // Registration status will be implemented next
        btnRegistrationStatus.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DashboardActivity.this,
                    StatusActivity.class
            );

            intent.putExtra("studentID", studentID);
            intent.putExtra("studentRecordKey", studentRecordKey);

            startActivity(intent);
        });
    }

    private void openService(Class<?> activityClass) {

        if (studentID.isEmpty() || studentRecordKey.isEmpty()) {
            Toast.makeText(this,
                    "Student information is missing",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(
                DashboardActivity.this,
                activityClass
        );

        // Pass student information to the selected activity
        intent.putExtra("studentID", studentID);
        intent.putExtra("studentRecordKey", studentRecordKey);

        startActivity(intent);
    }
}
