
package com.example.studentregistration;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HostelActivity extends AppCompatActivity {

    private Spinner spHostelLocation, spRoomType;
    private RadioGroup rgShareable;
    private EditText etHostelStudentID;
    private Button btnSubmitHostel;

    private DatabaseReference hostelReference;

    private String studentID = "";

    private String studentRecordKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hostel);

        // Connect to Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://dekut-student-registrati-9d637-default-rtdb.firebaseio.com"
        );

        hostelReference = database.getReference("hostelBookings");

        // Connect Java variables to XML components
        spHostelLocation = findViewById(R.id.spHostelLocation);
        spRoomType = findViewById(R.id.spRoomType);
        rgShareable = findViewById(R.id.rgShareable);
        etHostelStudentID = findViewById(R.id.etHostelStudentID);
        btnSubmitHostel = findViewById(R.id.btnSubmitHostel);

        // Populate hostel locations
        String[] locations = {
                "Select Hostel Location",
                "Hostel A",
                "Hostel B",
                "Hostel C"
        };

        ArrayAdapter<String> locationAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                locations
        );

        locationAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spHostelLocation.setAdapter(locationAdapter);

        // Populate room types
        String[] roomTypes = {
                "Select Room Type",
                "Single",
                "Double",
                "Shared"
        };

        ArrayAdapter<String> roomAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                roomTypes
        );

        roomAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spRoomType.setAdapter(roomAdapter);

        // Receive Student ID and Student Recovery Key from LibraryActivity
        studentID = getIntent().getStringExtra("studentID");
        studentRecordKey = getIntent().getStringExtra("studentRecordKey");

        if (studentRecordKey == null) {
            studentRecordKey = "";
        }

        if (studentID != null && !studentID.isEmpty()) {
            etHostelStudentID.setText(studentID);
        }

        // Submit hostel booking
        btnSubmitHostel.setOnClickListener(v -> submitHostelBooking());
    }

    private void submitHostelBooking() {

        String location = spHostelLocation.getSelectedItem().toString();
        String roomType = spRoomType.getSelectedItem().toString();

        int selectedRadioId = rgShareable.getCheckedRadioButtonId();

        // Validate hostel location
        if (spHostelLocation.getSelectedItemPosition() == 0) {
            Toast.makeText(this,
                    "Please select a hostel location",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate room type
        if (spRoomType.getSelectedItemPosition() == 0) {
            Toast.makeText(this,
                    "Please select a room type",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate shareable selection
        if (selectedRadioId == -1) {
            Toast.makeText(this,
                    "Please indicate whether the room is shareable",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (studentID == null || studentID.isEmpty()) {
            Toast.makeText(this,
                    "Student ID is missing",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (studentRecordKey.isEmpty()) {
            Toast.makeText(this,
                    "Student record key is missing",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String shareable;

        if (selectedRadioId == R.id.rbShareableYes) {
            shareable = "Yes";
        } else {
            shareable = "No";
        }

        // Create hostel booking object
        HostelBooking booking = new HostelBooking(
                location,
                roomType,
                shareable,
                studentID
        );

        // Prevent multiple submissions
        btnSubmitHostel.setEnabled(false);

        // Save booking to Firebase
        hostelReference.push().setValue(booking)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(HostelActivity.this,
                            "Hostel booking submitted successfully",
                            Toast.LENGTH_SHORT).show();

                    finish();

                })
                .addOnFailureListener(error -> {

                    Log.e("HOSTEL_FIREBASE",
                            "Hostel booking failed",
                            error);

                    Toast.makeText(this,
                            "Failed to submit hostel booking",
                            Toast.LENGTH_LONG).show();

                    btnSubmitHostel.setEnabled(true);
                });
    }
}
