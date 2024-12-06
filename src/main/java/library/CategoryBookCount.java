package library;

public class CategoryBookCount {
    private final String categoryName;
    private final int totalBooks;
    private final int borrowerCount;

    public CategoryBookCount(String categoryName, int totalBooks) {
        this.categoryName = categoryName;
        this.totalBooks = totalBooks;
        this.borrowerCount = 0;
    }

    public CategoryBookCount(String categoryName, int totalBooks, int borrowerCount) {
        this.categoryName = categoryName;
        this.totalBooks = totalBooks;
        this.borrowerCount = borrowerCount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getTotalBooks() {
        return totalBooks;
    }

    public int getBorrowerCount() {
        return borrowerCount;
    }

}
