
package com.example.studentregistration;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class LibraryActivity extends AppCompatActivity {

    private EditText etISBN, etAuthor, etBookTitle, etStudentID;

    private Button btnBorrowDate, btnReturnDate,
            btnSubmitBookRequest;

    private DatabaseReference libraryReference;

    private String dateBorrowed = "";
    private String dateOfReturn = "";

    // Student ID shared across this activity
    private String studentID = "";

    private String studentRecordKey="";

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        // Connect to Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://dekut-student-registrati-9d637-default-rtdb.firebaseio.com"
        );

        libraryReference = database.getReference("libraryRequests");

        // Connect Java variables to XML components
        etISBN = findViewById(R.id.etISBN);
        etAuthor = findViewById(R.id.etAuthor);
        etBookTitle = findViewById(R.id.etBookTitle);
        etStudentID = findViewById(R.id.etStudentID);

        btnBorrowDate = findViewById(R.id.btnBorrowDate);
        btnReturnDate = findViewById(R.id.btnReturnDate);
        btnSubmitBookRequest = findViewById(R.id.btnSubmitBookRequest);

        // Receive Student ID  and Student Recovery Key from MainActivity
        studentID = getIntent().getStringExtra("studentID");
        studentRecordKey = getIntent().getStringExtra("studentRecordKey");

        if (studentRecordKey == null) {
            studentRecordKey = "";
        }

        if (studentID != null && !studentID.isEmpty()) {
            etStudentID.setText(studentID);
            etStudentID.setEnabled(false);
        } else {
            studentID = "";
        }

        // Open date picker for borrowing date
        btnBorrowDate.setOnClickListener(v -> showDatePicker(true));

        // Open date picker for return date
        btnReturnDate.setOnClickListener(v -> showDatePicker(false));

        // Submit library request
        btnSubmitBookRequest.setOnClickListener(v -> submitBookRequest());
    }

    private void showDatePicker(boolean isBorrowDate) {

        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    Calendar selectedDate = Calendar.getInstance();

                    selectedDate.set(year, month, dayOfMonth);

                    String selectedDateString =
                            dateFormat.format(selectedDate.getTime());

                    if (isBorrowDate) {

                        dateBorrowed = selectedDateString;
                        btnBorrowDate.setText(dateBorrowed);

                    } else {

                        dateOfReturn = selectedDateString;
                        btnReturnDate.setText(dateOfReturn);
                    }

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.show();
    }

    private void submitBookRequest() {

        String isbn = etISBN.getText().toString().trim();
        String author = etAuthor.getText().toString().trim();
        String bookTitle = etBookTitle.getText().toString().trim();

        // Validate text fields
        if (isbn.isEmpty()) {
            etISBN.setError("Enter ISBN");
            etISBN.requestFocus();
            return;
        }

        if (author.isEmpty()) {
            etAuthor.setError("Enter author");
            etAuthor.requestFocus();
            return;
        }

        if (bookTitle.isEmpty()) {
            etBookTitle.setError("Enter book title");
            etBookTitle.requestFocus();
            return;
        }

        if (studentID.isEmpty()) {
            Toast.makeText(this,
                    "Student ID is missing",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate dates
        if (dateBorrowed.isEmpty()) {
            Toast.makeText(this,
                    "Select borrowing date",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (dateOfReturn.isEmpty()) {
            Toast.makeText(this,
                    "Select return date",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (studentRecordKey.isEmpty()) {
            Toast.makeText(this,
                    "Student record key is missing",
                    Toast.LENGTH_LONG).show();
            return;
        }

        try {

            Date borrowedDate = dateFormat.parse(dateBorrowed);
            Date returnDate = dateFormat.parse(dateOfReturn);

            if (borrowedDate == null || returnDate == null
                    || !returnDate.after(borrowedDate)) {

                Toast.makeText(this,
                        "Return date must be after borrowing date",
                        Toast.LENGTH_LONG).show();
                return;
            }

        } catch (ParseException e) {

            Toast.makeText(this,
                    "Invalid date selected",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Create LibraryRequest object
        LibraryRequest request = new LibraryRequest(
                isbn,
                author,
                bookTitle,
                dateBorrowed,
                dateOfReturn,
                studentID
        );

        // Prevent multiple submissions
        btnSubmitBookRequest.setEnabled(false);

        // Save library request to Firebase
        libraryReference.push().setValue(request)

                .addOnSuccessListener(unused -> {

                    Toast.makeText(LibraryActivity.this,
                            "Library request submitted successfully",
                            Toast.LENGTH_SHORT).show();

                    finish();

                })

                .addOnFailureListener(error -> {

                    Log.e("LIBRARY_FIREBASE",
                            "Library request failed",
                            error);

                    Toast.makeText(this,
                            "Failed to submit library request",
                            Toast.LENGTH_LONG).show();

                    btnSubmitBookRequest.setEnabled(true);
                });
    }
}
