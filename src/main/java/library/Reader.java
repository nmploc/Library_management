//package library;
//
//import java.util.Scanner;
//
//public class Reader
//{
//    private static final Scanner sc = new Scanner(System.in);
//
//    public static int readInt()
//    {
//        return sc.nextInt();
//    }
//
//    public static String readLine()
//    {
//        return sc.nextLine().trim();
//    }
//}
package library;

public class Reader {
    private int readerID;
    private String readerName;
    private String email;
    private String phoneNumber;

    public Reader(int readerID, String readerName, String email, String phoneNumber) {
        this.readerID = readerID;
        this.readerName = readerName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // Getters
    public int getReaderID() {
        return readerID;
    }

    public String getReaderName() {
        return readerName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    // Setters
    public void setReaderName(String readerName) {
        this.readerName = readerName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
