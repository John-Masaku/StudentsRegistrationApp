
package com.example.studentregistration;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class StatusActivity extends AppCompatActivity {

    private TextView tvStatusStudentID;
    private TextView tvRegistrationStatus;
    private TextView tvLibraryStatus;
    private TextView tvHostelStatus;
    private TextView tvPaymentStatus;

    private Button btnRefreshStatus;

    private String studentID = "";
    private String studentRecordKey = "";

    private DatabaseReference studentReference;
    private DatabaseReference libraryReference;
    private DatabaseReference hostelReference;
    private DatabaseReference paymentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        // Connect XML components
        tvStatusStudentID = findViewById(R.id.tvStatusStudentID);
        tvRegistrationStatus = findViewById(R.id.tvRegistrationStatus);
        tvLibraryStatus = findViewById(R.id.tvLibraryStatus);
        tvHostelStatus = findViewById(R.id.tvHostelStatus);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);

        btnRefreshStatus = findViewById(R.id.btnRefreshStatus);

        // Receive student information
        studentID = getIntent().getStringExtra("studentID");
        studentRecordKey = getIntent().getStringExtra("studentRecordKey");

        if (studentID == null) {
            studentID = "";
        }

        if (studentRecordKey == null) {
            studentRecordKey = "";
        }

        tvStatusStudentID.setText("Student ID: " + studentID);

        // Initialize Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://dekut-student-registrati-9d637-default-rtdb.firebaseio.com"
        );

        studentReference = database.getReference("students")
                .child(studentRecordKey);

        libraryReference = database.getReference("libraryRequests");
        hostelReference = database.getReference("hostelBookings");
        paymentReference = database.getReference("mpesaTransactions");

        // Initial loading
        loadAllStatuses();

        // Refresh button
        btnRefreshStatus.setOnClickListener(v -> loadAllStatuses());
    }

    private void loadAllStatuses() {

        if (studentID.isEmpty() || studentRecordKey.isEmpty()) {

            Toast.makeText(this,
                    "Student information is missing.",
                    Toast.LENGTH_LONG).show();

            return;
        }

        tvRegistrationStatus.setText("Loading...");
        tvLibraryStatus.setText("Loading...");
        tvHostelStatus.setText("Loading...");
        tvPaymentStatus.setText("Loading...");

        loadRegistrationStatus();
        loadLibraryStatus();
        loadHostelStatus();
        loadPaymentStatus();
    }

    // Retrieve original student registration
    private void loadRegistrationStatus() {

        studentReference.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            String status = snapshot.child("registrationStatus")
                                    .getValue(String.class);

                            if (status == null) {
                                status = "Pending";
                            }

                            tvRegistrationStatus.setText(status);

                        } else {

                            tvRegistrationStatus.setText(
                                    "Student record not found"
                            );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        tvRegistrationStatus.setText(
                                "Unable to retrieve registration status"
                        );

                        LogError("Registration", error);
                    }
                }
        );
    }

    // Check whether a library request exists
    private void loadLibraryStatus() {

        Query query = libraryReference.orderByChild("studentID")
                .equalTo(studentID);

        query.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            StringBuilder result = new StringBuilder();

                            result.append("Submitted");

                            for (DataSnapshot request : snapshot.getChildren()) {

                                String title = request.child("bookTitle")
                                        .getValue(String.class);

                                if (title != null) {
                                    result.append("\nBook: ").append(title);
                                }
                            }

                            tvLibraryStatus.setText(result.toString());

                        } else {

                            tvLibraryStatus.setText("Not requested");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        tvLibraryStatus.setText(
                                "Unable to retrieve library status"
                        );

                        LogError("Library", error);
                    }
                }
        );
    }

    // Check whether a hostel booking exists
    private void loadHostelStatus() {

        Query query = hostelReference.orderByChild("studentID")
                .equalTo(studentID);

        query.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            StringBuilder result = new StringBuilder();

                            result.append("Submitted");

                            for (DataSnapshot booking : snapshot.getChildren()) {

                                String location = booking.child("location")
                                        .getValue(String.class);

                                String roomType = booking.child("roomType")
                                        .getValue(String.class);

                                if (location != null) {
                                    result.append("\nLocation: ")
                                            .append(location);
                                }

                                if (roomType != null) {
                                    result.append("\nRoom: ")
                                            .append(roomType);
                                }
                            }

                            tvHostelStatus.setText(result.toString());

                        } else {

                            tvHostelStatus.setText("Not booked");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        tvHostelStatus.setText(
                                "Unable to retrieve hostel status"
                        );

                        LogError("Hostel", error);
                    }
                }
        );
    }

    // Check M-PESA transaction records
    private void loadPaymentStatus() {

        Query query = paymentReference.orderByChild("studentID")
                .equalTo(studentID);

        query.addListenerForSingleValueEvent(
                new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            for (DataSnapshot transaction :
                                    snapshot.getChildren()) {

                                String status = transaction.child("paymentStatus")
                                        .getValue(String.class);

                                String code = transaction.child("transactionCode")
                                        .getValue(String.class);

                                Double amount = transaction.child("amount")
                                        .getValue(Double.class);

                                StringBuilder result = new StringBuilder();

                                result.append(status != null
                                        ? status : "Transaction found");

                                if (code != null) {
                                    result.append("\nTransaction: ")
                                            .append(code);
                                }

                                if (amount != null) {
                                    result.append("\nAmount: KSh ")
                                            .append(amount);
                                }

                                tvPaymentStatus.setText(result.toString());
                            }

                        } else {

                            tvPaymentStatus.setText("Payment not made");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        tvPaymentStatus.setText(
                                "Unable to retrieve payment status"
                        );

                        LogError("Payment", error);
                    }
                }
        );
    }

    private void LogError(String section, DatabaseError error) {

        android.util.Log.e(
                "STATUS_ACTIVITY",
                section + " retrieval failed: " + error.getMessage()
        );
    }
}
