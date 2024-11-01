package library;

public class Books {
    private int documentID;
    private String documentName;
    private int categoryID;
    private String authors;

    // Constructor
    public Books(int documentID, String documentName, int categoryID, String authors) {
        this.documentID = documentID;
        this.documentName = documentName;
        this.categoryID = categoryID;
        this.authors = authors;
    }

    // Getters and Setters
    public int getDocumentID() {
        return documentID;
    }

    public void setDocumentID(int documentID) {
        this.documentID = documentID;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public void setCategoryID(int categoryID) {
        this.categoryID = categoryID;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }
}
