
package com.example.studentregistration;

public class LibraryRequest {


    public String isbn;
    public String author;
    public String bookTitle;
    public String dateBorrowed;
    public String dateOfReturn;
    public String studentID;

    // Required empty constructor for Firebase
    public LibraryRequest() {
    }

    // Constructor for creating a book request
    public LibraryRequest(
            String isbn,
            String author,
            String bookTitle,
            String dateBorrowed,
            String dateOfReturn,
            String studentID) {

        this.isbn = isbn;
        this.author = author;
        this.bookTitle = bookTitle;
        this.dateBorrowed = dateBorrowed;
        this.dateOfReturn = dateOfReturn;
        this.studentID = studentID;
    }
}
