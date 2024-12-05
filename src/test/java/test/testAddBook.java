package test;

import library.Books;
import library.DatabaseHelper;
import org.junit.jupiter.api.*;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class testAddBook {

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        // Kết nối đến cơ sở dữ liệu thực của bạn thông qua DatabaseHelper
        DatabaseHelper.connectToDatabase(); // Gọi phương thức kết nối từ DatabaseHelper
        connection = DatabaseHelper.getConnection(); // Lấy kết nối từ DatabaseHelper

        // Tạo bảng `categories` và `documents` nếu chưa có
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (categoryID INT PRIMARY KEY AUTO_INCREMENT, categoryName VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS documents (documentID INT PRIMARY KEY AUTO_INCREMENT, documentName VARCHAR(255), authors VARCHAR(255), categoryID INT, quantity INT)");
            stmt.execute("INSERT IGNORE INTO categories (categoryName) VALUES ('Fiction'), ('Non-Fiction')");
        }

        // Đặt chế độ không tự động commit để rollback sau khi test
        connection.setAutoCommit(false);
    }

    public void verifyBookInDatabase(String expectedName, String expectedAuthor, int expectedCategoryID, int expectedQuantity) {
        String query = "SELECT * FROM documents WHERE documentName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, expectedName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Kiểm tra từng cột một cách rõ ràng
                    assertEquals(expectedName, rs.getString("documentName"), "Document name does not match");
                    assertEquals(expectedAuthor, rs.getString("authors"), "Author does not match");
                    assertEquals(expectedCategoryID, rs.getInt("categoryID"), "Category ID does not match");
                    assertEquals(expectedQuantity, rs.getInt("quantity"), "Quantity does not match");
                } else {
                    fail("Book with name '" + expectedName + "' does not exist in the database");
                }
            }
        } catch (SQLException e) {
            fail("SQL Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void testAddBookToDatabase() {
        // Khởi tạo sách để kiểm tra
        Books testBook = new Books("Test Book", "John Doe", "Fiction", 10);

        // Gọi hàm thêm sách
        DatabaseHelper.addBookToDatabase(testBook);

        // Lấy documentID từ cơ sở dữ liệu sau khi thêm
        String query = "SELECT documentID FROM documents WHERE documentName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, testBook.getDocumentName());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    testBook.setDocumentID(rs.getInt("documentID")); // Gán documentID cho đối tượng
                } else {
                    fail("Document ID was not generated for the book");
                }
            }
        } catch (SQLException e) {
            fail("SQL Exception occurred while fetching documentID: " + e.getMessage());
        }

        // Kiểm tra sách trong cơ sở dữ liệu
        verifyBookInDatabase(testBook.getDocumentName(), testBook.getAuthors(), 1, testBook.getQuantity());

        // Xóa sách sau khi kiểm tra
        DatabaseHelper.deleteBookFromDatabase(testBook.getDocumentID());
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Rollback mọi thay đổi trong cơ sở dữ liệu
        if (connection != null) {
            connection.rollback();
            connection.close();
        }
    }
}
