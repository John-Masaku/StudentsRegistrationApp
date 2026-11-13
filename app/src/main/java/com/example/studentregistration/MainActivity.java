
package com.example.studentregistration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    // Registration form components
    private EditText etFullNames;
    private EditText etIdNumber;
    private EditText etRegNumber;

    private RadioGroup rgGender;

    private Spinner spCourse;
    private Spinner spDepartment;

    private Button btnRegister;

    // Firebase
    private FirebaseDatabase database;
    private DatabaseReference studentsReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize XML components
        etFullNames = findViewById(R.id.etFullNames);
        etIdNumber = findViewById(R.id.etIdNumber);
        etRegNumber = findViewById(R.id.etRegNumber);

        rgGender = findViewById(R.id.rgGender);

        spCourse = findViewById(R.id.spCourse);
        spDepartment = findViewById(R.id.spDepartment);

        btnRegister = findViewById(R.id.btnRegister);

        // Initialize Firebase
        database = FirebaseDatabase.getInstance(
                "https://dekut-student-registrati-9d637-default-rtdb.firebaseio.com"
        );

        // Enable offline persistence
        try {
            database.setPersistenceEnabled(true);
        } catch (Exception error) {
            Log.d("FIREBASE",
                    "Persistence may already be enabled: "
                            + error.getMessage());
        }

        studentsReference = database.getReference("students");

        // Populate course dropdown
        String[] courses = {
                "Select Course",
                "Computer Science",
                "Information Technology",
                "Computer Technology"
        };

        ArrayAdapter<String> courseAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        courses
                );

        courseAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spCourse.setAdapter(courseAdapter);

        // Populate department dropdown
        String[] departments = {
                "Select Department",
                "Computer Science",
                "Information Technology"
        };

        ArrayAdapter<String> departmentAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        departments
                );

        departmentAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spDepartment.setAdapter(departmentAdapter);

        // Register button
        btnRegister.setOnClickListener(v -> registerStudent());
    }

    // Collect and validate student information
    private void registerStudent() {

        String fullName = etFullNames.getText()
                .toString().trim();

        String idNumber = etIdNumber.getText()
                .toString().trim();

        String registrationNumber = etRegNumber.getText()
                .toString().trim();

        // Validate full names
        if (fullName.isEmpty()) {
            etFullNames.setError("Enter full names");
            etFullNames.requestFocus();
            return;
        }

        // Validate National ID
        if (idNumber.isEmpty()) {
            etIdNumber.setError("Enter National ID number");
            etIdNumber.requestFocus();
            return;
        }

        // Validate registration number
        if (registrationNumber.isEmpty()) {
            etRegNumber.setError("Enter registration number");
            etRegNumber.requestFocus();
            return;
        }

        // Validate gender
        int selectedGenderId = rgGender.getCheckedRadioButtonId();

        if (selectedGenderId == -1) {
            Toast.makeText(this,
                    "Please select gender",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedGender =
                findViewById(selectedGenderId);

        String gender = selectedGender.getText().toString();

        // Validate course
        if (spCourse.getSelectedItemPosition() == 0) {
            Toast.makeText(this,
                    "Please select a course",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String course = spCourse.getSelectedItem().toString();

        // Validate department
        if (spDepartment.getSelectedItemPosition() == 0) {
            Toast.makeText(this,
                    "Please select a department",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String department =
                spDepartment.getSelectedItem().toString();

        // Create student object
        Student student = new Student(
                fullName,
                idNumber,
                registrationNumber,
                gender,
                course,
                department
        );

        // Prevent repeated button clicks
        btnRegister.setEnabled(false);

        // Check both unique identifiers
        checkDuplicatesAndSave(
                student,
                idNumber,
                registrationNumber
        );
    }

    // Check whether the National ID or registration number exists
    private void checkDuplicatesAndSave(
            Student student,
            String idNumber,
            String registrationNumber) {

        Query idQuery = studentsReference
                .orderByChild("idNumber")
                .equalTo(idNumber)
                .limitToFirst(1);

        idQuery.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot idSnapshot) {

                        if (idSnapshot.exists()) {

                            Toast.makeText(MainActivity.this,
                                    "This National ID is already registered.",
                                    Toast.LENGTH_LONG).show();

                            btnRegister.setEnabled(true);
                            return;
                        }

                        // National ID is available.
                        // Now check registration number.
                        checkRegistrationNumber(
                                student,
                                registrationNumber
                        );
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Log.e("DUPLICATE_CHECK",
                                "National ID check failed",
                                error.toException());

                        Toast.makeText(MainActivity.this,
                                "Could not verify National ID. Try again.",
                                Toast.LENGTH_LONG).show();

                        btnRegister.setEnabled(true);
                    }
                }
        );
    }

    // Check registration number
    private void checkRegistrationNumber(
            Student student,
            String registrationNumber) {

        Query registrationQuery = studentsReference
                .orderByChild("registrationNumber")
                .equalTo(registrationNumber)
                .limitToFirst(1);

        registrationQuery.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(
                            @NonNull DataSnapshot regSnapshot) {

                        if (regSnapshot.exists()) {

                            Toast.makeText(MainActivity.this,
                                    "This registration number is already in use.",
                                    Toast.LENGTH_LONG).show();

                            btnRegister.setEnabled(true);
                            return;
                        }

                        // Both identifiers are available.
                        saveStudent(student);
                    }

                    @Override
                    public void onCancelled(
                            @NonNull DatabaseError error) {

                        Log.e("DUPLICATE_CHECK",
                                "Registration number check failed",
                                error.toException());

                        Toast.makeText(MainActivity.this,
                                "Could not verify registration number. Try again.",
                                Toast.LENGTH_LONG).show();

                        btnRegister.setEnabled(true);
                    }
                }
        );
    }

    // Save student in Firebase
    private void saveStudent(Student student) {

        DatabaseReference studentRecordReference =
                studentsReference.push();

        String studentRecordKey =
                studentRecordReference.getKey();

        if (studentRecordKey == null) {

            Toast.makeText(this,
                    "Could not generate student record key.",
                    Toast.LENGTH_LONG).show();

            btnRegister.setEnabled(true);
            return;
        }

        studentRecordReference.setValue(student)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(MainActivity.this,
                            "Student registered successfully",
                            Toast.LENGTH_SHORT).show();

                    // Open dashboard
                    Intent intent = new Intent(
                            MainActivity.this,
                            DashboardActivity.class
                    );

                    intent.putExtra(
                            "studentID",
                            student.idNumber
                    );

                    intent.putExtra(
                            "studentRecordKey",
                            studentRecordKey
                    );

                    startActivity(intent);

                    finish();
                })
                .addOnFailureListener(error -> {

                    Log.e("FIREBASE",
                            "Student registration failed",
                            error);

                    Toast.makeText(MainActivity.this,
                            "Registration failed. Please try again.",
                            Toast.LENGTH_LONG).show();

                    btnRegister.setEnabled(true);
                });
    }
}
