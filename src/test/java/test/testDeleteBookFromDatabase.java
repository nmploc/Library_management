package test;

import library.Books;
import library.DatabaseHelper;
import org.junit.jupiter.api.*;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class testDeleteBookFromDatabase {

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        // Lấy đối tượng duy nhất của DatabaseHelper thông qua Singleton
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        connection = databaseHelper.getConnection();  // Lấy kết nối từ DatabaseHelper

        // Tạo bảng `categories` và `documents` nếu chưa có
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (categoryID INT PRIMARY KEY AUTO_INCREMENT, categoryName VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS documents (documentID INT PRIMARY KEY AUTO_INCREMENT, documentName VARCHAR(255), authors VARCHAR(255), categoryID INT, quantity INT)");
        }
    }

    @Test
    public void testDeleteBookFromDatabase() {
        // Step 1: Thêm sách mới
        Books testBook = new Books("Temporary Book", "Author Name", "Fiction", 10);
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();  // Lấy đối tượng DatabaseHelper
        databaseHelper.addBookToDatabase(testBook);

        // Step 2: Lấy `documentID` của sách vừa thêm
        String query = "SELECT documentID FROM documents WHERE documentName = ?";
        int documentID = -1;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, testBook.getDocumentName());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    documentID = rs.getInt("documentID");
                } else {
                    fail("Book was not added successfully to the database.");
                }
            }
        } catch (SQLException e) {
            fail("SQL error occurred while retrieving documentID: " + e.getMessage());
        }

        // Step 3: Xóa sách vừa thêm
        databaseHelper.deleteBookFromDatabase(documentID);  // Sử dụng phương thức từ đối tượng duy nhất của DatabaseHelper

        // Step 4: Kiểm tra sách đã bị xóa
        query = "SELECT * FROM documents WHERE documentID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, documentID);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertFalse(rs.next(), "Book with ID " + documentID + " was not deleted from the database.");
            }
        } catch (SQLException e) {
            fail("SQL error occurred while verifying book deletion: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Đóng kết nối sau mỗi bài kiểm tra
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
