package library;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.*;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://localhost:3306/librarydb";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static DatabaseHelper instance;
    private Connection connection;

    // Private constructor để ngăn chặn việc tạo thể hiện trực tiếp
    private DatabaseHelper() {
        openConnection();
    }

    // Phương thức để lấy thể hiện duy nhất của DatabaseHelper
    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    // Kiểm tra và mở kết nối mới nếu cần
    private void openConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    // Trả về kết nối hiện tại
    public Connection getConnection() {
        openConnection();  // Kiểm tra và mở lại kết nối nếu cần
        return connection;
    }

    // Start XAMPP services
    public static void startXamppServices() {
        String xamppPath = "D:\\App\\Xampp\\"; // Path to your XAMPP installation
        try {
            // Start Apache and MySQL services
            new ProcessBuilder(xamppPath + "apache_start.bat").start();
            System.out.println("Starting Apache server...");
            new ProcessBuilder(xamppPath + "mysql_start.bat").start();
            System.out.println("Starting MySQL server...");

            // Wait for a few seconds to ensure services are up
            Thread.sleep(200); // Adjust the delay as necessary
        } catch (IOException | InterruptedException e) {
            System.err.println("Error starting XAMPP services: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Stop XAMPP services
    public static void stopXamppServices() {
        String xamppPath = "D:\\App\\Xampp\\"; // Path to your XAMPP installation
        try {
            // Run the xampp_stop.exe file
            new ProcessBuilder(xamppPath + "xampp_stop.exe").start();
            System.out.println("Stopping XAMPP services...");
        } catch (IOException e) {
            System.err.println("Error stopping XAMPP services: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addBookToDatabase(Books newBook) {
        openConnection();
        String insertQuery = "INSERT INTO documents (documentName, authors, categoryID, quantity) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setString(1, newBook.getDocumentName());
            pstmt.setString(2, newBook.getAuthors());
            pstmt.setInt(3, getCategoryIdByName(newBook.getCategory()));
            pstmt.setInt(4, newBook.getQuantity());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateBookInDatabase(Books updatedBook) {
        openConnection();
        String updateQuery = "UPDATE documents SET documentName = ?, authors = ?, categoryID = ?, quantity = ? WHERE documentID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
            pstmt.setString(1, updatedBook.getDocumentName());
            pstmt.setString(2, updatedBook.getAuthors());
            pstmt.setInt(3, getCategoryIdByName(updatedBook.getCategory()));
            pstmt.setInt(4, updatedBook.getQuantity());
            pstmt.setInt(5, updatedBook.getDocumentID());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteBookFromDatabase(int documentID) {
        openConnection();
        String deleteQuery = "DELETE FROM documents WHERE documentID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, documentID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ObservableList<Books> searchBooksInDatabase(String searchQuery) {
        openConnection();
        ObservableList<Books> searchResults = FXCollections.observableArrayList();
        String query = "SELECT d.documentID, d.documentName, d.authors, c.categoryName, d.quantity, d.isbn " +
                "FROM documents d " +
                "LEFT JOIN categories c ON d.categoryID = c.categoryID " +
                "WHERE d.documentName LIKE ? OR d.authors LIKE ? OR c.categoryName LIKE ? OR d.isbn LIKE ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, "%" + searchQuery + "%");
            pstmt.setString(2, "%" + searchQuery + "%");
            pstmt.setString(3, "%" + searchQuery + "%");
            pstmt.setString(4, "%" + searchQuery + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Books book = new Books(
                        rs.getInt("documentID"),
                        rs.getString("documentName"),
                        rs.getString("authors"),
                        rs.getString("categoryName"),
                        rs.getInt("quantity"),
                        rs.getString("isbn")
                );
                searchResults.add(book);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return searchResults;
    }

    public int getCategoryIdByName(String categoryName) {
        openConnection();
        String query = "SELECT categoryID FROM categories WHERE categoryName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, categoryName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("categoryID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Nếu không tìm thấy category
    }

    public boolean isCategoryExists(String categoryName) {
        openConnection(); // Kiểm tra và mở lại kết nối nếu cần thiết
        String query = "SELECT COUNT(*) FROM categories WHERE categoryName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, categoryName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addCategoryToDatabase(String categoryName) {
        openConnection();
        String insertQuery = "INSERT INTO categories (categoryName) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setString(1, categoryName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addReaderToDatabase(Reader newReader) {
        openConnection();
        String insertQuery = "INSERT INTO readers (readerName, fullName, email, phoneNumber) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertQuery)) {
            pstmt.setString(1, newReader.getReaderName());
            pstmt.setString(2, newReader.getFullName());
            pstmt.setString(3, newReader.getEmail());
            pstmt.setString(4, newReader.getPhoneNumber());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Kiểm tra xem thông tin reader đã tồn tại trong cơ sở dữ liệu chưa
    public boolean isReaderExist(Reader reader) {
        openConnection(); // Kiểm tra và mở lại kết nối nếu cần
        String checkQuery = "SELECT COUNT(*) FROM readers WHERE (readerName = ? OR email = ? OR phoneNumber = ?) AND readerID != ?";

        try (PreparedStatement pstmt = connection.prepareStatement(checkQuery)) {
            pstmt.setString(1, reader.getReaderName());
            pstmt.setString(2, reader.getEmail());
            pstmt.setString(3, reader.getPhoneNumber());
            pstmt.setInt(4, reader.getReaderID()); // Kiểm tra tất cả các reader khác, ngoại trừ reader hiện tại

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;  // Nếu có ít nhất 1 dòng dữ liệu trùng thì trả về true
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false; // Không có dữ liệu trùng
    }


    public void deleteReaderFromDatabase(int readerID) {
        openConnection();
        String deleteQuery = "DELETE FROM readers WHERE readerID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, readerID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateReaderInDatabase(Reader updatedReader) {
        openConnection();
        String updateQuery = "UPDATE readers SET readerName = ?, fullName = ?, email = ?, phoneNumber = ? WHERE readerID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
            pstmt.setString(1, updatedReader.getReaderName());
            pstmt.setString(2, updatedReader.getFullName());
            pstmt.setString(3, updatedReader.getEmail());
            pstmt.setString(4, updatedReader.getPhoneNumber());
            pstmt.setInt(5, updatedReader.getReaderID());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // New method to check if a book is currently on loan
    public boolean isBookOnLoan(int documentID) {
        openConnection(); // Kiểm tra và mở lại kết nối nếu cần
        String query = "SELECT COUNT(*) FROM borrowings WHERE documentID = ? AND borrowingStatus IN ('borrowing', 'late', 'lost')";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, documentID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("SQL error in isBookOnLoan: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

}
