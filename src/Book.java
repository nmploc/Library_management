class Book extends Document {
    public Book(String title, String author, int quantity) {
        super(title, author, quantity);
    }

    @Override
    public void printInfo() {
        System.out.println("Book Title: " + title + ", Author: " + author + ", Quantity: " + quantity);
    }
}
