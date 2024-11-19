package library;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class BookDetailController {

    @FXML
    private Label titleLabel, authorLabel, categoryLabel, quantityLabel;
    @FXML
    private ImageView thumbnailImageView;

    private Books book;

    public void initialize(Books book) {
        this.book = book;
        // Set data on the UI components
        titleLabel.setText(book.getDocumentName());
        authorLabel.setText("Author: " + book.getAuthors());
        categoryLabel.setText("Category: " + book.getCategory());
        quantityLabel.setText("Quantity: " + book.getQuantity());

        // Load thumbnail image (if available)
        String imageUrl = book.getCoverImageUrl(); // Ensure the Books class has this method
        if (imageUrl != null && !imageUrl.isEmpty()) {
            thumbnailImageView.setImage(new Image(imageUrl));
        } else {
            thumbnailImageView.setImage(new Image("default_image.png"));
        }
    }
}
