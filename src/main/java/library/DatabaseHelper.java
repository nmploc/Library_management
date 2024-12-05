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
        private static Connection connection;

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

        // Connection Database
        public static void connectToDatabase() {
            try {
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Connect to database successfully");
            } catch (SQLException e) {
                System.err.println("Failed to connect to database " + e.getMessage());
            }
        }

        public static Connection getConnection() throws SQLException {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }

        public static void addBookToDatabase(Books newBook) {
            String insertQuery = "INSERT INTO documents (documentName, authors, categoryID, quantity) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

                pstmt.setString(1, newBook.getDocumentName());
                pstmt.setString(2, newBook.getAuthors());
                pstmt.setInt(3, DatabaseHelper.getCategoryIdByName(newBook.getCategory()));
                pstmt.setInt(4, newBook.getQuantity());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public static void updateBookInDatabase(Books updatedBook) {
            String updateQuery = "UPDATE documents SET documentName = ?, authors = ?, categoryID = ?, quantity = ? WHERE documentID = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

                pstmt.setString(1, updatedBook.getDocumentName());
                pstmt.setString(2, updatedBook.getAuthors());
                pstmt.setInt(3, DatabaseHelper.getCategoryIdByName(updatedBook.getCategory()));
                pstmt.setInt(4, updatedBook.getQuantity());
                pstmt.setInt(5, updatedBook.getDocumentID());
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public static void deleteBookFromDatabase(int documentID) {
            String deleteQuery = "DELETE FROM documents WHERE documentID = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {

                pstmt.setInt(1, documentID);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public static ObservableList<Books> searchBooksInDatabase(String searchQuery) {
            ObservableList<Books> searchResults = FXCollections.observableArrayList();
            String query = "SELECT d.documentID, d.documentName, d.authors, c.categoryName, d.quantity, d.isbn " +
                    "FROM documents d " +
                    "LEFT JOIN categories c ON d.categoryID = c.categoryID " +
                    "WHERE d.documentName LIKE ? OR d.authors LIKE ? OR c.categoryName LIKE ? OR d.isbn LIKE ?";

            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                // Use wildcard search to find partial matches
                pstmt.setString(1, "%" + searchQuery + "%");
                pstmt.setString(2, "%" + searchQuery + "%");
                pstmt.setString(3, "%" + searchQuery + "%");
                pstmt.setString(4, "%" + searchQuery + "%"); // Add for ISBN search
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

        public static int getCategoryIdByName(String categoryName) {
            String query = "SELECT categoryID FROM categories WHERE categoryName = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setString(1, categoryName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("categoryID");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return -1; // Return -1 if category is not found
        }

        public static boolean isCategoryExists(String categoryName) {
            String query = "SELECT COUNT(*) FROM categories WHERE categoryName = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

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

        public static void addCategoryToDatabase(String categoryName) {
            String insertQuery = "INSERT INTO categories (categoryName) VALUES (?)";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

                pstmt.setString(1, categoryName);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public static void addReaderToDatabase(Reader newReader) {
            String insertQuery = "INSERT INTO readers (readerName, fullName, email, phoneNumber) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {

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
        public static boolean isReaderExist(Reader reader) {
            String checkQuery = "SELECT COUNT(*) FROM readers WHERE (readerName = ? OR email = ? OR phoneNumber = ?) AND readerID != ?";

            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(checkQuery)) {

                pstmt.setString(1, reader.getReaderName());
                pstmt.setString(2, reader.getEmail());
                pstmt.setString(3, reader.getPhoneNumber());
                pstmt.setInt(4, reader.getReaderID());  // Kiểm tra tất cả các reader khác, ngoại trừ reader hiện tại

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

        public static void deleteReaderFromDatabase(int readerID) {
            String deleteQuery = "DELETE FROM readers WHERE readerID = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
                pstmt.setInt(1, readerID);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public static void updateReaderInDatabase(Reader updatedReader) {
            String updateQuery = "UPDATE readers SET readerName = ?, fullName = ?, email = ?, phoneNumber = ? WHERE readerID = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

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
        public static boolean isBookOnLoan(int documentID) {
            String query = "SELECT COUNT(*) FROM borrowings WHERE documentID = ? AND borrowingStatus = 'borrowing'";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

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
