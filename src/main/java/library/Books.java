package library;

public class Books {
    private int documentID;
    private String documentName;
    private String authors;
    private String category;
    private int quantity;
    private String coverImageUrl;
    private String highResCoverImageUrl;
    private String isbn;
    private String description; // New field for description

    // Constructor without cover image, ISBN, and description
    public Books(String documentName, String authors, String category, int quantity) {
        this.documentName = documentName;
        this.authors = authors;
        this.category = category;
        this.quantity = quantity;
    }

    public Books(int documentID, String documentName, String authors, String category, int quantity) {
        this.documentID = documentID;
        this.documentName = documentName;
        this.authors = authors;
        this.category = category;
        this.quantity = quantity;
    }

    public Books(int documentID, String documentName, String authors, String category, int quantity, String isbn) {
        this.documentID = documentID;
        this.documentName = documentName;
        this.authors = authors;
        this.category = category;
        this.quantity = quantity;
        this.isbn = isbn;
    }

    public Books(int documentID, String documentName, String authors, String category, int quantity, String isbn,  String description) {
        this.documentID = documentID;
        this.documentName = documentName;
        this.authors = authors;
        this.category = category;
        this.quantity = quantity;
        this.isbn = isbn;
        this.description = description;
    }

    // Constructor with cover image, ISBN, and description
    public Books(int documentID, String documentName, String authors, String category, int quantity, String coverImageUrl, String isbn, String description) {
        this.documentID = documentID;
        this.documentName = documentName;
        this.authors = authors;
        this.category = category;
        this.quantity = quantity;
        this.coverImageUrl = coverImageUrl;
        this.isbn = isbn;
        this.description = description;
    }

    // Constructor with cover image, high-resolution cover image, ISBN, and description
    public Books(int documentID, String documentName, String authors, String category, int quantity, String coverImageUrl, String highResCoverImageUrl, String isbn, String description) {
        this.documentID = documentID;
        this.documentName = documentName;
        this.authors = authors;
        this.category = category;
        this.quantity = quantity;
        this.coverImageUrl = coverImageUrl;
        this.highResCoverImageUrl = highResCoverImageUrl;
        this.isbn = isbn;
        this.description = description;
    }

    // Getters and setters for all fields
    public int getDocumentID() { return documentID; }
    public void setDocumentID(int documentID) { this.documentID = documentID; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getAuthors() { return authors; }
    public void setAuthors(String authors) { this.authors = authors; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public String getHighResCoverImageUrl() { return highResCoverImageUrl; }
    public void setHighResCoverImageUrl(String highResCoverImageUrl) { this.highResCoverImageUrl = highResCoverImageUrl; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getDescription() { return description; } // Getter for description
    public void setDescription(String description) { this.description = description; } // Setter for description
}
