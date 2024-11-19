package library;

public class Books {
    private int documentID;
    private String documentName;
    private String authors;
    private String category;
    private int quantity; // Quantity attribute
    private String coverImageUrl; // URL for the book cover

    public Books(int documentID, String documentName, String authors, String category, int quantity) {
        this.documentID = documentID;
        this.documentName = documentName;
        this.authors = authors;
        this.category = category;
        this.quantity = quantity;
    }
    // Constructor with cover image
    public Books(int documentID, String documentName, String authors, String category, int quantity, String coverImageUrl) {
        this.documentID = documentID;
        this.documentName = documentName;
        this.authors = authors;
        this.category = category;
        this.quantity = quantity;
        this.coverImageUrl = coverImageUrl;
    }

    // Getters
    public int getDocumentID() {
        return documentID;
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getAuthors() {
        return authors;
    }

    public String getCategory() {
        return category;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    // Setters
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }
}
