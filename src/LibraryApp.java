import java.util.Scanner;
public class LibraryApp {
    private static Library library = new Library();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("Welcome to My Application!");
            System.out.println("[0] Exit");
            System.out.println("[1] Add Document");
            System.out.println("[2] Remove Document");
            System.out.println("[3] Update Document");
            System.out.println("[4] Find Document");
            System.out.println("[5] Display Document");
            System.out.println("[6] Add User");
            System.out.println("[7] Borrow Document");
            System.out.println("[8] Return Document");
            System.out.println("[9] Display User Info");

            choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    // Gọi phương thức thêm tài liệu
                    break;
                case 2:
                    // Gọi phương thức xóa tài liệu
                    break;
                case 3:
                    // Gọi phương thức sửa tài liệu
                    break;
                case 4:
                    // Gọi phương thức tìm kiếm tài liệu
                    break;
                case 5:
                    // Gọi phương thức hiển thị tài liệu
                    break;
                case 6:
                    // Gọi phương thức thêm người dùng
                    break;
                case 7:
                    // Gọi phương thức mượn tài liệu
                    break;
                case 8:
                    // Gọi phương thức trả tài liệu
                    break;
                case 9:
                    // Gọi phương thức hiển thị thông tin người dùng
                    break;
                case 0:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Action is not supported.");
                    break;
            }
        } while (choice != 0);

        scanner.close();
    }
}