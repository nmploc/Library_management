package library;

public class Borrowing {
    private int borrowingID;
    private String readerName;
    private String documentName;
    private String borrowDate;
    private String dueDate;
    private String borrowingStatus;

    // Constructor
    public Borrowing(int borrowingID, String readerName, String documentName, String borrowDate, String dueDate, String borrowingStatus) {
        this.borrowingID = borrowingID;
        this.readerName = readerName;
        this.documentName = documentName;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.borrowingStatus = borrowingStatus;
    }

    // Getters and Setters
    public int getBorrowingID() {
        return borrowingID;
    }

    public void setBorrowingID(int borrowingID) {
        this.borrowingID = borrowingID;
    }

    public String getReaderName() {
        return readerName;
    }

    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(String borrowDate) {
        this.borrowDate = borrowDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getBorrowingStatus() {
        return borrowingStatus;
    }

    public void setBorrowingStatus(String borrowingStatus) {
        this.borrowingStatus = borrowingStatus;
    }
}
