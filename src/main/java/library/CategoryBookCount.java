package library;

public class CategoryBookCount {
    private final String categoryName;
    private final int totalBooks;

    public CategoryBookCount(String categoryName, int totalBooks) {
        this.categoryName = categoryName;
        this.totalBooks = totalBooks;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getTotalBooks() {
        return totalBooks;
    }
}
