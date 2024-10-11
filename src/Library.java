import java.util.ArrayList;

class Library {
    private ArrayList<Document> documents = new ArrayList<>();
    private ArrayList<User> users = new ArrayList<>();

    public void addDocument(Document document) {
        documents.add(document);
    }

    public Document findDocument(String title) {
        for (Document doc : documents) {
            if (doc.title.equalsIgnoreCase(title)) {
                return doc;
            }
        }
        return null;
    }

    public void borrowDocument(String title, User user) {
        Document doc = findDocument(title);
        if (doc != null && doc.quantity > 0) {
            doc.quantity--;
            System.out.println(user.getName() + " borrowed " + doc.title);
        } else {
            System.out.println("Document is not available.");
        }
    }

    // Thêm các phương thức khác (xóa, sửa, tìm kiếm, hiển thị thông tin...)
}
