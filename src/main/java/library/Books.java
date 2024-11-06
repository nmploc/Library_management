package library;

public class Books {
    private int documentID;
    private String documentName;
    private String authors;
    private String category;

    // Constructor
    public Books(int documentID, String documentName, String authors, String category) {
        this.documentID = documentID;
        this.documentName = documentName;
        this.authors = authors;
        this.category = category;
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
}