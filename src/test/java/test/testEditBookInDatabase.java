package test;

import library.Books;
import library.DatabaseHelper;
import org.junit.jupiter.api.*;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

public class testEditBookInDatabase {

    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        // Kết nối đến cơ sở dữ liệu thực của bạn thông qua DatabaseHelper
        DatabaseHelper.connectToDatabase();  // Gọi phương thức kết nối từ DatabaseHelper
        connection = DatabaseHelper.getConnection(); // Lấy kết nối từ DatabaseHelper

        // Tạo bảng `categories` và `documents` nếu chưa có
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (categoryID INT PRIMARY KEY AUTO_INCREMENT, categoryName VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS documents (documentID INT PRIMARY KEY AUTO_INCREMENT, documentName VARCHAR(255), authors VARCHAR(255), categoryID INT, quantity INT)");
            stmt.execute("INSERT IGNORE INTO categories (categoryName) VALUES ('Fiction'), ('Non-Fiction')");
        }
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
    public void testEditBookInDatabase() {
        // Thêm sách vào cơ sở dữ liệu trước khi chỉnh sửa
        Books testBook = new Books("Test Book", "John Doe", "Poetry", 10);

        // Lấy documentID của sách đã thêm vào
        String query = "SELECT documentID FROM documents WHERE documentName = ?";
        int documentID = -1;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, testBook.getDocumentName());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                documentID = rs.getInt("documentID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Chỉnh sửa sách
        Books updatedBook = new Books(documentID, "Updated Book", "Jane Doe", "Poetry", 15);
        DatabaseHelper.updateBookInDatabase(updatedBook);

        // Kiểm tra lại sách sau khi chỉnh sửa
        verifyBookInDatabase(updatedBook.getDocumentName(), updatedBook.getAuthors(), 2, updatedBook.getQuantity()); // categoryID cho 'Non-Fiction' là 2
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Đóng kết nối sau mỗi bài kiểm tra
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
