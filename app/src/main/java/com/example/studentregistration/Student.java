package com.example.studentregistration;
public class Student {

    public String fullName;
    public String idNumber;
    public String registrationNumber;
    public String gender;
    public String course;
    public String department;

    public Student() {
        // Required empty constructor for Firebase
    }

    public Student(String fullName, String idNumber,
                   String registrationNumber, String gender,
                   String course, String department) {

        this.fullName = fullName;
        this.idNumber = idNumber;
        this.registrationNumber = registrationNumber;
        this.gender = gender;
        this.course = course;
        this.department = department;
    }
}