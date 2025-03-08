import java.sql.*;
import java.util.Scanner;

public class ClientManager {
    private static final Scanner scanner = new Scanner(System.in);

    public static void manageClients() {
        System.out.println("\n=== Client Management ===");
        System.out.println("1. Add Client\n2. View Clients\n3. Update Client\n4. Delete Client\n5. Back");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        switch (choice) {
            case 1:
                addClient();
                break;
            case 2:
                viewClients();
                break;
            case 3:
                updateClient();
                break;
            case 4:
                deleteClient();
                break;
            case 5:
                return;
            default:
                System.out.println("Invalid choice!");
        }
    }

    private static void addClient() {

        viewClients();
        System.out.print("\nEnter client ID: ");
        int ID = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter client name: ");
        String name = scanner.nextLine();  
        System.out.print("Enter client email address: ");
        String email = scanner.nextLine();              
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO clients (id, name, email) VALUES (?, ?, ?)")) {
            stmt.setInt(1, ID);
            stmt.setString(2, name);
            stmt.setString(3, email);
            stmt.executeUpdate();
            System.out.println("\nClient added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewClients() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM clients")) {
            while (rs.next()) {
                System.out.println(rs.getInt("id") + ". " + rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateClient() {
        System.out.print("\nEnter client ID to update: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter new client name: ");
        String name = scanner.nextLine();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE clients SET name = ? WHERE id = ?")) {
            stmt.setString(1, name);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            System.out.println("\nClient updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteClient() {
        System.out.print("\nEnter client ID to delete: ");
        int id = scanner.nextInt();
        scanner.nextLine();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM clients WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            System.out.println("Client deleted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
