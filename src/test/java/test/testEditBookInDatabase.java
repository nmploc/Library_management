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
        Books testBook = new Books("Test Book", "John Doe", "Fiction", 10);
        DatabaseHelper.addBookToDatabase(testBook);

        // Lấy documentID của sách vừa được thêm
        String query = "SELECT documentID FROM documents WHERE documentName = ?";
        int documentID = -1;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, testBook.getDocumentName());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    documentID = rs.getInt("documentID");
                } else {
                    fail("Không tìm thấy sách vừa thêm trong cơ sở dữ liệu");
                }
            }
        } catch (SQLException e) {
            fail("Lỗi SQL khi lấy documentID: " + e.getMessage());
        }

        // Đảm bảo documentID hợp lệ
        assertTrue(documentID > 0, "Document ID không hợp lệ");

        // Chỉnh sửa sách
        Books updatedBook = new Books(documentID, "Updated Book", "Jane Doe", "Fiction", 15);
        DatabaseHelper.updateBookInDatabase(updatedBook);

        // Kiểm tra sách sau khi chỉnh sửa
        int fictionCategoryID = -1;
        try (PreparedStatement pstmt = connection.prepareStatement("SELECT categoryID FROM categories WHERE categoryName = ?")) {
            pstmt.setString(1, "Fiction");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    fictionCategoryID = rs.getInt("categoryID");
                } else {
                    fail("Không tìm thấy thể loại 'Fiction' trong cơ sở dữ liệu");
                }
            }
        } catch (SQLException e) {
            fail("Lỗi SQL khi lấy categoryID: " + e.getMessage());
        }

        verifyBookInDatabase(updatedBook.getDocumentName(), updatedBook.getAuthors(), fictionCategoryID, updatedBook.getQuantity());

        // Xóa sách sau khi kiểm tra
        try (PreparedStatement pstmt = connection.prepareStatement("DELETE FROM documents WHERE documentID = ?")) {
            pstmt.setInt(1, documentID);
            int rowsDeleted = pstmt.executeUpdate();
            assertTrue(rowsDeleted > 0, "Không thể xóa sách sau khi kiểm tra");
        } catch (SQLException e) {
            fail("Lỗi SQL khi xóa sách: " + e.getMessage());
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
