package library;

public class Reader {
    private int readerID;
    private String readerName;
    private String fullName;  // New field for full name
    private String email;
    private String phoneNumber;

    public Reader(int readerID, String readerName, String fullName, String email, String phoneNumber) {
        this.readerID = readerID;
        this.readerName = readerName;
        this.fullName = fullName;  // Set full name
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Getters and setters
    public int getReaderID() {
        return readerID;
    }

    public void setReaderID(int readerID) {
        this.readerID = readerID;
    }

    public String getReaderName() {
        return readerName;
    }

    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
