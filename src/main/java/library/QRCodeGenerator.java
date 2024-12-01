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

    public static void generateQRCode(Books book, String filePath) {
        // Create a URL link to the book on Google Books using the ISBN
        String bookLink = "https://books.google.com/books?id=" + book.getIsbn();

        // Concatenate other book details into a formatted string
        String qrCodeData = "====== Book Details ======\n" +
                "Name      : " + book.getDocumentName() + "\n" +
                "Authors   : " + book.getAuthors() + "\n" +
                "Category  : " + book.getCategory() + "\n" +
                "ISBN      : " + book.getIsbn() + "\n" +
                "Link      : " + bookLink + "\n" +
                "==========================\n" +
                "Scan this QR code to open the book details directly in your browser.";

        int width = 300;  // Width of the QR code
        int height = 300; // Height of the QR code

        try {
            // Create a QR code writer
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // Encode the formatted book details into the QR code
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
