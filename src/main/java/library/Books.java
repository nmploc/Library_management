package library;

public class Books {
    private int documentID;
    private String documentName;
    private String authors;
    private String category;
    private int quantity; // New quantity attribute

    // Constructor with quantity
    public Books(int documentID, String documentName, String authors, String category, int quantity) {
        this.documentID = documentID;
        this.documentName = documentName;
        this.authors = authors;
        this.category = category;
        this.quantity = quantity;
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

    // Setter for quantity
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
