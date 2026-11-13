
package com.example.studentregistration;

public class HostelBooking {

    public String location;
    public String roomType;
    public String shareable;
    public String studentID;

    // Required empty constructor for Firebase
    public HostelBooking() {
    }

    // Constructor for creating a hostel booking
    public HostelBooking(String location,
                         String roomType,
                         String shareable,
                         String studentID) {

        this.location = location;
        this.roomType = roomType;
        this.shareable = shareable;
        this.studentID = studentID;
    }
}
