package library;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class BooksController extends Controller {
    @FXML
    private TableView<Books> booksTable;

    @FXML
    private Button addBookButton, editBookButton, deleteBookButton;

    private ObservableList<Books> booksList;

    @FXML
    public void initialize() {
        // Khởi tạo bảng sách và tải dữ liệu
        DatabaseHelper.connectToDatabase(); // Kết nối cơ sở dữ liệu một lần
        loadBooksData();

        // Cấu hình cột cho bảng sách
        TableColumn<Books, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("documentID"));

        TableColumn<Books, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("documentName"));

        TableColumn<Books, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("authors"));

        booksTable.getColumns().addAll(idColumn, titleColumn, authorColumn);

        // Thiết lập trình xử lý cho các nút
        addBookButton.setOnAction(event -> handleAddBook());
        editBookButton.setOnAction(event -> handleEditBook());
        deleteBookButton.setOnAction(event -> handleDeleteBook());
    }

    private void loadBooksData() {
        booksList = FXCollections.observableArrayList();
        // Tải dữ liệu từ cơ sở dữ liệu
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM documents")) {

            while (rs.next()) {
                int id = rs.getInt("documentID");
                String name = rs.getString("documentName");
                String authors = rs.getString("authors");
                int categoryId = rs.getInt("categoryID");

                booksList.add(new Books(id, name, categoryId, authors));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        booksTable.setItems(booksList);
    }

    @FXML
    private void handleAddBook() {
        // Hiển thị dialog hoặc form để thêm sách
        System.out.println("Add Book functionality not implemented yet.");
    }

    @FXML
    private void handleEditBook() {
        // Hiển thị dialog hoặc form để chỉnh sửa sách
        System.out.println("Edit Book functionality not implemented yet.");
    }

    @FXML
    private void handleDeleteBook() {
        // Implement delete book functionality
        Books selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            booksList.remove(selectedBook);
            // Implement actual deletion from database
            System.out.println("Deleted book: " + selectedBook.getDocumentName());
        }
    }
}
