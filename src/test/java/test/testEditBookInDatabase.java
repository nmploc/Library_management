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
        // Lấy đối tượng duy nhất của DatabaseHelper thông qua Singleton
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        connection = databaseHelper.getConnection();  // Lấy kết nối từ DatabaseHelper

        // Tạo bảng `categories` và `documents` nếu chưa có
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS categories (categoryID INT PRIMARY KEY AUTO_INCREMENT, categoryName VARCHAR(255))");
            stmt.execute("CREATE TABLE IF NOT EXISTS documents (documentID INT PRIMARY KEY AUTO_INCREMENT, documentName VARCHAR(255), authors VARCHAR(255), categoryID INT, quantity INT)");
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
        // Step 1: Add a new book to the database
        Books testBook = new Books("Test Book", "John Doe", "Fiction", 10);
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();  // Lấy đối tượng DatabaseHelper
        databaseHelper.addBookToDatabase(testBook);

        // Step 2: Retrieve the documentID of the added book
        String query = "SELECT documentID FROM documents WHERE documentName = ?";
        int documentID = -1;
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, testBook.getDocumentName());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    documentID = rs.getInt("documentID");
                } else {
                    fail("Book was not found in the database after adding.");
                }
            }
        } catch (SQLException e) {
            fail("SQL error occurred while retrieving documentID: " + e.getMessage());
        }

        // Ensure a valid documentID is retrieved
        assertTrue(documentID > 0, "Invalid document ID retrieved.");

        // Step 3: Edit the book
        Books updatedBook = new Books(documentID, "Updated Book", "Jane Doe", "Fiction", 15);
        databaseHelper.updateBookInDatabase(updatedBook);  // Sử dụng phương thức từ đối tượng duy nhất của DatabaseHelper

        // Step 4: Verify the book after editing
        verifyBookInDatabase(updatedBook.getDocumentName(), updatedBook.getAuthors(), 1, updatedBook.getQuantity());

        // Step 5: Delete the book
        databaseHelper.deleteBookFromDatabase(documentID);  // Sử dụng phương thức từ đối tượng duy nhất của DatabaseHelper

        // Step 6: Verify the book has been deleted
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
