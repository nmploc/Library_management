class Thesis extends Document {
    public Thesis(String title, String author, int quantity) {
        super(title, author, quantity);
    }

    @Override
    public void printInfo() {
        System.out.println("Thesis Title: " + title + ", Author: " + author + ", Quantity: " + quantity);
    }
}
