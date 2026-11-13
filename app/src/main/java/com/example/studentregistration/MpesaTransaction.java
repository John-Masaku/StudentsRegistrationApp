
package com.example.studentregistration;

public class MpesaTransaction {

    public String studentID;
    public String phoneNumber;
    public double amount;
    public String transactionCode;
    public String paymentStatus;
    public String paymentDate;

    // Required empty constructor for Firebase
    public MpesaTransaction() {
    }

    // Constructor for creating a payment transaction
    public MpesaTransaction(
            String studentID,
            String phoneNumber,
            double amount,
            String transactionCode,
            String paymentStatus,
            String paymentDate) {

        this.studentID = studentID;
        this.phoneNumber = phoneNumber;
        this.amount = amount;
        this.transactionCode = transactionCode;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
    }
}
