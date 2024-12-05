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
        // Kết nối đến cơ sở dữ liệu thực của bạn thông qua DatabaseHelper
        DatabaseHelper.connectToDatabase();  // Gọi phương thức kết nối từ DatabaseHelper
        connection = DatabaseHelper.getConnection()
        ; // Lấy kết nối từ DatabaseHelper

        // Tạo bảng `categories` và `documents` nếu chưa có
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (categoryID INT PRIMARY KEY AUTO_INCREMENT, categoryName VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS documents (documentID INT PRIMARY KEY AUTO_INCREMENT, documentName VARCHAR(255), authors VARCHAR(255), categoryID INT, quantity INT)");
            stmt.execute("INSERT IGNORE INTO categories (categoryName) VALUES ('Fiction'), ('Non-Fiction')");
        }
    }

    @Test
    public void testDeleteBookFromDatabase() {
        // Step 1: Add a book to the database
        Books testBook = new Books("Test Book", "John Doe", "Fiction", 10);
        DatabaseHelper.addBookToDatabase(testBook);

        // Step 2: Retrieve the documentID of the added book
        String query = "SELECT documentID FROM documents WHERE documentName = ?";
        int documentID = -1;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, testBook.getDocumentName());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    documentID = rs.getInt("documentID");
                } else {
                    fail("The book was not found in the database after being added.");
                }
            }
        } catch (SQLException e) {
            fail("SQL error occurred while retrieving the documentID: " + e.getMessage());
        }

        // Ensure that the documentID is valid
        assertTrue(documentID > 0, "Invalid document ID retrieved.");

        // Step 3: Delete the book using the retrieved documentID
        DatabaseHelper.deleteBookFromDatabase(documentID);

        // Step 4: Verify the book has been deleted from the database
        query = "SELECT * FROM documents WHERE documentID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, documentID);
            try (ResultSet rs = pstmt.executeQuery()) {
                assertFalse(rs.next(), "The book with ID " + documentID + " was not deleted from the database.");
            }
        } catch (SQLException e) {
            fail("SQL error occurred while verifying the book deletion: " + e.getMessage());
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
