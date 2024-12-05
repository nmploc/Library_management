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
        connection = DatabaseHelper.connection; // Lấy kết nối từ DatabaseHelper

        // Tạo bảng `categories` và `documents` nếu chưa có
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (categoryID INT PRIMARY KEY AUTO_INCREMENT, categoryName VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS documents (documentID INT PRIMARY KEY AUTO_INCREMENT, documentName VARCHAR(255), authors VARCHAR(255), categoryID INT, quantity INT)");
            stmt.execute("INSERT IGNORE INTO categories (categoryName) VALUES ('Fiction'), ('Non-Fiction')");
        }
    }

    @Test
    public void testDeleteBookFromDatabase() {
        // Thêm sách vào cơ sở dữ liệu trước khi xóa
        Books testBook = new Books("Updated Book", "Jane Doe", "Fiction", 15);

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

        // Xóa sách
        DatabaseHelper.deleteBookFromDatabase(documentID);

        // Kiểm tra lại sách đã bị xóa chưa
        query = "SELECT * FROM documents WHERE documentID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, documentID);
            ResultSet rs = pstmt.executeQuery();
            assertFalse(rs.next(), "Book with ID " + documentID + " was not deleted.");
        } catch (SQLException e) {
            e.printStackTrace();
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
