import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== Virtual Assistant Invoice System ===");
            System.out.println("1. Client Management");
            System.out.println("2. Service Management");
            System.out.println("3. Invoice Management");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    ClientManager.manageClients();
                    break;
                case 2:
                    ServiceManager.manageServices();
                    break;
                case 3:
                    InvoiceManager.manageInvoices();
                    break;
                case 4:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice! Please enter a number between 1-4.");
            }
        }
    }
}