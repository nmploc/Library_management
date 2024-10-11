abstract class Document {
    protected String title;
    protected String author;
    protected int quantity;

    public Document(String title, String author, int quantity) {
        this.title = title;
        this.author = author;
        this.quantity = quantity;
    }

    public abstract void printInfo();
}
