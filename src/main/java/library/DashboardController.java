package library;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DashboardController extends Controller
{
    @FXML
    public VBox VBoxTotalBooks;

    @FXML
    public VBox VBoxTotalUsers;

    @FXML
    public Label labelTotalBooks;

    @FXML
    public Label labelTotalUsers;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        DatabaseHelper.connectToDatabase();
        try (Connection connection=DatabaseHelper.getConnection())
        {
            String bookQuery = "select count(*) from documents";
            String userQuery = "select count(*) from users";

            //Truy vấn lượng sách
            PreparedStatement statement = connection.prepareStatement(bookQuery);
            ResultSet res = statement.executeQuery();

            if(res.next())
            {
                int numOfBooks = res.getInt("count(*)");
                labelTotalBooks.setText("Total books: " + numOfBooks);
            }

            //Truy vấn số người dùng
            statement = connection.prepareStatement(userQuery);
            res = statement.executeQuery();
            if(res.next())
            {
                int numOfUers = res.getInt("count(*)");
                labelTotalUsers.setText("Total users: " + numOfUers);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();;
        }
    }
}
