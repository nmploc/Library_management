package library;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class StageController extends Controller {

    @FXML
    public static void openBookDetail(Books selectedBook) {
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to view details.");
            return;
        }

        // Create a new Stage (window) for showing book details
        Stage detailWindow = new Stage();
        detailWindow.setTitle("Book Details");

        // Register the stage with Main to track it
        Main.registerStage(detailWindow);

        // Main HBox layout for the content
        HBox hbox = new HBox(30);
        hbox.setStyle("-fx-padding: 20px; -fx-alignment: center;");

        // Left section: Book details and QR code
        VBox leftVBox = new VBox(10);
        leftVBox.setStyle("-fx-alignment: top-left;");

        // Consolidate book details into one TextArea with shadow and border styling
        TextArea bookDetailsArea = new TextArea();
        bookDetailsArea.setText(String.format(
                "Title: %s\nAuthor: %s\nCategory: %s\nISBN: %s",
                selectedBook.getDocumentName(),
                selectedBook.getAuthors(),
                selectedBook.getCategory(),
                selectedBook.getIsbn()
        ));
        bookDetailsArea.setWrapText(true);
        bookDetailsArea.setEditable(false);
        bookDetailsArea.setPrefHeight(120);
        bookDetailsArea.setPrefWidth(250);
        bookDetailsArea.getStyleClass().add("content-area");

        leftVBox.getChildren().add(bookDetailsArea);

        // QR code section
        VBox qrVBox = new VBox(10);
        try {
            String tempQRCodePath = "temp_qr_code.png";
            QRCodeGenerator.generateQRCode(selectedBook, tempQRCodePath);

            ImageView qrCodeView = new ImageView(new Image("file:" + tempQRCodePath));
            qrCodeView.setFitHeight(160);
            qrCodeView.setFitWidth(160);
            qrCodeView.setPreserveRatio(true);

            qrVBox.getChildren().add(new Label("QR Code:"));
            qrVBox.getChildren().add(qrCodeView);
        } catch (Exception e) {
            qrVBox.getChildren().add(new Label("QR Code unavailable."));
            showAlert("QR Code Error", "Failed to generate QR code: " + e.getMessage());
        }
        leftVBox.getChildren().add(qrVBox);

        // Middle section: Description
        TextArea descriptionArea = new TextArea();
        descriptionArea.setText(selectedBook.getDescription() != null ? selectedBook.getDescription() : "No description available");
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.setPrefHeight(260);
        descriptionArea.setPrefWidth(260);
        descriptionArea.getStyleClass().add("content-area");

        VBox descriptionVBox = new VBox(10);
        descriptionVBox.getChildren().addAll(new Label("Description:"), descriptionArea);

        // Right section: Cover image
        VBox rightVBox = new VBox(10);
        rightVBox.setStyle("-fx-padding: 28px;-fx-alignment: top-right;");

        if (selectedBook.getCoverImageUrl() != null && !selectedBook.getCoverImageUrl().isEmpty()) {
            try {
                ImageView coverImageView = new ImageView(new Image(selectedBook.getCoverImageUrl()));
                coverImageView.setFitHeight(260);
                coverImageView.setFitWidth(260);
                coverImageView.setPreserveRatio(true);
                rightVBox.getChildren().add(coverImageView);
            } catch (Exception e) {
                rightVBox.getChildren().add(new Label("Cover image unavailable."));
            }
        } else {
            rightVBox.getChildren().add(new Label("No cover image available."));
        }

        // Add all sections to the HBox
        hbox.getChildren().addAll(leftVBox, descriptionVBox, rightVBox);

        // Main layout: VBox to include only the content
        VBox mainLayout = new VBox();
        mainLayout.getChildren().add(hbox);

        // Create a scene with the VBox as the root node
        Scene detailScene = new Scene(mainLayout, 820, 400);
        detailScene.getStylesheets().add(APIHelper.class.getResource("/CSS/Books.css").toExternalForm());
        detailWindow.setScene(detailScene);

        // Show the window
        detailWindow.show();
    }

    @FXML
    public static void openBookDetailWithoutCover(Books selectedBook) {
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to view details.");
            return;
        }

        // Create a new Stage (window) for showing book details
        Stage detailWindow = new Stage();
        detailWindow.setTitle("Book Details");

        // Register the stage with Main to track it
        Main.registerStage(detailWindow);

        // Main HBox layout for the content
        HBox hbox = new HBox(30);
        hbox.setStyle("-fx-padding: 20px; -fx-alignment: center;");

        // Left section: Book details and QR code
        VBox leftVBox = new VBox(10);
        leftVBox.setStyle("-fx-alignment: top-left;");

        // Consolidate book details into one TextArea with shadow and border styling
        TextArea bookDetailsArea = new TextArea();
        bookDetailsArea.setText(String.format(
                "Title: %s\nAuthor: %s\nCategory: %s\nISBN: %s",
                selectedBook.getDocumentName(),
                selectedBook.getAuthors(),
                selectedBook.getCategory(),
                selectedBook.getIsbn()
        ));
        bookDetailsArea.setWrapText(true);
        bookDetailsArea.setEditable(false);
        bookDetailsArea.setPrefHeight(120);
        bookDetailsArea.setPrefWidth(250);
        bookDetailsArea.getStyleClass().add("content-area");

        leftVBox.getChildren().add(bookDetailsArea);

        // QR code section
        VBox qrVBox = new VBox(10);
        try {
            String tempQRCodePath = "temp_qr_code.png";
            QRCodeGenerator.generateQRCode(selectedBook, tempQRCodePath);

            ImageView qrCodeView = new ImageView(new Image("file:" + tempQRCodePath));
            qrCodeView.setFitHeight(160);
            qrCodeView.setFitWidth(160);
            qrCodeView.setPreserveRatio(true);

            qrVBox.getChildren().add(new Label("QR Code:"));
            qrVBox.getChildren().add(qrCodeView);
        } catch (Exception e) {
            qrVBox.getChildren().add(new Label("QR Code unavailable."));
            showAlert("QR Code Error", "Failed to generate QR code: " + e.getMessage());
        }
        leftVBox.getChildren().add(qrVBox);

        // Middle section: Description
        TextArea descriptionArea = new TextArea();
        descriptionArea.setText(selectedBook.getDescription() != null ? selectedBook.getDescription() : "No description available");
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.setPrefHeight(260);
        descriptionArea.setPrefWidth(260);
        descriptionArea.getStyleClass().add("content-area");

        VBox descriptionVBox = new VBox(10);
        descriptionVBox.getChildren().addAll(new Label("Description:"), descriptionArea);

        // Right section: Empty since there is no cover image
        VBox rightVBox = new VBox(10);
        rightVBox.setStyle("-fx-padding: 28px;-fx-alignment: top-right;");
        rightVBox.getChildren().add(new Label("No cover image available."));

        // Add all sections to the HBox
        hbox.getChildren().addAll(leftVBox, descriptionVBox, rightVBox);

        // Main layout: VBox to include only the content
        VBox mainLayout = new VBox();
        mainLayout.getChildren().add(hbox);

        // Create a scene with the VBox as the root node
        Scene detailScene = new Scene(mainLayout, 820, 400);
        detailScene.getStylesheets().add(APIHelper.class.getResource("/CSS/Books.css").toExternalForm());
        detailWindow.setScene(detailScene);

        // Show the window
        detailWindow.show();
    }

}
