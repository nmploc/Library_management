package library;

import java.time.LocalDate;

public class BorrowReturn {
    private LocalDate borrowDate;
    private int bookId;
    private int readerId;

    // Constructor
    public BorrowReturn(LocalDate borrowDate, int bookId, int readerId) {
        this.borrowDate = borrowDate;
        this.bookId = bookId;
        this.readerId = readerId;
    }

    // Getters
    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public int getBookId() {
        return bookId;
    }

    public int getReaderId() {
        return readerId;
    }

    // Setters
    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public void setReaderId(int readerId) {
        this.readerId = readerId;
    }
}
