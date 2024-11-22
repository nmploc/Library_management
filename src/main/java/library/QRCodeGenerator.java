package library;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class QRCodeGenerator {

    // Method to generate a QR code for book data without ID
    public static void generateQRCode(Books book, String filePath) {
        // Concatenate book details into a single string (excluding ID)
        String qrCodeData = "Book Details:\n" +
                "Name: " + book.getDocumentName() + "\n" +
                "Authors: " + book.getAuthors() + "\n" +
                "Category: " + book.getCategory() + "\n" +
                "Quantity: " + book.getQuantity();

        int width = 300;  // Width of the QR code
        int height = 300; // Height of the QR code

        try {
            // Create a QR code writer
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // Encode the book data into a QR code
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, width, height);

            // Define the file path for the QR code image
            Path path = FileSystems.getDefault().getPath(filePath);

            // Write the QR code to the file
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            System.out.println("QR Code for book generated successfully at: " + filePath);
        } catch (WriterException | IOException e) {
            System.err.println("Error generating QR code: " + e.getMessage());
        }
    }

}
