
package com.example.studentregistration;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MpesaActivity extends AppCompatActivity {

    private EditText etMpesaStudentID;
    private EditText etMpesaPhone;
    private EditText etMpesaAmount;

    private Button btnInitiatePayment;
    private Button btnConfirmPayment;

    private TextView tvPaymentStatus;
    private TextView tvTransactionCode;

    private DatabaseReference transactionsReference;
    private DatabaseReference studentReference;

    private String studentID = "";
    private String studentRecordKey = "";
    private String transactionCode = "";

    private double paymentAmount = 0;
    private String phoneNumber = "";

    private boolean paymentInitiated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mpesa);

        // Connect XML components
        etMpesaStudentID = findViewById(R.id.etMpesaStudentID);
        etMpesaPhone = findViewById(R.id.etMpesaPhone);
        etMpesaAmount = findViewById(R.id.etMpesaAmount);

        btnInitiatePayment = findViewById(R.id.btnInitiatePayment);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvTransactionCode = findViewById(R.id.tvTransactionCode);

        // Receive student information
        studentID = getIntent().getStringExtra("studentID");
        studentRecordKey = getIntent().getStringExtra("studentRecordKey");

        if (studentID == null) {
            studentID = "";
        }

        if (studentRecordKey == null) {
            studentRecordKey = "";
        }

        // Display and protect student ID
        etMpesaStudentID.setText(studentID);
        etMpesaStudentID.setEnabled(false);

        // Initialize Firebase references
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://dekut-student-registrati-9d637-default-rtdb.firebaseio.com"
        );

        transactionsReference =
                database.getReference("mpesaTransactions");

        if (!studentRecordKey.isEmpty()) {
            studentReference = database.getReference("students")
                    .child(studentRecordKey);
        }

        // Initial payment interface
        btnConfirmPayment.setEnabled(false);

        tvPaymentStatus.setText("Payment not initiated");
        tvTransactionCode.setText("Transaction Code: Not available");

        // Initiate simulated payment
        btnInitiatePayment.setOnClickListener(v -> initiatePayment());

        // Confirm simulated payment
        btnConfirmPayment.setOnClickListener(v -> confirmPayment());
    }

    private void initiatePayment() {

        // Validate student information
        if (studentID.isEmpty()
                || studentRecordKey.isEmpty()
                || studentReference == null) {

            Toast.makeText(this,
                    "Student record not found. Please register again.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Validate phone number
        phoneNumber = etMpesaPhone.getText().toString().trim();

        if (!phoneNumber.matches("^(07|01)\\d{8}$")) {

            etMpesaPhone.setError(
                    "Enter a valid Kenyan phone number"
            );
            return;
        }

        // Validate payment amount
        String amountText = etMpesaAmount.getText().toString().trim();

        if (amountText.isEmpty()) {
            etMpesaAmount.setError("Enter payment amount");
            return;
        }

        try {
            paymentAmount = Double.parseDouble(amountText);
        } catch (NumberFormatException error) {
            etMpesaAmount.setError("Invalid payment amount");
            return;
        }

        if (paymentAmount <= 0) {
            etMpesaAmount.setError(
                    "Amount must be greater than zero"
            );
            return;
        }

        // Generate simulated transaction code
        transactionCode = "SIM" +
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase(Locale.ROOT);

        paymentInitiated = true;

        tvPaymentStatus.setText(
                "Payment Successful (Simulation)"
        );

        tvTransactionCode.setText(
                "Transaction Code: " + transactionCode
        );

        btnInitiatePayment.setEnabled(false);
        btnConfirmPayment.setEnabled(true);

        Toast.makeText(this,
                "Simulated payment successful. Confirm to complete registration.",
                Toast.LENGTH_LONG).show();
    }

    private void confirmPayment() {

        if (!paymentInitiated || transactionCode.isEmpty()) {
            Toast.makeText(this,
                    "Please initiate payment first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (studentReference == null) {
            Toast.makeText(this,
                    "Student record reference is missing.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        btnConfirmPayment.setEnabled(false);

        // Generate payment date and time
        String paymentDate = new SimpleDateFormat(
                "dd-MM-yyyy HH:mm:ss",
                Locale.getDefault()
        ).format(new Date());

        // Create payment object
        MpesaTransaction transaction = new MpesaTransaction(
                studentID,
                phoneNumber,
                paymentAmount,
                transactionCode,
                "Successful (Simulation)",
                paymentDate
        );

        // Save transaction using its transaction code
        transactionsReference.child(transactionCode)
                .setValue(transaction)
                .addOnSuccessListener(unused -> {

                    // Prepare updates for the original student record
                    Map<String, Object> updates = new HashMap<>();

                    updates.put("registrationStatus", "Confirmed");

                    updates.put("payment/amount", paymentAmount);
                    updates.put("payment/phoneNumber", phoneNumber);
                    updates.put("payment/transactionCode", transactionCode);
                    updates.put("payment/paymentStatus",
                            "Successful (Simulation)");
                    updates.put("payment/paymentDate", paymentDate);

                    // Update the original student record
                    studentReference.updateChildren(updates)
                            .addOnSuccessListener(updateUnused -> {

                                tvPaymentStatus.setText(
                                        "Registration Confirmed"
                                );

                                Toast.makeText(MpesaActivity.this,
                                        "Payment saved. Registration confirmed!",
                                        Toast.LENGTH_LONG).show();

                                // Return to dashboard
                                finish();
                            })
                            .addOnFailureListener(error -> {

                                Log.e("FIREBASE",
                                        "Student record update failed",
                                        error);

                                tvPaymentStatus.setText(
                                        "Payment saved, but registration confirmation failed."
                                );

                                Toast.makeText(MpesaActivity.this,
                                        "Payment saved, but student record update failed. Retry confirmation.",
                                        Toast.LENGTH_LONG).show();

                                btnConfirmPayment.setEnabled(true);
                            });
                })
                .addOnFailureListener(error -> {

                    Log.e("FIREBASE",
                            "Payment transaction save failed",
                            error);

                    tvPaymentStatus.setText(
                            "Payment transaction could not be saved."
                    );

                    Toast.makeText(MpesaActivity.this,
                            "Could not save payment. Please retry.",
                            Toast.LENGTH_LONG).show();

                    btnConfirmPayment.setEnabled(true);
                });
    }
}
